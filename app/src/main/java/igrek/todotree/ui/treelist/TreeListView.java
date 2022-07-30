package igrek.todotree.ui.treelist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;
import java.util.Set;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;
import igrek.todotree.intent.ItemEditorCommand;
import igrek.todotree.intent.TreeCommand;
import igrek.todotree.ui.contextmenu.ItemActionsMenu;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

public class TreeListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	private TreeItemAdapter adapter;
	private TreeListScrollHandler scrollHandler;
	private TreeListReorder reorder = new TreeListReorder(this);
	private TreeListGestureHandler gestureHandler = new TreeListGestureHandler(this);
	
	/** view index -> view height */
	private SparseArray<Integer> itemHeights = new SparseArray<>();
	
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
		adapter = new TreeItemAdapter(context, null, this);
		scrollHandler = new TreeListScrollHandler(this, context);
		
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		setOnScrollListener(scrollHandler);
		setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setAdapter(adapter);
	}
	
	@Override
	public TreeItemAdapter getAdapter() {
		return adapter;
	}
	
	public TreeListReorder getReorder() {
		return reorder;
	}
	
	public TreeListScrollHandler getScrollHandler() {
		return scrollHandler;
	}
	
	public TreeListGestureHandler getGestureHandler() {
		return gestureHandler;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getSource() == 777) { // from moveButton
			if (ev.getAction() == MotionEvent.ACTION_MOVE)
				return true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				gestureHandler.gestureStart(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				if (reorder.isDragging()) {
					reorder.setLastTouchY(event.getY());
					reorder.handleItemDragging();
					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (gestureHandler.handleItemGesture(event.getX(), event.getY(), scrollHandler.getScrollOffset()))
					return super.onTouchEvent(event);
			case MotionEvent.ACTION_CANCEL:
				reorder.itemDraggingStopped();
				gestureHandler.reset();
				break;
		}
		return super.onTouchEvent(event);
	}
	
	public void onItemTouchDown(int position, MotionEvent event, View v) {
		gestureHandler.gestureStartPos(position);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (reorder != null && reorder.isDragging()) {
			reorder.setDraggedItemView();
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		reorder.dispatchDraw(canvas);
	}
	
	public void setItemsAndSelected(List<AbstractTreeItem> items, Set<Integer> selectedPositions) {
		adapter.setSelections(selectedPositions);
		setItems(items);
	}
	
	private void setItems(List<AbstractTreeItem> items) {
		adapter.setDataSource(items);
		invalidate();
		calculateViewHeights();
	}
	
	public List<AbstractTreeItem> getItems() {
		return adapter.getItems();
	}
	
	private void calculateViewHeights() {
		// WARNING: for a moment - there's invalidated item heights map
		final ViewTreeObserver observer = this.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				
				itemHeights.clear();
				TreeListView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// now view width should be available at last
				int viewWidth = TreeListView.this.getWidth();
				if (viewWidth == 0)
					logger.warn("List view width == 0");
				
				int measureSpecW = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
				for (int i = 0; i < adapter.getCount(); i++) {
					View itemView = adapter.getView(i, null, TreeListView.this);
					itemView.measure(measureSpecW, MeasureSpec.UNSPECIFIED);
					itemHeights.put(i, itemView.getMeasuredHeight());
				}
				
			}
		});
	}
	
	public int getItemHeight(int position) {
		Integer h = itemHeights.get(position);
		if (h == null) {
			logger.warn("Item View (" + position + ") = null");
			return 0;
		}
		
		return h;
	}
	
	public void putItemHeight(Integer position, Integer height) {
		itemHeights.put(position, height);
	}
	
	public View getItemView(int position) {
		return adapter.getStoredView(position);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		try {
			if (position == adapter.getItems().size()) {
				//nowy element
				new ItemEditorCommand().addItemClicked();
			} else {
				//istniejący element
				AbstractTreeItem item = adapter.getItem(position);
				new TreeCommand().itemClicked(position, item);
			}
		} catch (Throwable t) {
			UIErrorHandler.showError(t);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (!reorder.isDragging()) {
			reorder.itemDraggingStopped();
			gestureHandler.reset();
			new ItemActionsMenu(position).show(view);
		}
		return true;
	}
	
	@Override
	public int computeVerticalScrollOffset() {
		return super.computeVerticalScrollOffset();
	}
	
	@Override
	public int computeVerticalScrollExtent() {
		return super.computeVerticalScrollExtent();
	}
	
	@Override
	public int computeVerticalScrollRange() {
		return super.computeVerticalScrollRange();
	}
	
	public Integer getCurrentScrollPosition() {
		return scrollHandler.getCurrentScrollPosition();
	}
	
	public void scrollToBottom() {
		scrollHandler.scrollToBottom();
	}
	
	public void scrollToPosition(int y) {
		scrollHandler.scrollToPosition(y);
	}
	
	public void scrollToItem(int itemIndex) {
		scrollHandler.scrollToItem(itemIndex);
	}
}
