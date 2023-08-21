@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package igrek.todotree.ui.treelist

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.ItemsContainer
import igrek.todotree.compose.ReorderListView
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch

class TreeListLayout {
    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)
    private val treeManager: TreeManager by LazyExtractor(appFactory.treeManager)
    private val gui: GUI by LazyExtractor(appFactory.gui)
    private val treeSelectionManager: TreeSelectionManager by LazyExtractor(appFactory.treeSelectionManager)

    val state = TreeLayoutState()

    fun showLayout(layout: View) {
        updateItemsList()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }
    }

    fun updateItemsList() {
        val currentItem: AbstractTreeItem = treeManager.currentItem ?: return
        val selectedPositions: Set<Int>? = treeSelectionManager.selectedItems
        val items: MutableList<AbstractTreeItem> = currentItem.children

        val sb = StringBuilder(currentItem.displayName)
        if (!currentItem.isEmpty) {
            sb.append(" [")
            sb.append(currentItem.size())
            sb.append("]")
        }
        gui.setTitle(sb.toString())
        gui.showBackButton(currentItem.getParent() != null)

        state.visibleItems.replaceAll(items)

        state.selectMode.value = selectedPositions?.isNotEmpty() == true
        state.selectedPositions.value = selectedPositions
    }

    fun onItemClick(position: Int, item: AbstractTreeItem) {
        try {
            if (position == state.visibleItems.items.size) { // plus - new
                ItemEditorCommand().addItemClicked()
            } else { // existing one
                TreeCommand().itemClicked(position, item)
            }
        } catch (t: Throwable) {
            UiErrorHandler.handleError(t)
        }
    }

    fun onItemLongClick(position: Int, item: AbstractTreeItem): Boolean {
//        if (reorder?.isDragging == false) {
//            reorder?.itemDraggingStopped()
//            gestureHandler.reset()
//            ItemActionsMenu(position).show(view)
//        }
        return true
    }

    fun onItemsReordered(newItems: MutableList<AbstractTreeItem>) {
        val mNewItems: MutableList<AbstractTreeItem> = newItems.toMutableList()
        val currentItem: AbstractTreeItem = treeManager.currentItem ?: return
        currentItem.children = mNewItems
        appFactory.changesHistory.get().registerChange()
    }

}


class TreeLayoutState {
    val scrollState: ScrollState = ScrollState(0)
    val visibleItems: ItemsContainer<AbstractTreeItem> = ItemsContainer()
    val selectMode: MutableState<Boolean> = mutableStateOf(false)
    val selectedPositions: MutableState<Set<Int>?> = mutableStateOf(null)
}


@Composable
private fun MainComponent(controller: TreeListLayout) {
    Column {
        ReorderListView(
            itemsContainer = controller.state.visibleItems,
            scrollState = controller.state.scrollState,
            onReorder = { newItems ->
                controller.onItemsReordered(newItems)
            },
        ) { itemsContainer: ItemsContainer<AbstractTreeItem>, index: Int, modifier: Modifier, reorderButtonModifier: Modifier ->
            TreeItemComposable(controller, itemsContainer, index, modifier, reorderButtonModifier)
        }
    }
}


@SuppressLint("ModifierParameter")
@Composable
private fun TreeItemComposable(
    controller: TreeListLayout,
    itemsContainer: ItemsContainer<AbstractTreeItem>,
    index: Int,
    modifier: Modifier,
    reorderButtonModifier: Modifier,
) {
    val item: AbstractTreeItem = itemsContainer.items.getOrNull(index) ?: return

    Row(
        modifier.padding(0.dp)
            .combinedClickable(
                onClick = {
                    mainScope.launch {
                        controller.onItemClick(index, item)
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        controller.onItemLongClick(index, item)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        IconButton(
            modifier = reorderButtonModifier
                .padding(1.dp).size(24.dp),
            onClick = {},
        ) {
            Icon(
                painterResource(id = R.drawable.reorder),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            text = item.displayName,
            fontWeight = FontWeight.Normal,
        )

//        IconButton(
//            onClick = {
//                mainScope.launch {
//                    controller.onPlaylistMore(playlist)
//                }
//            },
//        ) {
//            Icon(
//                painterResource(id = R.drawable.more),
//                contentDescription = null,
//                modifier = Modifier.size(24.dp),
//                tint = Color.White,
//            )
//        }
    }
}
