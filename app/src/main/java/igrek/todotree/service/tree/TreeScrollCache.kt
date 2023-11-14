package igrek.todotree.service.tree

import android.os.Handler
import android.os.Looper
import java.util.HashMap
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.ui.treelist.TreeListLayout
import igrek.todotree.util.mainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

// Single instance
class TreeScrollCache {

    private val treeManager: TreeManager by LazyExtractor(appFactory.treeManager)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    private val storedScrollPositions: HashMap<AbstractTreeItem, Int> = HashMap()
    private val logger: Logger = LoggerFactory.logger

    fun storeScrollPosition() {
        val item: AbstractTreeItem? = treeManager.currentItem
        val y: Int = treeListLayout.state.scrollState.value
        if (item != null) {
            storedScrollPositions[item] = y
        }
    }

    fun restoreScrollPosition() {
        val item: AbstractTreeItem = treeManager.currentItem ?: return
        val y = storedScrollPositions[item] ?: 0
        mainScope.launch {
            for (attempt in 1..8) {
                treeListLayout.scrollToPosition(y)
                delay(attempt * 25L) // 900 ms in total
                val diff = abs(treeListLayout.state.scrollState.value - y)
                if (diff < 5)
                    return@launch
            }
            logger.warn("Failed to restore scroll to $y px")
        }
    }

    fun getStoredScrollPosition(): Int {
        val item: AbstractTreeItem = treeManager.currentItem ?: return 0
        return storedScrollPositions[item] ?: 0
    }

    fun scrollToBottom() {
        mainScope.launch {
            treeListLayout.scrollToBottom()
            Handler(Looper.getMainLooper()).post {
                mainScope.launch {
                    treeListLayout.scrollToBottom()
                }
            }
        }
    }

    fun clear() {
        storedScrollPositions.clear()
    }
}