package igrek.todotree.ui.treelist

import igrek.todotree.ui.treelist.TreeItemAdapter.getItem
import igrek.todotree.intent.TreeCommand.itemGoIntoClicked
import igrek.todotree.intent.TreeCommand.goBack
import igrek.todotree.ui.treelist.TreeListView
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.intent.TreeCommand

class TreeListGestureHandler(private val listView: TreeListView) {
    /** położenie X punktu rozpoczęcia gestu  */
    private var gestureStartX: Float? = null

    /** położenie Y punktu rozpoczęcia gestu  */
    private var gestureStartY: Float? = null

    /** położenie scrolla podczas rozpoczęcia gestu  */
    private var gestureStartScroll: Int? = null

    /** numer pozycji, na której rozpoczęto gest  */
    private var gestureStartPos: Int? = null
    private val GESTURE_MIN_DX = 0.27f
    private val GESTURE_MAX_DY = 0.8f
    fun gestureStart(startX: Float, startY: Float) {
        gestureStartX = startX
        gestureStartY = startY
        gestureStartScroll = listView.scrollHandler.scrollOffset
    }

    fun gestureStartPos(gestureStartPos: Int?) {
        this.gestureStartPos = gestureStartPos
    }

    fun handleItemGesture(gestureX: Float, gestureY: Float, scrollOffset: Int): Boolean {
        if (!listView.reorder.isDragging) {
            if (gestureStartPos != null && gestureStartX != null && gestureStartY != null) {
                if (gestureStartPos!! < listView.items.size) {
                    val dx = gestureX - gestureStartX!!
                    var dy = gestureY - gestureStartY!!
                    val dscroll = (scrollOffset - gestureStartScroll!!).toFloat()
                    dy -= dscroll
                    val itemH = listView.getItemHeight(gestureStartPos!!)
                    if (Math.abs(dy) <= itemH * GESTURE_MAX_DY) { // no swiping vertically
                        if (dx >= listView.width * GESTURE_MIN_DX) { // swipe right
                            val item: AbstractTreeItem? = listView.adapter.getItem(
                                gestureStartPos!!
                            )
                            TreeCommand().itemGoIntoClicked(gestureStartPos!!, item)
                            gestureStartPos = null //reset
                            return true
                        } else if (dx <= -listView.width * GESTURE_MIN_DX) { // swipe left
                            TreeCommand().goBack()
                            gestureStartPos = null //reset
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun reset() {
        gestureStartX = null
        gestureStartY = null
        gestureStartScroll = null
        gestureStartPos = null
    }
}