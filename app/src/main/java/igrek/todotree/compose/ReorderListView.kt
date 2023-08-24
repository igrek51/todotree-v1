package igrek.todotree.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import igrek.todotree.info.logger.LoggerFactory.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


class ItemsContainer<T>(
    var items: MutableList<T> = mutableListOf(),
    val modifiedMap: MutableMap<Int, MutableState<Long>> = mutableMapOf(),
    val modifiedAll: MutableState<Long> = mutableStateOf(0),
    val itemHeights: MutableMap<Int, Float> = mutableMapOf(),
    val itemAnimatedOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(),
    val itemStablePositions: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(),
    val reorderButtonModifiers: MutableMap<Int, Modifier> = mutableMapOf(),
    val isDraggingMes: MutableMap<Int, State<Boolean>> = mutableMapOf(),
    val indexToPositionMap: MutableMap<Int, Int> = mutableMapOf(), // item index (ID) on list to real displayed position index
    val positionToIndexMap: MutableMap<Int, Int> = mutableMapOf(), // real displayed index to item index (ID) on list
    var dividerPx: Float = 0f,
) {
    fun replaceAll(newList: MutableList<T>) {
        items = newList
        items.indices.forEach { index: Int ->
            if (!modifiedMap.containsKey(index)) {
                modifiedMap[index] = mutableStateOf(0)
            }
        }
        modifiedAll.value += 1
    }

    fun notifyItemChange(index: Int) {
        modifiedMap.getValue(index).value += 1
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun <T> ReorderListView(
    itemsContainer: ItemsContainer<T>,
    scrollState: ScrollState = rememberScrollState(),
    onReorder: (newItems: MutableList<T>) -> Unit,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier) -> Unit,
    postContent: @Composable () -> Unit,
) {
    val draggingIndex: MutableState<Int> = remember { mutableStateOf(-1) }
    val scrollDiff: MutableState<Float> = remember { mutableStateOf(0f) }
    val parentViewportHeight: MutableState<Float> = remember { mutableStateOf(0f) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val scrollJob: MutableState<Job?> = remember { mutableStateOf(null) }

//    val isDragging: State<Boolean> = derivedStateOf {
//        draggingIndex.value != -1
//    }

    itemsContainer.dividerPx = with(LocalDensity.current) { 1.dp.toPx() }

    itemsContainer.items.indices.forEach { index: Int ->
        if (!itemsContainer.reorderButtonModifiers.containsKey(index)) {
            itemsContainer.reorderButtonModifiers[index] = Modifier.createReorderButtonModifier(
                itemsContainer,
                index,
                draggingIndex,
                scrollState,
                scrollDiff,
                parentViewportHeight,
                coroutineScope,
                scrollJob,
                onReorder,
            )
        }

        itemsContainer.indexToPositionMap[index] = index
        itemsContainer.positionToIndexMap[index] = index

        itemsContainer.itemAnimatedOffsets[index] = Animatable(0f)
        itemsContainer.itemStablePositions[index] = Animatable(0f)

        itemsContainer.isDraggingMes[index] = derivedStateOf {
            draggingIndex.value == index
        }
    }

    key(itemsContainer.modifiedAll.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    parentViewportHeight.value =
                        coordinates.parentLayoutCoordinates?.size?.height?.toFloat() ?: 0f
                },
        ) {
            ReorderListColumn(
                itemsContainer,
                scrollDiff,
                itemContent,
            )

            postContent()
        }
    }
}


@Composable
fun <T> ReorderListColumn(
    itemsContainer: ItemsContainer<T>,
    scrollDiff: State<Float>,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier) -> Unit,
) {
    logger.debug("recomposing all items")
    itemsContainer.items.indices.forEach { index: Int ->
        ReorderListViewItem(
            itemsContainer, index,
            scrollDiff, itemContent,
        )
    }
}

@SuppressLint("ModifierParameter")
@Composable
private fun <T> ReorderListViewItem(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    scrollDiff: State<Float>,
    itemContent: @Composable (itemsContainer: ItemsContainer<T>, index: Int, modifier: Modifier) -> Unit,
) {
    key(itemsContainer.modifiedMap.getValue(index).value) {
        logger.debug("recomposing item $index")

        val stablePosition: Animatable<Float, AnimationVector1D> = itemsContainer.itemStablePositions.getValue(index)
        val offsetYAnimated: Animatable<Float, AnimationVector1D> = itemsContainer.itemAnimatedOffsets.getValue(index)
        var itemModifier = Modifier
            .offset { IntOffset(0, stablePosition.value.roundToInt() + offsetYAnimated.value.roundToInt()) }
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, colorItemListBorder))
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                itemsContainer.itemHeights[index] = coordinates.size.height.toFloat() //+ itemsContainer.dividerPx
            }
        val isDraggingMe: State<Boolean> = itemsContainer.isDraggingMes.getValue(index)
        if (isDraggingMe.value) {
            itemModifier = itemModifier
                .offset { IntOffset(0, scrollDiff.value.roundToInt()) }
                .background(Color.LightGray.copy(alpha = 0.3f))
        }

        itemContent(itemsContainer, index, itemModifier)
    }
}

private fun <T> Modifier.createReorderButtonModifier(
    itemsContainer: ItemsContainer<T>,
    index: Int,
    draggingIndex: MutableState<Int>,
    scrollState: ScrollState,
    scrollDiff: MutableState<Float>,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    scrollJob: MutableState<Job?>,
    onReorder: (newItems: MutableList<T>) -> Unit,
) = this.pointerInput(index) {
    detectDragGestures(

        onDragStart = { _: Offset ->
            draggingIndex.value = index
            scrollDiff.value = 0f
            coroutineScope.launch {
                itemsContainer.itemAnimatedOffsets[index]?.snapTo(0f)
            }
        },

        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()

            scrollJob.value?.cancel()
            scrollJob.value = null

            var offsetYAnimatedVal: Float = itemsContainer.itemAnimatedOffsets[index]?.targetValue ?: 0f
            val relativateOffset: Float = offsetYAnimatedVal + scrollDiff.value
            val thisHeight = itemsContainer.itemHeights[index] ?: 0f
            val position = itemsContainer.indexToPositionMap.getValue(index) // real positional index on view
            val draggedId: Int = index

            // minimize overlap by moving item when it's half-covered
            val swappedBy: Int = calculateItemsToSwap(
                index, position, relativateOffset, itemsContainer.items.size, itemsContainer.itemHeights,
                itemsContainer.positionToIndexMap,
            )

            // reorder overlapped items in temporary maps
            when {
                swappedBy < 0 -> { // moving up
                    var draggedPxDelta = 0f
                    for(swapStep in 1..-swappedBy) {
                        val swappedPosition = position - swapStep // position of item being swapped
                        val newPosition = swappedPosition + 1
                        val swappedId = itemsContainer.positionToIndexMap.getValue(swappedPosition)
                        draggedPxDelta += itemsContainer.itemHeights[swappedId] ?: 0f
                        val currentStablePosition = itemsContainer.itemStablePositions[swappedId]?.targetValue ?: 0f
                        coroutineScope.launch {
                            itemsContainer.itemStablePositions[swappedId]?.animateTo(currentStablePosition + thisHeight)
                        }
                        itemsContainer.indexToPositionMap[swappedId] = newPosition
                        itemsContainer.positionToIndexMap[newPosition] = swappedId
                    }

                    itemsContainer.indexToPositionMap[draggedId] = position + swappedBy
                    itemsContainer.positionToIndexMap[position + swappedBy] = draggedId
                    val currentStablePosition = itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f
                    offsetYAnimatedVal += draggedPxDelta
                    coroutineScope.launch {
                        itemsContainer.itemStablePositions[draggedId]?.snapTo(currentStablePosition - draggedPxDelta)
                        itemsContainer.itemAnimatedOffsets[draggedId]?.snapTo(offsetYAnimatedVal)
                    }
                }

                swappedBy > 0 -> { // moving down
                    var draggedPxDelta = 0f
                    for(swapStep in 1..swappedBy) {
                        val swappedPosition = position + swapStep // position of item being swapped
                        val newPosition = swappedPosition - 1
                        val swappedId = itemsContainer.positionToIndexMap.getValue(swappedPosition)
                        draggedPxDelta += itemsContainer.itemHeights[swappedId] ?: 0f
                        val currentStablePosition = itemsContainer.itemStablePositions[swappedId]?.targetValue ?: 0f
                        coroutineScope.launch {
                            itemsContainer.itemStablePositions[swappedId]?.animateTo(currentStablePosition - thisHeight)
                        }
                        itemsContainer.indexToPositionMap[swappedId] = newPosition
                        itemsContainer.positionToIndexMap[newPosition] = swappedId
                    }

                    itemsContainer.indexToPositionMap[draggedId] = position + swappedBy
                    itemsContainer.positionToIndexMap[position + swappedBy] = draggedId
                    val currentStablePosition = itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f
                    offsetYAnimatedVal -= draggedPxDelta
                    coroutineScope.launch {
                        itemsContainer.itemStablePositions[draggedId]?.snapTo(currentStablePosition + draggedPxDelta)
                        itemsContainer.itemAnimatedOffsets[draggedId]?.snapTo(offsetYAnimatedVal)
                    }
                }
            }

            // overscroll
//            var priorVisibleHeight = thisHeight / 2 - scrollState.value
//            for (i in 0 until index) {
//                priorVisibleHeight += itemsContainer.itemHeights[i] ?: 0f
//            }
//            val beyondVisibleHeight = parentViewportHeight.value - priorVisibleHeight
//            val borderArea = thisHeight * 2.5f
//            val overscrolledTop = priorVisibleHeight + relativateOffset - borderArea
//            val overscrolledBottom = -beyondVisibleHeight + relativateOffset + borderArea
//            val movedABit = relativateOffset.absoluteValue > thisHeight
//            val overscrolledY: Float = when {
//                (offsetYAnimatedVal < 0 || movedABit) && overscrolledTop < 0 && scrollState.canScrollBackward -> {
//                    overscrolledTop
//                }
//                (offsetYAnimatedVal > 0 || movedABit) && overscrolledBottom > 0 && scrollState.canScrollForward -> {
//                    overscrolledBottom
//                }
//                else -> 0f
//            }

            coroutineScope.launch {
                itemsContainer.itemAnimatedOffsets[index]?.snapTo(offsetYAnimatedVal + dragAmount.y)
            }

//            if (overscrolledY != 0f) {
//                val scrollBy = overscrolledY * 0.07f
//                scrollJob.value = coroutineScope.launch {
//                    while ((scrollState.canScrollForward && scrollBy > 0) || (scrollState.canScrollBackward && scrollBy < 0)) {
//                        yield()
//                        scrollDiff.value += scrollState.scrollBy(scrollBy)
//                        delay(20)
//                    }
//                }
//            }
        },

        onDragEnd = {
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            scrollJob.value?.cancel()
            scrollJob.value = null
            val relativateOffset = (itemsContainer.itemAnimatedOffsets[index]?.targetValue ?: 0f) + scrollDiff.value
            coroutineScope.launch {
                itemsContainer.itemAnimatedOffsets[index]?.snapTo(relativateOffset)
                itemsContainer.itemAnimatedOffsets[index]?.animateTo(0f)
            }
        },

        onDragCancel = {
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            scrollJob.value?.cancel()
            scrollJob.value = null
            val relativateOffset = (itemsContainer.itemAnimatedOffsets[index]?.targetValue ?: 0f) + scrollDiff.value
            coroutineScope.launch {
                itemsContainer.itemAnimatedOffsets[index]?.snapTo(relativateOffset)
                itemsContainer.itemAnimatedOffsets[index]?.animateTo(0f)
            }
        },
    )
}

private fun calculateItemsToSwap(
    itemIndex: Int, // ID
    position: Int, // real item position
    offsetY: Float, // relativate Offset
    itemsCount: Int,
    itemHeights: Map<Int, Float>,
    positionToIndexMap: MutableMap<Int, Int>, // real displayed index to item index (ID) on list
): Int {
    // Return swapped positions and corresponding pixels
    val thisItemHeight: Float = itemHeights[itemIndex] ?: return 0
    var swappedBy = 0
    var overlapY: Float = abs(offsetY)
    when {
        offsetY < 0 -> { // moving up
            while (true) {
                val currentPosition: Int = position + swappedBy
                if (currentPosition <= 0)
                    return swappedBy
                val nextItemId = positionToIndexMap.getValue(currentPosition - 1)
                val nextItemHeight = itemHeights[nextItemId] ?: thisItemHeight // guess the height is the same
                if (overlapY <= nextItemHeight / 2)
                    return swappedBy
                swappedBy -= 1
                overlapY -= nextItemHeight
            }
        }
        offsetY > 0 -> { // moving down
            while (true) {
                val currentPosition: Int = position + swappedBy
                if (currentPosition >= itemsCount - 1)
                    return swappedBy
                val nextItemId = positionToIndexMap.getValue(currentPosition + 1)
                val nextItemHeight = itemHeights[nextItemId] ?: thisItemHeight
                if (overlapY <= nextItemHeight / 2)
                    return swappedBy
                swappedBy += 1
                overlapY -= nextItemHeight
            }
        }
        else -> return 0
    }
}

private fun <T> persistSwappedItems(
    itemsContainer: ItemsContainer<T>,
    onReorder: (newItems: MutableList<T>) -> Unit,
) {
    val changesMade = itemsContainer.items.indices.any { index: Int ->
        val position = itemsContainer.indexToPositionMap.getValue(index)
        position != index
    }
    if (!changesMade) return

    val indicesNewOrder = itemsContainer.items.indices.map { position: Int ->
        itemsContainer.positionToIndexMap.getValue(position)
    }
    if (indicesNewOrder.distinct().size != itemsContainer.items.size)
        throw RuntimeException("new indices don't contain the same original indices")

    val newItems: MutableList<T> = indicesNewOrder.map { index: Int ->
        itemsContainer.items[index]
    }.toMutableList()

    onReorder(newItems)
}