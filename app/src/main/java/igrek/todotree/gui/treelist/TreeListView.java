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

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;
import igrek.todotree.logic.LogicActionController;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;

public class TreeListView extends ListView implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	LogicActionController actionController;
	
	private List<TreeItem> items;
	private TreeItemAdapter adapter;
	
	/** początkowa pozycja kursora przy rozpoczęciu przeciągania (ulega zmianie przy zamianie elementów) */
	private float startTouchY;
	/** aktualna pozycja kursora względem ekranu i listview (bez scrollowania) */
	private float lastTouchY;
	
	/** pozycja aktualnie przeciąganego elementu */
	private Integer draggedItemPos = null;
	/** widok aktualnie przeciąganego elementu */
	private View draggedItemView = null;
	/** oryginalna górna pozycja niewidocznego elementu na liście, którego bitmapa jest przeciągana */
	private Integer draggedItemViewTop = null;
	
	/** aktualne położenie scrolla */
	private Integer scrollOffset = 0;
	/** położenie scrolla przy rozpoczęciu przeciągania */
	private Integer scrollStart = 0;
	
	/** położenie X punktu rozpoczęcia gestu */
	private Float gestureStartX;
	/** położenie Y punktu rozpoczęcia gestu */
	private Float gestureStartY;
	/** położenie scrolla podczas rozpoczęcia gestu */
	private Integer gestureStartScroll;
	/** numer pozycji, na której rozpoczęto gest */
	private Integer gestureStartPos;
	
	/** index widoku na wysokość */
	private HashMap<Integer, Integer> itemHeights = new HashMap<>();
	
	private int scrollState = SCROLL_STATE_IDLE;
	
	private BitmapDrawable hoverBitmap = null;
	private BitmapDrawable hoverBitmapAnimation = null;
	private Rect hoverBitmapBounds;
	
	private final int SMOOTH_SCROLL_EDGE_DP = 160;
	private int SMOOTH_SCROLL_EDGE_PX;
	private final float SMOOTH_SCROLL_FACTOR = 0.34f;
	private final int SMOOTH_SCROLL_DURATION = 10;
	
	private final float ITEMS_REPLACE_COVER = 0.65f;
	
	private final int HOVER_BORDER_THICKNESS = 5;
	private final int HOVER_BORDER_COLOR = 0xccb0b0b0;
	
	private final float GESTURE_MIN_DX = 0.27f;
	private final float GESTURE_MAX_DY = 0.8f;
	
	{
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public TreeListView(Context context) {
		super(context);
	}
	
	public TreeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public TreeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init(Context context) {
		setOnScrollListener(this);
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		SMOOTH_SCROLL_EDGE_PX = (int) (SMOOTH_SCROLL_EDGE_DP / metrics.density);
		setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		adapter = new TreeItemAdapter(context, null, this, actionController);
		setAdapter(adapter);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				gestureStartX = event.getX();
				gestureStartY = event.getY();
				gestureStartScroll = scrollOffset;
				break;
			case MotionEvent.ACTION_MOVE:
				if (draggedItemPos != null) {
					lastTouchY = event.getY();
					handleItemDragging();
					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (handleItemGesture(event.getX(), event.getY(), scrollOffset)) {
					return super.onTouchEvent(event);
				}
			case MotionEvent.ACTION_CANCEL:
				itemDraggingStopped();
				gestureStartX = null;
				gestureStartY = null;
				gestureStartScroll = null;
				gestureStartPos = null;
				break;
		}
		return super.onTouchEvent(event);
	}
	
	public void onItemTouchDown(int position, MotionEvent event, View v) {
		gestureStartPos = position;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (draggedItemPos != null) {
			draggedItemView = getItemView(draggedItemPos);
			if (draggedItemView != null) {
				draggedItemView.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hoverBitmap != null) {
			updateHoverBitmap();
			hoverBitmap.draw(canvas);
		}
		if (hoverBitmapAnimation != null) {
			hoverBitmapAnimation.draw(canvas);
		}
	}
	
	public void setItemsAndSelected(List<TreeItem> items, List<Integer> selectedPositions) {
		adapter.setSelections(selectedPositions);
		setItems(items);
	}
	
	public void setItems(List<TreeItem> items) {
		this.items = items;
		adapter.setDataSource(items);
		invalidate();
		calculateViewHeights();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		if (position == items.size()) {
			//nowy element
			actionController.addItemClicked();
		} else {
			//istniejący element
			TreeItem item = adapter.getItem(position);
			actionController.itemClicked(position, item);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		itemDraggingStopped();
		if (position == items.size()) {
			//nowy element na końcu
			actionController.addItemClicked();
		} else {
			//tryb zaznaczania elementów
			actionController.itemLongClicked(position);
			gestureStartPos = null;
		}
		return true;
	}
	
	
	private void updateHoverBitmap() {
		if (draggedItemViewTop != null && draggedItemPos != null) {
			float dy = lastTouchY - startTouchY;
			hoverBitmapBounds.offsetTo(0, draggedItemViewTop + (int) dy);
			hoverBitmap.setBounds(hoverBitmapBounds);
		}
	}
	
	private BitmapDrawable getAndAddHoverView(View v) {
		int top = v.getTop();
		int left = v.getLeft();
		
		Bitmap b = getBitmapWithBorder(v);
		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);
		
		hoverBitmapBounds = new Rect(left, top, left + v.getWidth(), top + v.getHeight());
		drawable.setBounds(hoverBitmapBounds);
		
		return drawable;
	}
	
	private Bitmap getBitmapWithBorder(View v) {
		Bitmap bitmap = getBitmapFromView(v);
		Canvas can = new Canvas(bitmap);
		
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(HOVER_BORDER_THICKNESS);
		paint.setColor(HOVER_BORDER_COLOR);
		
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
	
	
	private int getItemHeight(int position) {
		Integer h = itemHeights.get(position);
		if (h == null) {
			Logs.warn("Item View = null");
		}
		return h != null ? h : 0;
	}
	
	private View getItemView(int position) {
		return adapter.getStoredView(position);
	}
	
	private void calculateViewHeights() {
		//TODO: przyspieszyć pomiar wysokości elementów (ładowanie widoków)
		int measureSpecW = MeasureSpec.makeMeasureSpec(this.getWidth(), MeasureSpec.EXACTLY);
		int measureSpecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		itemHeights = new HashMap<>();
		for (int i = 0; i < adapter.getCount(); i++) {
			View mView = adapter.getView(i, null, this);
			mView.measure(measureSpecW, measureSpecH);
			itemHeights.put(i, mView.getMeasuredHeight());
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
	
	
	private void itemDraggingStarted(int position, View itemView) {
		draggedItemPos = position;
		draggedItemView = itemView;
		draggedItemViewTop = itemView.getTop();
		scrollStart = scrollOffset;
		
		hoverBitmap = getAndAddHoverView(draggedItemView);
		draggedItemView.setVisibility(INVISIBLE);
		
		invalidate();
	}
	
	private void handleItemDragging() {
		
		float dyTotal = lastTouchY - startTouchY + (scrollOffset - scrollStart);
		
		if (draggedItemViewTop == null) {
			Logs.error("draggedItemViewTop = null");
			return;
		}
		if (draggedItemPos == null) {
			Logs.error("draggedItemPos = null");
			return;
		}
		
		int step = 0;
		int deltaH = 0;
		
		if (dyTotal > 0) { //przesuwanie w dół
			while (true) {
				if (draggedItemPos + step + 1 >= items.size())
					break;
				int downHeight = getItemHeight(draggedItemPos + step + 1);
				if (downHeight == 0)
					break;
				if (dyTotal - deltaH > downHeight * ITEMS_REPLACE_COVER) {
					step++;
					deltaH += downHeight;
				} else {
					break;
				}
			}
		} else if (dyTotal < 0) { //przesuwanie w górę
			while (true) {
				if (draggedItemPos + step - 1 < 0)
					break;
				int upHeight = getItemHeight(draggedItemPos + step - 1);
				if (upHeight == 0)
					break;
				if (-dyTotal + deltaH > upHeight * ITEMS_REPLACE_COVER) {
					step--;
					deltaH -= upHeight;
				} else {
					break;
				}
			}
		}
		
		if (step != 0) {
			int targetPosition = draggedItemPos + step;
			
			actionController.itemMoved(draggedItemPos, step);
			items = treeManager.getCurrentItem().getChildren();
			
			//update mapy wysokości
			if (step > 0) {
				Integer draggedItemHeight = itemHeights.get(draggedItemPos);
				for (int i = draggedItemPos; i < targetPosition; i++) {
					Integer nextHeight = itemHeights.get(i + 1);
					itemHeights.put(i, nextHeight);
				}
				itemHeights.put(targetPosition, draggedItemHeight);
			} else if (step < 0) {
				Integer draggedItemHeight = itemHeights.get(draggedItemPos);
				for (int i = draggedItemPos; i > targetPosition; i--) {
					Integer nextHeight = itemHeights.get(i - 1);
					itemHeights.put(i, nextHeight);
				}
				itemHeights.put(targetPosition, draggedItemHeight);
			}
			
			adapter.setDataSource(items);
			
			startTouchY += deltaH;
			draggedItemViewTop += deltaH;
			
			draggedItemPos = targetPosition;
			if (draggedItemView != null) {
				draggedItemView.setVisibility(View.VISIBLE);
			}
			draggedItemView = getItemView(draggedItemPos);
			if (draggedItemView != null) {
				draggedItemView.setVisibility(View.INVISIBLE);
			}
		}
		
		handleScrolling();
		
		invalidate();
	}
	
	private void itemDraggingStopped() {
		if (draggedItemPos != null && draggedItemViewTop != null) {
			//wyłączenie automatycznego ustawiania pozycji hover bitmapy
			draggedItemPos = null;
			
			//animacja powrotu do aktualnego połozenia elementu
			// kopia referencji na czas trwania animacji
			hoverBitmapAnimation = hoverBitmap;
			hoverBitmapBounds.offsetTo(0, draggedItemViewTop - (scrollOffset - scrollStart));
			final Rect hoverBitmapBoundsCopy = new Rect(hoverBitmapBounds);
			final View draggedItemViewCopy = draggedItemView;
			
			draggedItemView = null;
			hoverBitmap = null;
			draggedItemViewTop = null;
			
			ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverBitmapAnimation, "bounds", rectBoundsEvaluator, hoverBitmapBoundsCopy);
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
					if (draggedItemViewCopy != null) {
						draggedItemViewCopy.setVisibility(VISIBLE);
					}
					hoverBitmapAnimation = null;
					setEnabled(true);
					invalidate();
				}
			});
			hoverViewAnimator.start();
		} else {
			draggedItemPos = null;
			draggedItemView = null;
			draggedItemViewTop = null;
		}
	}
	
	
	public boolean handleScrolling() {
		if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			int offset = computeVerticalScrollOffset();
			int height = getHeight();
			int extent = computeVerticalScrollExtent();
			int range = computeVerticalScrollRange();
			int hoverViewTop = hoverBitmapBounds.top;
			int hoverHeight = hoverBitmapBounds.height();
			
			if (draggedItemPos != null) {
				if (hoverViewTop <= SMOOTH_SCROLL_EDGE_PX && offset > 0) {
					int scrollDistance = (int) ((hoverViewTop - SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
					smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
					return true;
				}
				if (hoverViewTop + hoverHeight >= height - SMOOTH_SCROLL_EDGE_PX && (offset + extent) < range) {
					int scrollDistance = (int) ((hoverViewTop + hoverHeight - height + SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
					smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		scrollOffset = getRealScrollPosition();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
		if (scrollState == SCROLL_STATE_IDLE) {
			if (draggedItemPos != null) {
				boolean scrollingResult = handleScrolling();
				// nie da się scrollować - dojechano do końca zakresu
				if (!scrollingResult) {
					handleItemDragging(); // sprawdzenie, czy elementy powinny zostać przemieszczone
				}
			}
		}
	}
	
	private int getRealScrollPosition() {
		if (getChildAt(0) == null) {
			return 0;
		}
		int sumh = 0;
		for (int i = 0; i < getFirstVisiblePosition(); i++) {
			sumh += getItemHeight(i);
		}
		//separatory
		//sumh += getFirstVisiblePosition() * getDividerHeight();
		return sumh - getChildAt(0).getTop();
	}
	
	/**
	 * @param position pozycja elementu do przescrollowania (-1 - ostatni element)
	 */
	public void scrollToItem(int position) {
		if (position == -1)
			position = items.size() - 1;
		if (position < 0)
			position = 0;
		setSelection(position);
		invalidate();
	}
	
	public void scrollToPosition(int y) {
		//wyznaczenie najbliższego elementu i przesunięcie względem niego
		int position = 0;
		while (y > itemHeights.get(position)) {
			y -= itemHeights.get(position);
			position++;
		}
		
		setSelection(position);
		smoothScrollBy(y, 50);
		
		invalidate();
	}
	
	public void scrollToBottom() {
		setSelection(items.size());
	}
	
	public Integer getCurrentScrollPosition() {
		return getRealScrollPosition();
	}
	
	public void onItemMoveButtonPressed(int position, TreeItem item, View itemView, float touchX, float touchY) {
		startTouchY = itemView.getTop() + touchY;
		lastTouchY = startTouchY;
		itemDraggingStarted(position, itemView);
	}
	
	public void onItemMoveButtonReleased(int position, TreeItem item, View itemView, float touchX, float touchY) {
		itemDraggingStopped();
	}
	
	public boolean onItemMoveLongPressed(int position, TreeItem item) {
		if (position == 0) {
			//przeniesienie na koniec
			itemDraggingStopped();
			
			actionController.itemMoved(position, items.size() - 1);
			items = treeManager.getCurrentItem().getChildren();
			
			adapter.setDataSource(items);
			scrollToItem(items.size() - 1);
			return true;
		}
		if (position == items.size() - 1) {
			//przeniesienie na początek
			itemDraggingStopped();
			
			actionController.itemMoved(position, -(items.size() - 1));
			items = treeManager.getCurrentItem().getChildren();
			
			adapter.setDataSource(items);
			scrollToItem(0);
			return true;
		}
		return false;
	}
	
	private boolean handleItemGesture(float gestureX, float gestureY, Integer scrollOffset) {
		if (gestureStartPos != null && gestureStartX != null && gestureStartY != null) {
			if (gestureStartPos < items.size()) {
				float dx = gestureX - gestureStartX;
				float dy = gestureY - gestureStartY;
				float dscroll = scrollOffset - gestureStartScroll;
				dy -= dscroll;
				int itemH = getItemHeight(gestureStartPos);
				if (dx >= getWidth() * GESTURE_MIN_DX) { // warunek przesunięcia w prawo
					if (Math.abs(dy) <= itemH * GESTURE_MAX_DY) { //zachowanie braku przesunięcia w pionie
						//wejście wgłąb elementu smyraniem w prawo
						//Logs.debug("gesture: go into intercepted, dx: " + (dx / getWidth()) + " , dy: " + (Math.abs(dy) / itemH));
						TreeItem item = adapter.getItem(gestureStartPos);
						actionController.itemGoIntoClicked(gestureStartPos);
						gestureStartPos = null; //reset
						return true;
					}
				}
			}
		}
		return false;
	}
}
