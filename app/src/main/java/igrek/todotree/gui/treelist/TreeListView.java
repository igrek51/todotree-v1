package igrek.todotree.gui.treelist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import igrek.todotree.gui.GUIListener;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.system.output.Output;

public class TreeListView extends ListView implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private List<TreeItem> items;
    private TreeItemAdapter adapter;
    private GUIListener guiListener;

    /**
     * początkowa pozycja kursora przy rozpoczęciu przeciągania (ulega zmianie przy zamianie elementów)
     */
    private float startTouchY;
    /**
     * aktualna pozycja kursora względem ekranu i listview (bez scrollowania)
     */
    private float lastTouchY;

    /**
     * pozycja aktualnie przeciąganego elementu
     */
    private Integer draggedItemPos = null;
    /**
     * widok aktualnie przeciąganego elementu
     */
    private View draggedItemView = null;
    /**
     * oryginalna górna pozycja niewidocznego elementu na liście, którego bitmapa jest przeciągana
     */
    private Integer draggedItemViewTop = null;
    /**
     * wysokość jednego elementu
     */
    private Integer draggedItemViewHeight = null;

    /**
     * aktualne położenie scrolla
     */
    private int scrollOffset = 0;
    /**
     * położenie scrolla przy rozpoczęciu przeciągania
     */
    private int scrollStart = 0;

    private BitmapDrawable hoverBitmap;
    private Rect hoverBitmapBounds;

    private int scrollEdge = 0;
    private final int SMOOTH_SCROLL_EDGE_DP = 140;
    private final float SMOOTH_SCROLL_FACTOR = 0.4f;
    private final int SMOOTH_SCROLL_DURATION = 10;

    public TreeListView(Context context) {
        super(context);
    }

    public TreeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TreeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, GUIListener aGuiListener) {
        this.guiListener = aGuiListener;
        setOnScrollListener(this);
        setOnItemClickListener(this);
        //setOnItemLongClickListener(this);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        scrollEdge = (int) (SMOOTH_SCROLL_EDGE_DP / metrics.density);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new TreeItemAdapter(context, null, guiListener, this);
        setAdapter(adapter);
    }

    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapWithBorder(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        hoverBitmapBounds = new Rect(left, top, left + w, top + h);

        drawable.setBounds(hoverBitmapBounds);

        return drawable;
    }

    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        int LINE_THICKNESS = 5;
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(0xcc505050);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverBitmap != null) {
            updateHoverBitmap();
            hoverBitmap.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Output.log("listview touch down: " + event.getX() + " , " + event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (draggedItemPos != null) {
                    lastTouchY = event.getY();
                    handleItemDragging();
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                itemDraggingStopped();
                break;
            case MotionEvent.ACTION_CANCEL:
                itemDraggingStopped();
                break;
        }
        return super.onTouchEvent(event);
    }

    public View getViewByPosition(int itemPos) {
        if (itemPos < 0) return null;
        if (itemPos >= items.size()) return null;
        int itemNum = itemPos - getFirstVisiblePosition();
        return getChildAt(itemNum);
    }


    @Override
    public void invalidate() {
        super.invalidate();
        if (draggedItemPos != null) {
            draggedItemView = getViewByPosition(draggedItemPos);
            if (draggedItemView != null) {
                draggedItemView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private final static TypeEvaluator<Rect> rectBoundsEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };


    public void setItems(List<TreeItem> items) {
        this.items = items;
        adapter.setDataSource(items);
        invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (position == adapter.getCount() - 1) {
            //nowy element
            guiListener.onAddItemClicked();
        } else {
            //istniejący element
            TreeItem item = adapter.getItem(position);
            guiListener.onItemClicked(position, item);
        }
    }

    public boolean handleScrolling() {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = hoverBitmapBounds.top;
        int hoverHeight = hoverBitmapBounds.height();

        if (draggedItemPos != null) {
            if (hoverViewTop <= scrollEdge && offset > 0) {
                int scrollDistance = (int)((hoverViewTop - scrollEdge) * SMOOTH_SCROLL_FACTOR);
                smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
                Output.log("scrolled " + scrollDistance);
                return true;
            }
            if (hoverViewTop + hoverHeight >= height - scrollEdge && (offset + extent) < range) {
                int scrollDistance = (int)((hoverViewTop + hoverHeight - height + scrollEdge) * SMOOTH_SCROLL_FACTOR);
                smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
                Output.log("scrolled " + scrollDistance);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        scrollOffset = getREALScrollPosition();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (draggedItemPos != null) {
                handleScrolling();
                Output.log("rescroll");
            }
        }
    }


    private void updateHoverBitmap() {
        if (draggedItemViewTop != null) {
            float dy = lastTouchY - startTouchY;
            hoverBitmapBounds.offsetTo(0, draggedItemViewTop + (int) dy);
            hoverBitmap.setBounds(hoverBitmapBounds);
        }
    }


    private void itemDraggingStarted(int position, View itemView) {
        Output.log("item dragging started");
        draggedItemPos = position;
        draggedItemView = itemView;
        draggedItemViewTop = itemView.getTop();
        draggedItemViewHeight = itemView.getHeight();
        scrollStart = scrollOffset;

        hoverBitmap = getAndAddHoverView(draggedItemView);
        draggedItemView.setVisibility(INVISIBLE);

        invalidate();
    }

    private void handleItemDragging() {
        final float dy = lastTouchY - startTouchY;
        float dyTotal = dy + scrollOffset - scrollStart;

        Output.log("handleItemDragging: dy: " + dy + ", dyTotal: " + dyTotal);
        Output.log("scroll diff: " + (scrollOffset - scrollStart));
        Output.log("view height: " + draggedItemViewHeight);

        if (draggedItemViewTop == null) {
            Output.error("draggedItemViewTop = null");
            return;
        }
        if (draggedItemPos == null) {
            Output.error("draggedItemPos = null");
            return;
        }
        if (draggedItemViewHeight == null) {
            Output.error("draggedItemViewHeight = null");
            return;
        }

        int stepUp = (int) (-dyTotal / draggedItemViewHeight + 0.4); //minimalne nałożenie się itemów: 60 %
        int stepDown = (int) (dyTotal / draggedItemViewHeight + 0.4); //minimalne nałożenie się itemów: 60 %
        int step = stepDown > 0 ? stepDown : (stepUp > 0 ? -stepUp : 0);
        //walidacja wyjścia poza granicę
        int targetPosition = draggedItemPos + step;
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= items.size()) targetPosition = items.size() - 1;
        step = targetPosition - draggedItemPos;

        Output.log("item pos: " + draggedItemPos + " moving by step: " + step);
        if (step != 0) {
            items = guiListener.onItemMoved(draggedItemPos, step);
            adapter.setDataSource(items);

            startTouchY += step * draggedItemViewHeight;
            draggedItemViewTop += step * draggedItemViewHeight;

            draggedItemPos = targetPosition;
            if(draggedItemView != null) {
                draggedItemView.setVisibility(View.VISIBLE);
            }
            draggedItemView = getViewByPosition(draggedItemPos);
            if(draggedItemView != null) {
                draggedItemView.setVisibility(View.INVISIBLE);
            }
        }

        handleScrolling();

        invalidate();
    }

    private void itemDraggingStopped() {
        Output.log("item dragging stopped");
        if (draggedItemPos != null && draggedItemViewTop != null) {

            hoverBitmapBounds.offsetTo(0, draggedItemViewTop);

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverBitmap, "bounds", rectBoundsEvaluator, hoverBitmapBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (draggedItemView == null) {
                        Output.error("itemDraggingStopped: onAnimationEnd: draggedItemView = null");
                    } else {
                        draggedItemView.setVisibility(VISIBLE);
                    }
                    hoverBitmap = null;
                    draggedItemPos = null;
                    draggedItemView = null;
                    draggedItemViewTop = null;
                    draggedItemViewHeight = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            draggedItemPos = null;
            draggedItemView = null;
            draggedItemViewTop = null;
            draggedItemViewHeight = null;
        }
    }

    private int getREALScrollPosition() {
        if (getChildAt(0) == null) {
            return 0;
        }
        return getChildAt(0).getHeight() * getFirstVisiblePosition() - getChildAt(0).getTop();
    }


    public void onItemMoveButtonPressed(int position, TreeItem item, View itemView, float touchX, float touchY) {
        startTouchY = itemView.getTop() + touchY;
        lastTouchY = startTouchY;
        itemDraggingStarted(position, itemView);

        Output.log("Button move pressed, item: " + position + ", relative touchY: " + touchY + ", startTouchY : " + startTouchY);
    }

    public void onItemMoveButtonReleased(int position, TreeItem item, View itemView, float touchX, float touchY) {
        itemDraggingStopped();
    }
}
