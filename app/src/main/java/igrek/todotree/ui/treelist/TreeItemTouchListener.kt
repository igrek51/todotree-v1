package igrek.todotree.ui.treelist

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

internal class TreeItemTouchListener(
    private val listView: TreeListView,
    private val position: Int
) : OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> listView.onItemTouchDown(
                position, event, v
            )
        }
        return false
    }
}