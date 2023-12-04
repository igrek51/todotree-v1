package igrek.todotree.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import igrek.todotree.domain.treeitem.AbstractTreeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

val itemBorderStroke = BorderStroke(0.5.dp, colorItemListBorder)
private val logger = igrek.todotree.info.logger.LoggerFactory.logger

class ItemsContainer(
    var items: MutableList<AbstractTreeItem> = mutableListOf(),
    val modifiedAll: MutableState<Long> = mutableLongStateOf(0),
    val itemHeights: MutableMap<Int, Float> = mutableMapOf(),
    val itemBiasOffsets: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(),
    val itemStablePositions: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(), // ID to position offset
    val itemHighlights: MutableMap<Int, Animatable<Float, AnimationVector1D>> = mutableMapOf(),
    val reorderButtonModifiers: MutableMap<Int, Modifier> = mutableMapOf(),
    val itemModifiers: MutableMap<Int, Modifier> = mutableMapOf(),
    val isDraggingMes: MutableMap<Int, State<Boolean>> = mutableMapOf(),
    val indexToPositionMap: MutableMap<Int, Int> = mutableMapOf(), // item index (ID) on list to real displayed position index
    val positionToIndexMap: MutableMap<Int, Int> = mutableMapOf(), // real displayed index to item index (ID) on list
    var totalRelativeSwapOffset: Float = 0f,
    val overscrollDiff: MutableState<Float> = mutableFloatStateOf(0f),
    val parentViewportWidth: MutableState<Float> = mutableFloatStateOf(0f),
    val parentViewportHeight: MutableState<Float> = mutableFloatStateOf(0f),
    val highlightedIndex: MutableState<Int> = mutableIntStateOf(-1),
    private val draggingIndex: MutableState<Int> = mutableIntStateOf(-1),
    var scrollJob: Job? = null,
    val maxItemsSize: MutableState<Int> = mutableIntStateOf(10),
    private val actualItemsSize: MutableState<Int> = mutableIntStateOf(0),
    val itemContentKeys: MutableMap<Int, MutableState<String>> = mutableMapOf(),
    val isItemVisibles: MutableMap<Int, State<Boolean>> = mutableMapOf(),
    val isItemParent: MutableMap<Int, MutableState<Boolean>> = mutableMapOf(),
    private var coroutineScope: CoroutineScope? = null,
    val isSelected: MutableMap<Int, MutableState<Boolean>> = mutableMapOf(),
    var indicesOrder: List<Int> = listOf(),
    val reorderMutex: Mutex = Mutex(),
    var onDragJob: Job? = null,
) {
    fun init(
        coroutineScope: CoroutineScope,
        scrollState: ScrollState,
        onReorder: (newItems: MutableList<AbstractTreeItem>) -> Unit,
    ) {
        this.coroutineScope = coroutineScope
        (0 .. maxItemsSize.value).forEach { index: Int ->
            indexToPositionMap[index] = index
            positionToIndexMap[index] = index

            itemBiasOffsets.getOrPut(index) {
                Animatable(0f)
            }
            itemStablePositions.getOrPut(index) {
                Animatable(0f)
            }
            itemHighlights.getOrPut(index) {
                Animatable(0f)
            }

            isDraggingMes.getOrPut(index) {
                derivedStateOf {
                    draggingIndex.value == index
                }
            }
            isSelected.getOrPut(index) {
                mutableStateOf(false)
            }
            isItemVisibles.getOrPut(index) {
                derivedStateOf {
                    index < actualItemsSize.value
                }
            }
            isItemParent.getOrPut(index) {
                mutableStateOf(false)
            }
            reorderButtonModifiers[index] = Modifier.createReorderButtonModifier(
                this, index, draggingIndex, scrollState, parentViewportHeight,
                coroutineScope, onReorder,
            )
            itemModifiers[index] = Modifier.createItemModifier(
                this, index,
            )
        }
        indicesOrder = items.indices.toList()
    }

    private suspend fun rearrange() {
        items.indices.forEach { index: Int ->
            indexToPositionMap[index] = index
            positionToIndexMap[index] = index

            itemBiasOffsets[index]?.snapTo(0f)
            itemStablePositions[index]?.snapTo(0f)
            itemHighlights[index]?.snapTo(0f)
        }
        indicesOrder = items.indices.toList()
    }

    fun replaceAll(
        newList: MutableList<AbstractTreeItem>,
    ) {
        items = newList

        actualItemsSize.value = newList.size
        if (newList.size > maxItemsSize.value) {
            maxItemsSize.value = newList.size
        }
        modifiedAll.value += 1

        (0 .. maxItemsSize.value).forEach { index: Int ->
            val keyState = itemContentKeys.getOrPut(index) {
                mutableStateOf("")
            }
            keyState.value = when {
                index < newList.size -> {
                    val item = newList[index]
                    evaluateKey(item)
                }
                else -> ""
            }
        }

        coroutineScope?.launch {
            rearrange()
        }
    }

    private fun evaluateKey(item: AbstractTreeItem): String {
        return "${item.typeName}|${item.displayName}|${item.size()}"
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ReorderListView(
    itemsContainer: ItemsContainer,
    scrollState: ScrollState = rememberScrollState(),
    onReorder: (newItems: MutableList<AbstractTreeItem>) -> Unit,
    onLoad: () -> Unit,
    itemContent: @Composable (itemsContainer: ItemsContainer, index: Int, modifier: Modifier) -> Unit,
    postContent: @Composable () -> Unit,
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    key(itemsContainer.maxItemsSize.value) {
        logger.debug("recomposing all items")

        itemsContainer.init(coroutineScope, scrollState, onReorder)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    itemsContainer.parentViewportHeight.value =
                        coordinates.parentLayoutCoordinates?.size?.height?.toFloat() ?: 0f
                    itemsContainer.parentViewportWidth.value =
                        coordinates.parentLayoutCoordinates?.size?.width?.toFloat() ?: 0f
                },
        ) {

            (0 .. itemsContainer.maxItemsSize.value).forEach { index: Int ->
                val itemModifier = itemsContainer.itemModifiers.getValue(index)
                itemContent(itemsContainer, index, itemModifier)
            }

            postContent()
        }
    }

    EffectsLauncher1(itemsContainer)
    EffectsLauncher2(itemsContainer, onLoad)
}

@Composable
private fun EffectsLauncher1(
    itemsContainer: ItemsContainer,
) {
    val index = itemsContainer.highlightedIndex.value
    LaunchedEffect(index) {
        if (index > -1) {
            itemsContainer.itemHighlights[index]?.snapTo(1f)
            itemsContainer.itemHighlights[index]?.animateTo(
                0f, animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 0,
                    easing = FastOutLinearInEasing,
                )
            )
            itemsContainer.highlightedIndex.value = -1
        }
    }
}

@Composable
private fun EffectsLauncher2(
    itemsContainer: ItemsContainer,
    onLoad: () -> Unit,
) {
    LaunchedEffect(itemsContainer.modifiedAll.value) {
        onLoad()
    }
}

private fun Modifier.createItemModifier(
    itemsContainer: ItemsContainer,
    index: Int,
): Modifier {
    val stablePosition: Animatable<Float, AnimationVector1D> = itemsContainer.itemStablePositions.getValue(index)
    val offsetBias: Animatable<Float, AnimationVector1D> = itemsContainer.itemBiasOffsets.getValue(index)
    val highlightAlpha: Animatable<Float, AnimationVector1D> = itemsContainer.itemHighlights.getValue(index)
    val isDraggingMe: State<Boolean> = itemsContainer.isDraggingMes.getValue(index)
    return this
        .offset { IntOffset(0, stablePosition.value.roundToInt() + offsetBias.value.roundToInt()) }
        .fillMaxWidth()
        .border(itemBorderStroke)
        .onGloballyPositioned { coordinates: LayoutCoordinates ->
            itemsContainer.itemHeights[index] = coordinates.size.height.toFloat()
        }
        .drawBehind {
            if (isDraggingMe.value) {
                drawRect(color = colorItemDraggedBackground)
            } else if (highlightAlpha.value > 0) {
                drawRect(color = colorHighlightParent, alpha = highlightAlpha.value)
            }
        }
}

private fun Modifier.createReorderButtonModifier(
    itemsContainer: ItemsContainer,
    index: Int,
    draggingIndex: MutableState<Int>,
    scrollState: ScrollState,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    onReorder: (newItems: MutableList<AbstractTreeItem>) -> Unit,
) = this.pointerInput(index) {
    detectDragGestures(

        onDragStart = { _: Offset ->
            draggingIndex.value = index
            itemsContainer.overscrollDiff.value = 0f
            itemsContainer.totalRelativeSwapOffset = 0f
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.snapTo(0f)
            }
            // DEBUG: stable positions
//            itemsContainer.items.indices.forEach { ind: Int ->
//                val position = itemsContainer.indexToPositionMap[ind]!!
//                val stablePosition = itemsContainer.itemStablePositions[ind]?.targetValue ?: 0f
//                val itemH = itemsContainer.itemHeights[ind] ?: 0f
//                logger.debug("Reorder: ${ind}, $position - stable: $stablePosition, itemH: $itemH")
//            }
        },

        onDrag = { change: PointerInputChange, dragAmount: Offset ->
            change.consume()
            itemsContainer.onDragJob?.cancel()
            itemsContainer.onDragJob = coroutineScope.launch {
                itemsContainer.reorderMutex.withLock {
                    onReorderDrag(itemsContainer, index, scrollState, parentViewportHeight, coroutineScope, dragAmount)
                }
            }
        },

        onDragEnd = {
            itemsContainer.onDragJob?.cancel()
            itemsContainer.onDragJob = null
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            itemsContainer.scrollJob?.cancel()
            itemsContainer.scrollJob = null
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.animateTo(0f)
            }
        },

        onDragCancel = {
            itemsContainer.onDragJob?.cancel()
            itemsContainer.onDragJob = null
            persistSwappedItems(itemsContainer, onReorder)
            draggingIndex.value = -1
            itemsContainer.scrollJob?.cancel()
            itemsContainer.scrollJob = null
            coroutineScope.launch {
                itemsContainer.itemBiasOffsets[index]?.animateTo(0f)
            }
        },
    )
}

private suspend fun onReorderDrag(
    itemsContainer: ItemsContainer,
    index: Int,
    scrollState: ScrollState,
    parentViewportHeight: MutableState<Float>,
    coroutineScope: CoroutineScope,
    dragAmount: Offset,
) {
    itemsContainer.scrollJob?.cancel()
    itemsContainer.scrollJob = null

    var offsetBias: Float = itemsContainer.itemBiasOffsets[index]?.targetValue ?: 0f // relative offset
    val thisHeight = itemsContainer.itemHeights[index] ?: 0f
    var position = itemsContainer.indexToPositionMap.getValue(index) // real positional index on view
    val draggedId: Int = index

    // minimize overlap by moving item when it's half-covered
    val swappedBy: Int = calculateItemsToSwap(
        index, position, offsetBias, itemsContainer.items.size, itemsContainer.itemHeights,
        itemsContainer.positionToIndexMap,
    )

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

            position += swappedBy
            itemsContainer.indexToPositionMap[draggedId] = position
            itemsContainer.positionToIndexMap[position] = draggedId
            val newStablePosition = (itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f) - draggedPxDelta
            offsetBias += draggedPxDelta
            coroutineScope.launch {
                itemsContainer.itemStablePositions[draggedId]?.snapTo(newStablePosition)
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

            position += swappedBy
            itemsContainer.indexToPositionMap[draggedId] = position
            itemsContainer.positionToIndexMap[position] = draggedId
            val newStablePosition = (itemsContainer.itemStablePositions[draggedId]?.targetValue ?: 0f) + draggedPxDelta
            offsetBias -= draggedPxDelta
            coroutineScope.launch {
                itemsContainer.itemStablePositions[draggedId]?.snapTo(newStablePosition)
            }
        }
        else -> {}
    }

    itemsContainer.totalRelativeSwapOffset += dragAmount.y
    coroutineScope.launch {
        itemsContainer.itemBiasOffsets[draggedId]?.snapTo(offsetBias + dragAmount.y)
    }

    // overscroll
    var priorVisibleHeight = thisHeight / 2 - scrollState.value
    for (i in 0 until position) {
        val itemId = itemsContainer.positionToIndexMap.getValue(i)
        priorVisibleHeight += itemsContainer.itemHeights[itemId] ?: 0f
    }
    val beyondVisibleHeight = parentViewportHeight.value - priorVisibleHeight
    val borderArea = parentViewportHeight.value * 0.18f
    val overscrolledTop = priorVisibleHeight + offsetBias - borderArea
    val overscrolledBottom = -beyondVisibleHeight + offsetBias + borderArea
    val movedABit: Boolean = (itemsContainer.totalRelativeSwapOffset + itemsContainer.overscrollDiff.value).absoluteValue > thisHeight
    val overscrolledY: Float = when {
        (itemsContainer.totalRelativeSwapOffset < 0 || movedABit) && overscrolledTop < 0 && scrollState.canScrollBackward -> {
            overscrolledTop
        }
        (itemsContainer.totalRelativeSwapOffset > 0 || movedABit) && overscrolledBottom > 0 && scrollState.canScrollForward -> {
            overscrolledBottom
        }
        else -> 0f
    }

    if (overscrolledY != 0f) {
        val scrollBy = overscrolledY * 0.07f
        itemsContainer.scrollJob = coroutineScope.launch {
            while ((scrollState.canScrollForward && scrollBy > 0) || (scrollState.canScrollBackward && scrollBy < 0)) {
                yield()
                val scrollDelta = scrollState.scrollBy(scrollBy)
                itemsContainer.overscrollDiff.value += scrollDelta
                itemsContainer.itemBiasOffsets[draggedId]?.snapTo((itemsContainer.itemBiasOffsets[draggedId]?.targetValue ?: 0f) + scrollDelta)
                delay(20)
            }
        }
    }
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

private fun persistSwappedItems(
    itemsContainer: ItemsContainer,
    onReorder: (newItems: MutableList<AbstractTreeItem>) -> Unit,
) {
    val indicesNewOrder = itemsContainer.items.indices.map { position: Int ->
        itemsContainer.positionToIndexMap.getValue(position)
    }
    val hasChanged = itemsContainer.items.indices.any { index: Int ->
        indicesNewOrder[index] != itemsContainer.indicesOrder[index]
    }
    if (!hasChanged) return

    if (indicesNewOrder.distinct().size != itemsContainer.items.size)
        throw RuntimeException("new indices don't contain the same original indices")

    val newItems: MutableList<AbstractTreeItem> = indicesNewOrder.map { index: Int ->
        itemsContainer.items[index]
    }.toMutableList()

    onReorder(newItems)

    itemsContainer.indicesOrder = indicesNewOrder
}
