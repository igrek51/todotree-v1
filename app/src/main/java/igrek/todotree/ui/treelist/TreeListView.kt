package igrek.todotree.ui.treelist

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ListView
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.ui.contextmenu.ItemActionsMenu

class TreeListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private val logger: Logger = LoggerFactory.logger
    private var adapter: TreeItemAdapter? = null
    var scrollHandler: TreeListScrollHandler? = null
        private set
    val reorder: TreeListReorder = TreeListReorder(this)
    val gestureHandler = TreeListGestureHandler(this)

    /** view index -> view height  */
    private val itemHeights: SparseArray<Int> = SparseArray<Int>()

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    fun init(context: Context?) {
        adapter = TreeItemAdapter(context!!, null, this)
        scrollHandler = TreeListScrollHandler(this, context)
        onItemClickListener = this
        onItemLongClickListener = this
        setOnScrollListener(scrollHandler)
        choiceMode = CHOICE_MODE_SINGLE
        setAdapter(adapter!!)
    }

    override fun getAdapter(): TreeItemAdapter {
        return adapter!!
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.source == 777) { // from moveButton
            if (ev.action == MotionEvent.ACTION_MOVE) return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> gestureHandler.gestureStart(event.x, event.y)
            MotionEvent.ACTION_MOVE -> if (reorder.isDragging) {
                reorder.setLastTouchY(event.y)
                reorder.handleItemDragging()
                return false
            }
            MotionEvent.ACTION_UP -> {
                if (gestureHandler.handleItemGesture(
                        event.x,
                        event.y,
                        scrollHandler!!.scrollOffset
                    )
                ) return super.onTouchEvent(event)
                reorder.itemDraggingStopped()
                gestureHandler.reset()
            }
            MotionEvent.ACTION_CANCEL -> {
                reorder.itemDraggingStopped()
                gestureHandler.reset()
            }
        }
        return super.onTouchEvent(event)
    }

    fun onItemTouchDown(position: Int, event: MotionEvent?, v: View?) {
        gestureHandler.gestureStartPos(position)
    }

    override fun invalidate() {
        super.invalidate()
        if (reorder.isDragging) {
            reorder.setDraggedItemView()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        reorder.dispatchDraw(canvas)
    }

    fun setItemsAndSelected(items: List<AbstractTreeItem>, selectedPositions: Set<Int>?) {
        adapter!!.setSelections(selectedPositions)
        setItems(items)
    }

    private fun setItems(items: List<AbstractTreeItem>) {
        adapter!!.setDataSource(items)
        invalidate()
        calculateViewHeights()
    }

    val items: List<Any>?
        get() = adapter!!.items

    private fun calculateViewHeights() {
        // WARNING: for a moment - there's invalidated item heights map
        val observer: ViewTreeObserver = this.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                itemHeights.clear()
                this@TreeListView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                // now view width should be available at last
                val viewWidth: Int = this@TreeListView.width
                if (viewWidth == 0) logger.warn("List view width == 0")
                val measureSpecW: Int = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY)
                for (i in 0 until adapter!!.count) {
                    val itemView = adapter!!.getView(i, null, this@TreeListView)
                    itemView.measure(measureSpecW, MeasureSpec.UNSPECIFIED)
                    itemHeights.put(i, itemView.measuredHeight)
                }
            }
        })
    }

    fun getItemHeight(position: Int): Int {
        val h: Int? = itemHeights.get(position)
        if (h == null) {
            logger.warn("Item View ($position) = null")
            return 0
        }
        return h
    }

    fun putItemHeight(position: Int?, height: Int?) {
        position?.let {
            itemHeights.put(position, height)
        }
    }

    fun getItemView(position: Int): View? {
        return adapter!!.getStoredView(position)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        try {
            if (position == adapter!!.items!!.size) {
                //nowy element
                ItemEditorCommand().addItemClicked()
            } else {
                //istniejący element
                val item: AbstractTreeItem = adapter!!.getItem(position)
                TreeCommand().itemClicked(position, item)
            }
        } catch (t: Throwable) {
            UiErrorHandler.handleError(t)
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        if (!reorder.isDragging) {
            reorder.itemDraggingStopped()
            gestureHandler.reset()
            ItemActionsMenu(position).show(view)
        }
        return true
    }

    public override fun computeVerticalScrollOffset(): Int {
        return super.computeVerticalScrollOffset()
    }

    public override fun computeVerticalScrollExtent(): Int {
        return super.computeVerticalScrollExtent()
    }

    public override fun computeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }

    val currentScrollPosition: Int
        get() = scrollHandler!!.currentScrollPosition

    fun scrollToBottom() {
        scrollHandler!!.scrollToBottom()
    }

    fun scrollToPosition(y: Int) {
        scrollHandler!!.scrollToPosition(y)
    }

    fun scrollToItem(itemIndex: Int) {
        scrollHandler!!.scrollToItem(itemIndex)
    }
}