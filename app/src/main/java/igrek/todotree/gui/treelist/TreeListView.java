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
import android.view.ViewTreeObserver;
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

    /** początkowa pozycja kursora przy rozpoczęciu przeciągania (ulega zmianie przy zamianie elementów) */
    private float startTouchY;
    /** aktualna pozycja kursora względem listview */
    private float lastTouchY;

    /** pozycja aktualnie przeciąganego elementu */
    private Integer draggedItemPos = null;
    /** widok aktualnie przeciąganego elementu */
    private View draggedItemView = null;


    private int scrollOffset = 0;

    private BitmapDrawable hoverBitmap;
    private Rect hoverBitmapBounds;


    private final int MOVE_DURATION = 1;

    private int mSmoothScrollAmountAtEdge = 0;

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
        int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 20;
        mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new TreeItemAdapter(context, null, guiListener, this);
        setAdapter(adapter);
    }

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
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

    /**
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the listview's items whenever the listview is redrawn.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverBitmap != null) {
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
                Output.log("listview touch move");
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

    /**
     * Retrieves the view in the list corresponding to itemID
     */
    public View getViewByPosition(int itemPos) {
        if (itemPos < 0) return null;
        if (itemPos >= items.size()) return null;
        int itemNum = itemPos - getFirstVisiblePosition();
        return getChildAt(itemNum);
    }


    @Override
    public void invalidate() {
        super.invalidate();
        if(draggedItemPos != null) {
            draggedItemView = getViewByPosition(draggedItemPos);
            if(draggedItemView != null){
                draggedItemView.setVisibility(View.INVISIBLE);
                Output.log("invalidate: draggedItemPos invisible : " + draggedItemPos);
            }
        }
    }


    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
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


    /**
     * This scroll listener is added to the listview in order to handle cell swapping
     * when the cell is either at the top or bottom edge of the listview. If the hover
     * cell is at either edge of the listview, the listview will begin scrolling. As
     * scrolling takes place, the listview continuously checks if new cells became visible
     * and determines whether they are potential candidates for a cell swap.
     */

    /**
     * This method is in charge of determining if the hover cell is above
     * or below the bounds of the listview. If so, the listview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 100);
            Output.log("scrolled -");
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 100);
            Output.log("scrolled +");
            return true;
        }

        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }




    private void itemDraggingStarted(int position, View itemView) {
        Output.log("item dragging started");
        draggedItemPos = position;
        draggedItemView = itemView;

        scrollOffset = 0;

        hoverBitmap = getAndAddHoverView(draggedItemView);
        draggedItemView.setVisibility(INVISIBLE);

        invalidate();
    }

    private void handleItemDragging() {
        Output.log("handle item dragging");
        final float dy = lastTouchY - startTouchY;
        float dyTotal = dy + scrollOffset;

        Output.log("dy: "+ dy + ", dyTotal: " + dyTotal);

        if (draggedItemView == null) {
            Output.error("draggedItemView = null");
            return;
        }
        if (draggedItemPos == null) {
            Output.error("draggedItemPos = null");
            return;
        }

        hoverBitmapBounds.offsetTo(draggedItemView.getLeft(), draggedItemView.getTop() + (int) dy + scrollOffset);
        hoverBitmap.setBounds(hoverBitmapBounds);

        draggedItemView.setVisibility(View.INVISIBLE);

        int stepUp = (int)(-dy / draggedItemView.getHeight() + 0.4); //minimalne nałożenie się itemów: 60 %
        int stepDown = (int)(dy / draggedItemView.getHeight() + 0.4); //minimalne nałożenie się itemów: 60 %

        int step = stepDown > 0 ? stepDown : (stepUp > 0 ? -stepUp : 0);
        //walidacja wyjścia poza granicę
        int targetPosition = draggedItemPos + step;
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= items.size()) targetPosition = items.size() - 1;
        step = targetPosition - draggedItemPos;

        if(step != 0){
            Output.log("item pos: " + draggedItemPos + " moving by step: " + step);

            int newPos = guiListener.onItemMoved(draggedItemPos, step);

            startTouchY += step * draggedItemView.getHeight();
            scrollOffset = 0;

            draggedItemPos = newPos;
            draggedItemView = getViewByPosition(draggedItemPos);

            //adapter.setDataSource(items);
/*
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    switchView.setTranslationY(0);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, View.TRANSLATION_Y, 0);
                        animator.setDuration(MOVE_DURATION);
                        animator.start();
                    }

                    return true;
                }
            });
            */

        }


        handleMobileCellScroll(hoverBitmapBounds);

        invalidate();

    }

    private void itemDraggingStopped() {
        Output.log("item dragging stopped");
        if (draggedItemPos != null && draggedItemView != null) {

            hoverBitmapBounds.offsetTo(draggedItemView.getLeft(), draggedItemView.getTop());

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
                    draggedItemView.setVisibility(VISIBLE);
                    hoverBitmap = null;
                    draggedItemPos = null;
                    draggedItemView = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        }else{
            draggedItemPos = null;
            draggedItemView = null;
        }
    }


    public void onItemMoveButtonPressed(int position, TreeItem item, View itemView, float touchX, float touchY) {
        startTouchY = itemView.getTop() + touchY;
        lastTouchY = startTouchY;
        itemDraggingStarted(position, itemView);

        Output.log("Button move pressed, item: " + position + ", relative touchY: " + touchY + ", startTouchY : " + startTouchY);
    }

    public void onItemMoveButtonReleased(int position, TreeItem item, View itemView, float touchX, float touchY) {
        itemDraggingStopped();

        Output.log("Button move released, item: " + position);
    }
}
