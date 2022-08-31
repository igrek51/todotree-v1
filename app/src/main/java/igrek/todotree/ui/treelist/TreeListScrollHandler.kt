package igrek.todotree.ui.treelist

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.AbsListView
import igrek.todotree.info.logger.LoggerFactory

class TreeListScrollHandler(private val listView: TreeListView, context: Context) :
    AbsListView.OnScrollListener {
    private val logger = LoggerFactory.logger

    var scrollOffset = 0
        private set

    private var scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE
    private val smoothScrollEdgePx: Int

    val currentScrollPosition: Int
        get() = realScrollPosition

    init {
        val metrics = context.resources.displayMetrics
        smoothScrollEdgePx = (SMOOTH_SCROLL_EDGE_DP / metrics.density).toInt()
    }

    companion object {
        private const val SMOOTH_SCROLL_EDGE_DP = 200
        private const val SMOOTH_SCROLL_FACTOR = 0.34f
        private const val SMOOTH_SCROLL_DURATION = 10
    }

    fun handleScrolling(): Boolean {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            val offset = listView.computeVerticalScrollOffset()
            val height = listView.height
            val extent = listView.computeVerticalScrollExtent()
            val range = listView.computeVerticalScrollRange()
            if (listView.reorder!!.isDragging && listView.reorder.hoverBitmapBounds != null
            ) {
                val hoverViewTop = listView.reorder.hoverBitmapBounds!!.top
                val hoverHeight = listView.reorder.hoverBitmapBounds!!.height()
                if (hoverViewTop <= smoothScrollEdgePx && offset > 0) {
                    val scrollDistance =
                        ((hoverViewTop - smoothScrollEdgePx) * SMOOTH_SCROLL_FACTOR).toInt()
                    listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION)
                    return true
                }
                if (hoverViewTop + hoverHeight >= height - smoothScrollEdgePx && offset + extent < range) {
                    val scrollDistance =
                        ((hoverViewTop + hoverHeight - height + smoothScrollEdgePx) * SMOOTH_SCROLL_FACTOR).toInt()
                    listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION)
                    return true
                }
            }
        }
        return false
    }

    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
        scrollOffset = realScrollPosition
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (listView.reorder?.isDragging == true) {
                val scrollingResult = handleScrolling()
                if (!scrollingResult) {
                    listView.reorder.handleItemDragging()
                }
            }
        }
    }

    private val realScrollPosition: Int
        get() {
            if (listView.getChildAt(0) == null) return 0
            var sumh = 0
            for (i in 0 until listView.firstVisiblePosition) {
                sumh += listView.getItemHeight(i)
            }
            return sumh - listView.getChildAt(0).top
        }

    fun scrollToItem(_itemIndex: Int) {
        var itemIndex = _itemIndex
        if (itemIndex == -1) itemIndex = listView.items!!.size - 1
        if (itemIndex < 0) itemIndex = 0
        listView.setSelection(itemIndex)
        listView.invalidate()
    }

    fun scrollToPosition(_y: Int) {
        var y = _y
        try {
            var position = 0
            while (y > listView.getItemHeight(position)) {
                val itemHeight = listView.getItemHeight(position)
                if (itemHeight == 0) {
                    throw RuntimeException("item height = 0, cant scroll to position")
                }
                y -= itemHeight
                position++
            }
            listView.setSelection(position)
            listView.smoothScrollBy(y, 50)
        } catch (e: RuntimeException) {
            val move = y
            Handler(Looper.getMainLooper()).post { listView.smoothScrollBy(move, 50) }
            logger.warn(e.message)
        }
        listView.invalidate()
    }

    fun scrollToBottom() {
        listView.setSelection(listView.items!!.size)
    }
}