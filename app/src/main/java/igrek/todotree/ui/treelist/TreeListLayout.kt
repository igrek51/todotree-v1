@file:OptIn(ExperimentalFoundationApi::class)

package igrek.todotree.ui.treelist

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.ItemsContainer
import igrek.todotree.compose.ReorderListView
import igrek.todotree.compose.colorLinkItem
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.info.splitTime
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

open class TreeListLayout {
    private val treeManager: TreeManager by LazyExtractor(appFactory.treeManager)
    private val gui: GUI by LazyExtractor(appFactory.gui)
    private val treeSelectionManager: TreeSelectionManager by LazyExtractor(appFactory.treeSelectionManager)

    val state = LayoutState()

    class LayoutState {
        val scrollState: ScrollState = ScrollState(0)
        val visibleItems: ItemsContainer<AbstractTreeItem> = ItemsContainer()
        val selectMode: MutableState<Boolean> = mutableStateOf(false)
        val selectedPositions: MutableState<Set<Int>?> = mutableStateOf(null)
    }

    fun showLayout(layout: View) {
        updateItemsList()

        splitTime.split("show layout")
        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view_tree).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }
    }

    open fun updateItemsList() {
        gui.startLoading()

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

        updateFocusedItem(items)
    }

    private fun updateFocusedItem(items: MutableList<AbstractTreeItem>) {
        fun itemToBeFocused(item: AbstractTreeItem): Boolean {
            return when {
                item == treeManager.focusItem -> true
                item is LinkTreeItem && item.target == treeManager.focusItem -> true
                else -> false
            }
        }
        items.forEachIndexed { index, item ->
            if (itemToBeFocused(item)){
                state.visibleItems.highlightedIndex.value = index
                return
            }
        }
        state.visibleItems.highlightedIndex.value = -1
    }

    fun updateOneListItem(position: Int) {
        state.visibleItems.notifyItemChange(position)

        val selectedPositions: Set<Int>? = treeSelectionManager.selectedItems
        state.selectMode.value = selectedPositions?.isNotEmpty() == true
        state.selectedPositions.value = selectedPositions
    }

    fun onItemClick(position: Int, item: AbstractTreeItem) {
        TreeCommand().itemClicked(position, item)
    }

    fun onItemLongClick(position: Int, coordinates: SizeAndPosition) {
        ItemActionsMenu(position).show(coordinates)
    }

    fun onEnterItemClick(position: Int, item: AbstractTreeItem) {
        TreeCommand().itemGoIntoClicked(position, item)
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

    fun onAddItemAboveClick(position: Int) {
        ItemEditorCommand().addItemHereClicked(position)
    }

    fun onEditItemClick(item: AbstractTreeItem) {
        ItemEditorCommand().itemEditClicked(item)
    }

    fun onSelectItemClick(position: Int, checked: Boolean) {
        ItemSelectionCommand().selectedItemClicked(position, checked)
    }

    suspend fun scrollToPosition(y: Int) {
        state.scrollState.scrollTo(y)
    }

    suspend fun scrollToBottom() {
        state.scrollState.scrollBy(999999f)
    }

    fun startLoading() {
        gui.startLoading()
    }

    fun stopLoading() {
        gui.stopLoading()
    }
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
            onLoad = {
                controller.stopLoading()
                val absPath = appFactory.treeManager.get().currentItem?.absolutePath().orEmpty()
                splitTime.split("composition done: $absPath")
            },
            itemContent = { itemsContainer: ItemsContainer<AbstractTreeItem>, id: Int, modifier: Modifier ->
                TreeItemComposable(controller, itemsContainer, id, modifier)
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
    id: Int,
    modifier: Modifier,
) {
//    logger.debug("recompose item id: $id")
    val item: AbstractTreeItem = itemsContainer.items.getOrNull(id) ?: return
    val reorderButtonModifier: Modifier = itemsContainer.reorderButtonModifiers.getValue(id)

    val itemPosition: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    val itemSize: MutableState<IntSize> = remember { mutableStateOf(IntSize.Zero) }

    val selectMode: Boolean = controller.state.selectMode.value

    val linkStrokeWidthPx = with(LocalDensity.current) { 1.dp.toPx() }
    val linkOffset2Sp = with(LocalDensity.current) { 2.sp.toPx() }

    Row(
        modifier
            .onGloballyPositioned { coordinates ->
                itemPosition.value = coordinates.positionInRoot()
                itemSize.value = coordinates.size
            }
            .combinedClickable(
                onClick = {
                    splitTime.split("item click")
                    if (!selectMode)
                        controller.startLoading()
                    val position = itemsContainer.indexToPositionMap.getValue(id)
                    Handler(Looper.getMainLooper()).post {
                        mainScope.launch {
                            delay(1)
                            controller.onItemClick(position, item)
                        }
                    }
                },
                onLongClick = {
                    val position = itemsContainer.indexToPositionMap.getValue(id)
                    mainScope.launch {
                        val coordinates = SizeAndPosition(
                            x = itemPosition.value.x.toInt(),
                            y = itemPosition.value.y.toInt(),
                            w = itemSize.value.width,
                            h = itemSize.value.height,
                        )
                        controller.onItemLongClick(position, coordinates)
                    }
                },
            )
            .pointerInput(id) {
                detectMyTransformGestures { pan ->
                    val itemW = itemsContainer.parentViewportWidth.value
                    val itemH = itemsContainer.itemHeights.getValue(id)
                    val position = itemsContainer.indexToPositionMap.getValue(id)
                    val result = handleItemGesture(pan.x, pan.y, itemW, itemH, position, item)
                    result
                }
            }
        ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Reorder
        if (!selectMode) {
            IconButton(
                modifier = reorderButtonModifier
                    .size(34.dp, 36.dp),
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
            val position = itemsContainer.indexToPositionMap.getValue(id)
            val isSelected = positionsSet.contains(position)
            Checkbox(
                modifier = Modifier.size(36.dp),
                checked = isSelected,
                onCheckedChange = { checked ->
                    val actualPosition = itemsContainer.indexToPositionMap.getValue(id)
                    controller.onSelectItemClick(actualPosition, checked)
                }
            )
        }

        val fontWeight: FontWeight = when {
            item is LinkTreeItem -> FontWeight.Normal
            item.isEmpty -> FontWeight.Normal
            else -> FontWeight.Bold
        }

        if (item is LinkTreeItem) {
            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .drawBehind {
                        val verticalOffset = size.height - linkOffset2Sp
                        drawLine(
                            color = colorLinkItem,
                            strokeWidth = linkStrokeWidthPx,
                            start = Offset(0f, verticalOffset),
                            end = Offset(size.width, verticalOffset)
                        )
                    },
                color = colorLinkItem,
                text = item.displayName,
                fontSize = 16.sp,
                fontWeight = fontWeight,
            )
            Spacer(modifier = Modifier.weight(1.0f))

        } else {
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                text = item.displayName,
                fontSize = 16.sp,
                fontWeight = fontWeight,
            )
        }

        if (!selectMode) {
            if (item.isEmpty) { // leaf
                // Enter item
                ItemIconButton(R.drawable.arrow_forward) {
                    val position = itemsContainer.indexToPositionMap.getValue(id)
                    controller.onEnterItemClick(position, item)
                }
            } else { // parent
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "[${item.size()}]",
                    fontSize = 16.sp,
                )
                // Edit item
                ItemIconButton(R.drawable.edit) {
                    controller.onEditItemClick(item)
                }
            }
            // Add new above
            ItemIconButton(R.drawable.plus) {
                val position = itemsContainer.indexToPositionMap.getValue(id)
                controller.onAddItemAboveClick(position)
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
                    controller.startLoading()
                    Handler(Looper.getMainLooper()).post {
                        mainScope.launch {
                            controller.onPlusClick()
                        }
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
        modifier = Modifier.size(32.dp, 36.dp),
        onClick = {
            Handler(Looper.getMainLooper()).post {
                mainScope.launch {
                    onClick()
                }
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

private const val GESTURE_MIN_DX = 0.27f
private const val GESTURE_MAX_DY = 0.8f

fun handleItemGesture(dx: Float, dy: Float, itemWidth: Float, itemHeight: Float, position: Int, item: AbstractTreeItem): Boolean? {
    if (abs(dy) > itemHeight * GESTURE_MAX_DY) { // no swiping vertically
        return false // consumed and breaked
    }
    if (dx >= itemWidth * GESTURE_MIN_DX) { // swipe right
        mainScope.launch {
            TreeCommand().itemGoIntoClicked(position, item)
        }
        return true
    } else if (dx <= -itemWidth * GESTURE_MIN_DX) { // swipe left
        mainScope.launch {
            TreeCommand().goBack()
        }
        return true
    }
    return null
}

suspend fun PointerInputScope.detectMyTransformGestures(
    onGesture: (pan: Offset) -> Boolean?,
) {
    awaitEachGesture {
        var pan = Offset.Zero
        val touchSlop = viewConfiguration.touchSlop

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val panChange = event.calculatePan()

                pan += panChange
                val panMotion = pan.getDistance()
                if (panMotion > touchSlop) {
                    if (panChange != Offset.Zero) {
                        val consumed = onGesture(pan)
                        if (consumed == true) {
                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    it.consume()
                                }
                            }
                            break
                        } else if (consumed == false) {
                            break
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
}