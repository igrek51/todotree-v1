package igrek.todotree.ui.treelist

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.intent.TreeCommand
import kotlin.math.abs

class TreeListGestureHandler(private val listView: TreeListView) {

    private var gestureStartX: Float? = null
    private var gestureStartY: Float? = null
    private var gestureStartScroll: Int? = null

    private var gestureStartPos: Int? = null
    private val gestureMinDx = 0.27f
    private val gestureMaxDy = 0.8f

    fun gestureStart(startX: Float, startY: Float) {
        gestureStartX = startX
        gestureStartY = startY
        gestureStartScroll = listView.scrollHandler!!.scrollOffset
    }

    fun gestureStartPos(gestureStartPos: Int?) {
        this.gestureStartPos = gestureStartPos
    }

    fun handleItemGesture(gestureX: Float, gestureY: Float, scrollOffset: Int): Boolean {
        if (listView.reorder?.isDragging == false) {
            if (gestureStartPos != null && gestureStartX != null && gestureStartY != null) {
                if (gestureStartPos!! < listView.items!!.size) {
                    val dx = gestureX - gestureStartX!!
                    var dy = gestureY - gestureStartY!!
                    val dscroll = (scrollOffset - gestureStartScroll!!).toFloat()
                    dy -= dscroll
                    val itemH = listView.getItemHeight(gestureStartPos!!)
                    if (abs(dy) <= itemH * gestureMaxDy) { // no swiping vertically
                        if (dx >= listView.width * gestureMinDx) { // swipe right
                            val item: AbstractTreeItem = listView.adapter!!.getItem(
                                gestureStartPos!!
                            )
                            TreeCommand().itemGoIntoClicked(gestureStartPos!!, item)
                            gestureStartPos = null //reset
                            return true
                        } else if (dx <= -listView.width * gestureMinDx) { // swipe left
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