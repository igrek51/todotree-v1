package igrek.todotree.ui.treelist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import igrek.todotree.commands.ItemEditorCommand;
import igrek.todotree.commands.TreeCommand;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.ui.contextmenu.ItemActionsMenu;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

public class TreeListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	private Logs logger = new Logs();
	private TreeItemAdapter adapter;
	private TreeListScrollHandler scrollHandler;
	private TreeListReorder reorder = new TreeListReorder(this);
	private TreeListGestureHandler gestureHandler = new TreeListGestureHandler(this);
	private Context context;
	
	/** view index -> view height */
	private HashMap<Integer, Integer> itemHeights = new HashMap<>();
	
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
		this.context = context;
		
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
	
	public void setItems(List<AbstractTreeItem> items) {
		
		adapter = new TreeItemAdapter(context, null, this);
		setAdapter(adapter);
		adapter.setDataSource(items);
		
		invalidate();
		// TODO optional
		calculateViewHeights();
	}
	
	public List<AbstractTreeItem> getItems() {
		return adapter.getItems();
	}
	
	private void calculateViewHeights() {
		
		// FIXME invoked at least 2 times
		itemHeights = new HashMap<>();
		logger.debug("calculateViewHeights");
		
		final ViewTreeObserver observer = this.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
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
		if (h == null)
			logger.warn("Item View = null");
		return h != null ? h : 0;
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
		reorder.itemDraggingStopped();
		gestureHandler.reset();
		new ItemActionsMenu(position).show();
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
