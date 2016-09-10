package igrek.todotree.gui.treelist;

import android.view.MotionEvent;
import android.view.View;

public class TreeItemTouchListener implements View.OnTouchListener {

    private TreeListView listView;
    private int position;

    public TreeItemTouchListener(TreeListView listView, int position) {
        this.listView = listView;
        this.position = position;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                listView.onItemTouchDown(position, event, v);
                break;
            //case MotionEvent.ACTION_MOVE:
            //    Logs.debug("Item: ACTION_MOVE");
            //    break;
            //case MotionEvent.ACTION_UP:
            //    Logs.debug("Item: ACTION_UP");
            //case MotionEvent.ACTION_CANCEL:
            //    Logs.debug("Item: ACTION_CANCEL");
            //    break;
        }
        return false;
    }
}
