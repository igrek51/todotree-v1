package igrek.todotree.ui.tree;

import android.view.MotionEvent;
import android.view.View;

class TreeItemTouchListener implements View.OnTouchListener {
	
	private TreeListView listView;
	private int position;
	
	TreeItemTouchListener(TreeListView listView, int position) {
		this.listView = listView;
		this.position = position;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				listView.onItemTouchDown(position, event, v);
				break;
		}
		return false;
	}
}
