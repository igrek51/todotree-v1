package igrek.todotree.ui.treelist

import android.animation.*
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.TreeCommand


class TreeListReorder(private val listView: TreeListView) {

    private val logger = LoggerFactory.logger

    private var startTouchY = 0f
    private var lastTouchY = 0f
    private var scrollStart = 0
    private var draggedItemPos: Int? = null
    private var draggedItemView: View? = null
    private var draggedItemViewTop: Int? = null
    private var hoverBitmap: BitmapDrawable? = null
    private var hoverBitmapAnimation: BitmapDrawable? = null
    var hoverBitmapBounds: Rect? = null
        private set
    private val itemsReplaceCover = 0.65f
    private val hoverBorderThickness = 8
    private val hoverBorderColor = -0x334f4f50
    private val hoverTintColor: Long = 0x44aaaaaa
    private val treeCommand = TreeCommand()

    private fun updateHoverBitmap() {
        if (draggedItemViewTop != null && draggedItemPos != null) {
            val dy = lastTouchY - startTouchY
            hoverBitmapBounds!!.offsetTo(0, draggedItemViewTop!! + dy.toInt())
            hoverBitmap!!.bounds = hoverBitmapBounds!!
        }
    }

    private fun getAndAddHoverView(v: View?): BitmapDrawable {
        val top = v!!.top
        val left = v.left
        val b = getBitmapWithBorder(v)
        val drawable = BitmapDrawable(listView.resources, b)
        hoverBitmapBounds = Rect(left, top, left + v.width, top + v.height)
        drawable.bounds = hoverBitmapBounds!!
        return drawable
    }

    private fun getBitmapWithBorder(v: View?): Bitmap {
        val bitmap = getBitmapFromView(v)
        val canvas = Canvas(bitmap)

        val paint2 = Paint()
        paint2.colorFilter = PorterDuffColorFilter(getColor(hoverTintColor), PorterDuff.Mode.ADD)
        canvas.drawBitmap(bitmap, 0f, 0f, paint2)

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = hoverBorderThickness.toFloat()
        paint.color = hoverBorderColor
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        canvas.drawRect(rect, paint)

        return bitmap
    }

    private fun getBitmapFromView(v: View?): Bitmap {
        val bitmap = Bitmap.createBitmap(v!!.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
    }

    private fun itemDraggingStarted(position: Int, itemView: View) {
        draggedItemPos = position
        draggedItemView = itemView
        draggedItemViewTop = itemView.top
        scrollStart = listView.scrollHandler!!.scrollOffset
        hoverBitmap = getAndAddHoverView(draggedItemView)
        draggedItemView!!.visibility = View.INVISIBLE
        listView.invalidate()
    }

    fun setLastTouchY(lastTouchY: Float) {
        this.lastTouchY = lastTouchY
    }

    fun handleItemDragging() {
        val dyTotal = lastTouchY - startTouchY + (listView.scrollHandler!!.scrollOffset - scrollStart)
        if (draggedItemViewTop == null) {
            logger.error("draggedItemViewTop = null")
            return
        }
        if (draggedItemPos == null) {
            logger.error("draggedItemPos = null")
            return
        }
        var step = 0
        var deltaH = 0
        if (dyTotal > 0) {
            while (true) {
                if (draggedItemPos!! + step + 1 >= listView.items!!.size) break
                val downHeight = listView.getItemHeight(draggedItemPos!! + step + 1)
                if (downHeight == 0) break
                deltaH += if (dyTotal - deltaH > downHeight * itemsReplaceCover) {
                    step++
                    downHeight
                } else {
                    break
                }
            }
        } else if (dyTotal < 0) {
            while (true) {
                if (draggedItemPos!! + step - 1 < 0) break
                val upHeight = listView.getItemHeight(draggedItemPos!! + step - 1)
                if (upHeight == 0) break
                deltaH -= if (-dyTotal + deltaH > upHeight * itemsReplaceCover) {
                    step--
                    upHeight
                } else {
                    break
                }
            }
        }
        if (step != 0) {
            val targetPosition = draggedItemPos!! + step
            val items = treeCommand.itemMoved(
                draggedItemPos!!, step
            )

            if (step > 0) {
                val draggedItemHeight = listView.getItemHeight(draggedItemPos!!)
                for (i in draggedItemPos!! until targetPosition) {
                    val nextHeight = listView.getItemHeight(i + 1)
                    listView.putItemHeight(i, nextHeight)
                }
                listView.putItemHeight(targetPosition, draggedItemHeight)
            } else if (step < 0) {
                val draggedItemHeight = listView.getItemHeight(draggedItemPos!!)
                for (i in draggedItemPos!! downTo targetPosition + 1) {
                    val nextHeight = listView.getItemHeight(i - 1)
                    listView.putItemHeight(i, nextHeight)
                }
                listView.putItemHeight(targetPosition, draggedItemHeight)
            }
            listView.adapter?.setDataSource(items)
            startTouchY += deltaH.toFloat()
            draggedItemViewTop = draggedItemViewTop!! +  deltaH
            draggedItemPos = targetPosition
            if (draggedItemView != null) {
                draggedItemView!!.visibility = View.VISIBLE
            }
            draggedItemView = listView.getItemView(draggedItemPos!!)
            if (draggedItemView != null) {
                draggedItemView!!.visibility = View.INVISIBLE
            }
        }
        listView.scrollHandler?.handleScrolling()
        listView.invalidate()
    }

    fun itemDraggingStopped() {
        if (draggedItemPos != null && draggedItemViewTop != null) {
            draggedItemPos = null

            hoverBitmapAnimation = hoverBitmap
            val scrollOffset = listView.scrollHandler!!.scrollOffset
            hoverBitmapBounds!!.offsetTo(0, draggedItemViewTop!! - (scrollOffset - scrollStart))
            val hoverBitmapBoundsCopy = Rect(hoverBitmapBounds)
            val draggedItemViewCopy = draggedItemView
            draggedItemView = null
            hoverBitmap = null
            draggedItemViewTop = null
            val hoverViewAnimator = ObjectAnimator.ofObject(
                hoverBitmapAnimation,
                "bounds",
                rectBoundsEvaluator,
                hoverBitmapBoundsCopy
            )
            hoverViewAnimator.addUpdateListener { _: ValueAnimator? -> listView.invalidate() }
            hoverViewAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    listView.isEnabled = false
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (draggedItemViewCopy != null) {
                        draggedItemViewCopy.visibility = View.VISIBLE
                    }
                    hoverBitmapAnimation = null
                    listView.isEnabled = true
                    listView.invalidate()
                }
            })
            hoverViewAnimator.start()
        } else {
            draggedItemPos = null
            draggedItemView = null
            draggedItemViewTop = null
        }
    }

    fun onItemMoveButtonPressed(
        position: Int,
        item: AbstractTreeItem?,
        itemView: View,
        touchX: Float,
        touchY: Float
    ) {
        startTouchY = itemView.top + touchY
        lastTouchY = startTouchY
        itemDraggingStarted(position, itemView)
    }

    fun onItemMoveButtonReleased(
        position: Int,
        item: AbstractTreeItem?,
        itemView: View?,
        touchX: Float,
        touchY: Float
    ) {
        itemDraggingStopped()
    }

    val isDragging: Boolean
        get() = draggedItemPos != null

    fun setDraggedItemView() {
        val draggedItemViewOld = draggedItemView
        draggedItemView = listView.getItemView(draggedItemPos!!)
        if (draggedItemView != null) {
            draggedItemView!!.visibility = View.INVISIBLE
        }
        if (draggedItemViewOld !== draggedItemView && draggedItemViewOld != null) {
            draggedItemViewOld.visibility = View.VISIBLE
        }
    }

    fun dispatchDraw(canvas: Canvas?) {
        if (hoverBitmap != null) {
            updateHoverBitmap()
            hoverBitmap!!.draw(canvas!!)
        }
        if (hoverBitmapAnimation != null) {
            hoverBitmapAnimation!!.draw(canvas!!)
        }
    }

    companion object {
        private val rectBoundsEvaluator: TypeEvaluator<Rect> = object : TypeEvaluator<Rect> {
            override fun evaluate(fraction: Float, startValue: Rect, endValue: Rect): Rect {
                return Rect(
                    interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction)
                )
            }

            fun interpolate(start: Int, end: Int, fraction: Float): Int {
                return (start + fraction * (end - start)).toInt()
            }
        }
    }
}

fun getColor(color: Long): Int {
    // if alpha channel is not set - set it to max (opaque)
    if (color and -0x1000000 == 0L)
        return (color or -0x1000000).toInt()
    return color.toInt()
}
