package igrek.todotree.ui.treelist;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.intent.TreeCommand;

public class TreeListGestureHandler {
	
	private TreeListView listView;
	
	/** położenie X punktu rozpoczęcia gestu */
	private Float gestureStartX;
	/** położenie Y punktu rozpoczęcia gestu */
	private Float gestureStartY;
	/** położenie scrolla podczas rozpoczęcia gestu */
	private Integer gestureStartScroll;
	/** numer pozycji, na której rozpoczęto gest */
	private Integer gestureStartPos;
	
	private final float GESTURE_MIN_DX = 0.27f;
	private final float GESTURE_MAX_DY = 0.8f;
	
	public TreeListGestureHandler(TreeListView listView) {
		this.listView = listView;
	}
	
	public void gestureStart(float startX, float startY) {
		gestureStartX = startX;
		gestureStartY = startY;
		gestureStartScroll = listView.getScrollHandler().getScrollOffset();
	}
	
	public void gestureStartPos(Integer gestureStartPos) {
		this.gestureStartPos = gestureStartPos;
	}
	
	public boolean handleItemGesture(float gestureX, float gestureY, Integer scrollOffset) {
		if (!listView.getReorder().isDragging()) {
			if (gestureStartPos != null && gestureStartX != null && gestureStartY != null) {
				if (gestureStartPos < listView.getItems().size()) {
					float dx = gestureX - gestureStartX;
					float dy = gestureY - gestureStartY;
					float dscroll = scrollOffset - gestureStartScroll;
					dy -= dscroll;
					int itemH = listView.getItemHeight(gestureStartPos);
					if (Math.abs(dy) <= itemH * GESTURE_MAX_DY) { // no swiping vertically
						if (dx >= listView.getWidth() * GESTURE_MIN_DX) { // swipe right
							AbstractTreeItem item = listView.getAdapter().getItem(gestureStartPos);
							new TreeCommand().itemGoIntoClicked(gestureStartPos, item);
							gestureStartPos = null; //reset
							return true;
						} else if (dx <= -listView.getWidth() * GESTURE_MIN_DX) { // swipe left
							new TreeCommand().goBack();
							gestureStartPos = null; //reset
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void reset() {
		gestureStartX = null;
		gestureStartY = null;
		gestureStartScroll = null;
		gestureStartPos = null;
	}
}
