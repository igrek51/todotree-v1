package igrek.todotree.ui.treelist;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.AbsListView;

import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;

public class TreeListScrollHandler implements AbsListView.OnScrollListener {
	
	private TreeListView listView;
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	/** aktualne położenie scrolla */
	private Integer scrollOffset = 0;
	
	private int scrollState = SCROLL_STATE_IDLE;
	
	private final int SMOOTH_SCROLL_EDGE_DP = 200;
	private int SMOOTH_SCROLL_EDGE_PX;
	private final float SMOOTH_SCROLL_FACTOR = 0.34f;
	private final int SMOOTH_SCROLL_DURATION = 10;
	
	public TreeListScrollHandler(TreeListView listView, Context context) {
		this.listView = listView;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		SMOOTH_SCROLL_EDGE_PX = (int) (SMOOTH_SCROLL_EDGE_DP / metrics.density);
	}
	
	public boolean handleScrolling() {
		if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			int offset = listView.computeVerticalScrollOffset();
			int height = listView.getHeight();
			int extent = listView.computeVerticalScrollExtent();
			int range = listView.computeVerticalScrollRange();
			
			if (listView.getReorder().isDragging() && listView.getReorder()
					.getHoverBitmapBounds() != null) {
				int hoverViewTop = listView.getReorder().getHoverBitmapBounds().top;
				int hoverHeight = listView.getReorder().getHoverBitmapBounds().height();
				
				if (hoverViewTop <= SMOOTH_SCROLL_EDGE_PX && offset > 0) {
					int scrollDistance = (int) ((hoverViewTop - SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
					listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
					return true;
				}
				if (hoverViewTop + hoverHeight >= height - SMOOTH_SCROLL_EDGE_PX && (offset + extent) < range) {
					int scrollDistance = (int) ((hoverViewTop + hoverHeight - height + SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
					listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
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
			if (listView.getReorder().isDragging()) {
				boolean scrollingResult = handleScrolling();
				// nie da się scrollować - dojechano do końca zakresu
				if (!scrollingResult) {
					listView.getReorder()
							.handleItemDragging(); // sprawdzenie, czy elementy powinny zostać przemieszczone
				}
			}
		}
	}
	
	private int getRealScrollPosition() {
		if (listView.getChildAt(0) == null)
			return 0;
		
		int sumh = 0;
		for (int i = 0; i < listView.getFirstVisiblePosition(); i++) {
			sumh += listView.getItemHeight(i);
		}
		//separatory
		//sumh += getFirstVisiblePosition() * getDividerHeight();
		return sumh - listView.getChildAt(0).getTop();
	}
	
	/**
	 * @param itemIndex pozycja elementu do przescrollowania (-1 - ostatni element)
	 */
	public void scrollToItem(int itemIndex) {
		if (itemIndex == -1)
			itemIndex = listView.getItems().size() - 1;
		if (itemIndex < 0)
			itemIndex = 0;
		listView.setSelection(itemIndex);
		listView.invalidate();
	}
	
	public void scrollToPosition(int y) {
		//wyznaczenie najbliższego elementu i przesunięcie względem niego
		try {
			int position = 0;
			while (y > listView.getItemHeight(position)) {
				int itemHeight = listView.getItemHeight(position);
				if (itemHeight == 0) {
					throw new RuntimeException("item height = 0, cant scroll to position");
				}
				y -= itemHeight;
				position++;
			}
			
			listView.setSelection(position);
			listView.smoothScrollBy(y, 50);
		} catch (RuntimeException e) {
			final int move = y;
			new Handler().post(() -> {
				listView.smoothScrollBy(move, 50);
			});
			logger.warn(e.getMessage());
		}
		
		listView.invalidate();
	}
	
	public void scrollToBottom() {
		listView.setSelection(listView.getItems().size());
	}
	
	public Integer getCurrentScrollPosition() {
		return getRealScrollPosition();
	}
	
	public Integer getScrollOffset() {
		return scrollOffset;
	}
}
