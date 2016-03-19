package igrek.todotree.gui.treelist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

public class TreeListView extends ListView implements AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private List<TreeItem> items;
    private TreeItemAdapter adapter;
    private GUIListener guiListener;

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 1;
    private final int LINE_THICKNESS = 5;

    private int mLastEventY = -1;

    private int mDownY = -1;
    private int mDownX = -1;

    private int mTotalOffset = 0;

    private boolean mCellIsMobile = false;
    private boolean mIsMobileScrolling = false;
    private int mSmoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private int aboveItemPos = INVALID_ID;
    private int mobileItemPos = INVALID_ID;
    private int belowItemPos = INVALID_ID;

    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean isWaitingForScrollFinish = false;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

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
        setOnItemLongClickListener(this);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new TreeItemAdapter(context, null, guiListener);
        setAdapter(adapter);
    }

    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
        mTotalOffset = 0;

        int position = pointToPosition(mDownX, mDownY);
        if (position != adapter.getCount() - 1) {
            int itemNum = position - getFirstVisiblePosition();

            View selectedView = getChildAt(itemNum);
            mobileItemPos = position;
            mHoverCell = getAndAddHoverView(selectedView);
            selectedView.setVisibility(INVISIBLE);

            mCellIsMobile = true;

            updateNeighborViews(mobileItemPos);

            Output.log("item long click");

            return true;
        } else {
            return false;
        }
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

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }

    /**
     * Draws a black border over the screenshot of the view passed in.
     */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(0xcc505050);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Stores a reference to the views above and below the item currently
     * corresponding to the hover cell. It is important to note that if this
     * item is either at the top or bottom of the list, aboveItemPos or belowItemPos
     * may be invalid.
     */
    private void updateNeighborViews(int position) {
        aboveItemPos = position > 0 ? position - 1 : -1;
        belowItemPos = position < items.size() - 1 ? position + 1 : -1;
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

    /**
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the listview's items whenever the listview is redrawn.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);

                mLastEventY = (int) event.getY(pointerIndex);
                int deltaY = mLastEventY - mDownY;

                if (mCellIsMobile) {
                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    mIsMobileScrolling = false;
                    handleMobileCellScroll();

                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     * Using a ViewTreeObserver and a corresponding OnPreDrawListener, we can
     * offset the cell being swapped to where it previously was and then animate it to
     * its new position.
     */
    private void handleCellSwitch() {
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewByPosition(belowItemPos);
        View mobileView = getViewByPosition(mobileItemPos);
        View aboveView = getViewByPosition(aboveItemPos);

        if(mobileView != null){
            mobileView.setVisibility(View.INVISIBLE);
        }

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {

            final int switchItemPos = isBelow ? belowItemPos : aboveItemPos;
            final View switchView = isBelow ? belowView : aboveView;

            if (switchView == null) {
                updateNeighborViews(mobileItemPos);
                return;
            }

            swapElements(items, mobileItemPos, switchItemPos);

            //adapter.setDataSource(items);

            mDownY = mLastEventY;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.INVISIBLE); // ?????
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViews(mobileItemPos);


            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    mTotalOffset += deltaY;

                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    switchView.setTranslationY(delta);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, View.TRANSLATION_Y, 0);
                        animator.setDuration(MOVE_DURATION);
                        animator.start();
                    }

                    return true;
                }
            });

            mobileItemPos = switchItemPos;
            updateNeighborViews(mobileItemPos);
        }
    }

    private void swapElements(List arrayList, int indexOne, int indexTwo) {
        guiListener.onItemsSwapped(indexOne, indexTwo);

        Output.log("elements swapped: "+indexOne + ", " + indexTwo);
        //        Object temp = arrayList.get(indexOne);
        //        arrayList.set(indexOne, arrayList.get(indexTwo));
        //        arrayList.set(indexTwo, temp);
    }


    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded() {
        final View mobileView = getViewByPosition(mobileItemPos);
        if (mCellIsMobile || isWaitingForScrollFinish) {
            mCellIsMobile = false;
            isWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                isWaitingForScrollFinish = true;
                return;
            }

            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds", sBoundEvaluator, mHoverCellCurrentBounds);
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
                    aboveItemPos = INVALID_ID;
                    mobileItemPos = INVALID_ID;
                    belowItemPos = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    mHoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled() {
        View mobileView = getViewByPosition(mobileItemPos);
        if (mCellIsMobile) {
            aboveItemPos = INVALID_ID;
            mobileItemPos = INVALID_ID;
            belowItemPos = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            invalidate();
        }
        mCellIsMobile = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    /**
     * Determines whether this listview is in a scrolling state invoked
     * by the fact that the hover cell is out of the bounds of the listview;
     */
    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }

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
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }


    public void setItems(List<TreeItem> items) {
        this.items = items;
        adapter.setDataSource(items);
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

    private int mPreviousFirstVisibleItem = -1;
    private int mPreviousVisibleItemCount = -1;
    private int mCurrentFirstVisibleItem;
    private int mCurrentVisibleItemCount;
    private int mCurrentScrollState;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCurrentFirstVisibleItem = firstVisibleItem;
        mCurrentVisibleItemCount = visibleItemCount;

        mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
        mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount : mPreviousVisibleItemCount;

        checkAndHandleFirstVisibleCellChange();
        checkAndHandleLastVisibleCellChange();

        mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
        mPreviousVisibleItemCount = mCurrentVisibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;
        mScrollState = scrollState;
        isScrollCompleted();
    }

    /**
     * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview
     * is in a state of scrolling invoked by the hover cell being outside the bounds
     * of the listview, then this scrolling event is continued. Secondly, if the hover
     * cell has already been released, this invokes the animation for the hover cell
     * to return to its correct position after the listview has entered an idle scroll
     * state.
     */
    private void isScrollCompleted() {
        if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
            if (mCellIsMobile && mIsMobileScrolling) {
                handleMobileCellScroll();
            } else if (isWaitingForScrollFinish) {
                touchEventsEnded();
            }
        }
    }

    /**
     * Determines if the listview scrolled up enough to reveal a new cell at the
     * top of the list. If so, then the appropriate parameters are updated.
     */
    public void checkAndHandleFirstVisibleCellChange() {
        if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
            if (mCellIsMobile && mobileItemPos != INVALID_ID) {
                updateNeighborViews(mobileItemPos);
                handleCellSwitch();
            }
        }
    }

    /**
     * Determines if the listview scrolled down enough to reveal a new cell at the
     * bottom of the list. If so, then the appropriate parameters are updated.
     */
    public void checkAndHandleLastVisibleCellChange() {
        int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
        int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
        if (currentLastVisibleItem != previousLastVisibleItem) {
            if (mCellIsMobile && mobileItemPos != INVALID_ID) {
                updateNeighborViews(mobileItemPos);
                handleCellSwitch();
            }
        }
    }
}
