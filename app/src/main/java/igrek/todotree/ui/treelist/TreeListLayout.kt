@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package igrek.todotree.ui.treelist

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.ItemsContainer
import igrek.todotree.compose.ReorderListView
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.ItemSelectionCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import igrek.todotree.ui.SizeAndPosition
import igrek.todotree.ui.contextmenu.ItemActionsMenu
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch

class TreeListLayout {
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

    fun onItemClick(index: Int, item: AbstractTreeItem) {
        TreeCommand().itemClicked(index, item)
    }

    fun onItemLongClick(index: Int, coordinates: SizeAndPosition) {
        ItemActionsMenu(index).show(coordinates)
    }

    fun onEnterItemClick(index: Int, item: AbstractTreeItem) {
        TreeCommand().itemGoIntoClicked(index, item)
    }

    fun onPlusClick() {
        ItemEditorCommand().addItemClicked()
    }

    fun onPlusLongClick() {
        val index = state.visibleItems.items.size
        ItemActionsMenu(index).show(null)
    }

    fun onItemsReordered(newItems: MutableList<AbstractTreeItem>) {
        val mNewItems: MutableList<AbstractTreeItem> = newItems.toMutableList()
        val currentItem: AbstractTreeItem = treeManager.currentItem ?: return
        currentItem.children = mNewItems
        appFactory.changesHistory.get().registerChange()
    }

    fun onAddItemAboveClick(index: Int) {
        ItemEditorCommand().addItemHereClicked(index)
    }

    fun onEditItemClick(item: AbstractTreeItem) {
        ItemEditorCommand().itemEditClicked(item)
    }

    fun onSelectItemClick(index: Int, checked: Boolean) {
        ItemSelectionCommand().selectedItemClicked(index, checked)
    }

    suspend fun scrollToPosition(y: Int) {
        state.scrollState.scrollTo(y)
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
            itemContent = { itemsContainer: ItemsContainer<AbstractTreeItem>, index: Int, modifier: Modifier, reorderButtonModifier: Modifier ->
                TreeItemComposable(controller, itemsContainer, index, modifier, reorderButtonModifier)
            },
            postContent = {
                PlusButtonComposable(controller)
            },
        )
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

    val itemPosition: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    val itemSize: MutableState<IntSize> = remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier
            .onGloballyPositioned { coordinates ->
                itemPosition.value = coordinates.positionInRoot()
                itemSize.value = coordinates.size
            }
            .padding(0.dp)
            .combinedClickable(
                onClick = {
                    mainScope.launch {
                        controller.onItemClick(index, item)
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        val coordinates = SizeAndPosition(
                            x = itemPosition.value.x.toInt(),
                            y = itemPosition.value.y.toInt(),
                            w = itemSize.value.width,
                            h = itemSize.value.height,
                        )
                        controller.onItemLongClick(index, coordinates)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val selectMode: Boolean = controller.state.selectMode.value

        // Reorder
        if (!selectMode) {
            IconButton(
                modifier = reorderButtonModifier
                    .width(28.dp).fillMaxHeight(),
                onClick = {},
            ) {
                Icon(
                    painterResource(id = R.drawable.reorder),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White,
                )
            }
        }

        // Select
        if (selectMode) {
            val positionsSet: Set<Int> = controller.state.selectedPositions.value ?: emptySet()
            val isSelected = positionsSet.contains(index)
            Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                    controller.onSelectItemClick(index, checked)
                }
            )
        }

        // TODO gesture handling

        val fontWeight: FontWeight = when {
            item is LinkTreeItem -> FontWeight.Normal
            item.isEmpty -> FontWeight.Normal
            else -> FontWeight.Bold
        }
        val textDecoration: TextDecoration? = when (item) {
            is LinkTreeItem -> TextDecoration.Underline
            else -> null
        }

        Text(
            modifier = Modifier.weight(1f).padding(vertical = 2.dp, horizontal = 4.dp),
            text = item.displayName,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
        )

        if (!selectMode) {
            if (item.isEmpty) { // leaf
                // Enter item
                ItemIconButton(R.drawable.arrow_forward) {
                    controller.onEnterItemClick(index, item)
                }
            } else { // parent
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "[${item.size()}]",
                )
                // Edit item
                ItemIconButton(R.drawable.edit) {
                    controller.onEditItemClick(item)
                }
            }
            // Add new above
            ItemIconButton(R.drawable.plus) {
                controller.onAddItemAboveClick(index)
            }
        }

    }
}

@Composable
private fun PlusButtonComposable(
    controller: TreeListLayout,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFF444444)))
            .combinedClickable(
                onClick = {
                    mainScope.launch {
                        controller.onPlusClick()
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        controller.onPlusLongClick()
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(id = R.drawable.plus),
            contentDescription = null,
            modifier = Modifier.padding(12.dp).size(24.dp),
            tint = Color.White,
        )
    }
}

@Composable
private fun ItemIconButton(
    @DrawableRes drawableId: Int,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.width(32.dp).fillMaxHeight(),
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
    ) {
        Icon(
            painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White,
        )
    }
}