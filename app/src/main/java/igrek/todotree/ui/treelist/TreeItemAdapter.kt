package igrek.todotree.ui.treelist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.SparseArray
import android.view.*
import android.widget.*
import igrek.todotree.R
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.ItemSelectionCommand
import igrek.todotree.intent.TreeCommand
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class TreeItemAdapter(
    context: Context,
    _dataSource: List<AbstractTreeItem>?,
    listView: TreeListView
) : ArrayAdapter<AbstractTreeItem>(context, 0, ArrayList<AbstractTreeItem>()) {

    private var dataSource: List<AbstractTreeItem>?
    private var selections: Set<Int>? = null // selected indexes
    private val listView: TreeListView
    private val storedViews: SparseArray<View>
    private val inflater: LayoutInflater

    init {
        var dataSource: List<AbstractTreeItem>? = _dataSource
        if (dataSource == null) dataSource = ArrayList()
        this.dataSource = dataSource
        this.listView = listView
        storedViews = SparseArray<View>()
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<AbstractTreeItem>?) {
        this.dataSource = dataSource
        storedViews.clear()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getItem(position: Int): AbstractTreeItem {
        return dataSource!![position]
    }

    val items: List<Any>?
        get() = dataSource

    fun setSelections(selections: Set<Int>?) {
        this.selections = selections
    }

    fun getStoredView(position: Int): View? {
        return if (position >= dataSource!!.size) null else storedViews.get(position)
    }

    override fun getCount(): Int {
        return dataSource!!.size + 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        if (position < 0) return -1
        return if (position >= dataSource!!.size) -1 else position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (position == dataSource!!.size) {
            getAddItemView(position, parent)
        } else {
            getItemView(position, parent)
        }
    }

    private fun getItemView(position: Int, parent: ViewGroup): View {
        // get from cache
        if (storedViews.get(position) != null) return storedViews.get(position)
        val item: AbstractTreeItem = dataSource!![position]
        val itemView: View = if (!item.isEmpty) {
            getParentItemView(item, position, parent)
        } else {
            getSingleItemView(item, position, parent)
        }

        // store view
        storedViews.put(position, itemView)
        return itemView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getSingleItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView: View = inflater.inflate(R.layout.tree_item_single, parent, false)

        val textView: TextView = itemView.findViewById(R.id.tvItemContent)
        textView.text = item.displayName

        // link
        if (item is LinkTreeItem) {
            val content = SpannableString(item.displayName)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            textView.text = content
        }

        val moveButton: ImageButton = itemView.findViewById(R.id.buttonItemMove)
        moveButton.isFocusableInTouchMode = false
        moveButton.isFocusable = false
        moveButton.isClickable = false
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener(View.OnTouchListener { v: View?, event: MotionEvent ->
                event.source = 777 // from moveButton
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder?.onItemMoveButtonPressed(
                            position, item, itemView, event.x, event.y + moveButton.top
                        )
                        return@OnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@OnTouchListener false
                    MotionEvent.ACTION_UP -> {
                        listView.reorder?.onItemMoveButtonReleased(
                            position, item, itemView, event.x, event.y + moveButton.top
                        )
                        return@OnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.visibility = View.INVISIBLE
            moveButton.layoutParams = RelativeLayout.LayoutParams(0, 0)
        }

        val cbItemSelected: CheckBox = itemView.findViewById(R.id.cbItemSelected)
        cbItemSelected.isFocusableInTouchMode = false
        cbItemSelected.isFocusable = false
        if (selections != null) {
            cbItemSelected.visibility = View.VISIBLE
            cbItemSelected.isChecked = selections!!.contains(position)
            cbItemSelected.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                ItemSelectionCommand()
                    .selectedItemClicked(position, isChecked)
            }
        }
        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))

        //add new item above
        val addButton: ImageButton = itemView.findViewById(R.id.buttonItemAdd)
        addButton.isFocusableInTouchMode = false
        addButton.isFocusable = false
        addButton.isClickable = true
        increaseTouchArea(addButton, 20)
        if (selections == null) {
            addButton.visibility = View.VISIBLE
            addButton.setOnClickListener(SafeClickListener {
                ItemEditorCommand().addItemHereClicked(position)
            })
        } else {
            addButton.visibility = View.GONE
        }

        // button: enter item
        val buttonItemEnter: ImageButton = itemView.findViewById(R.id.buttonItemEnter)
        buttonItemEnter.isFocusableInTouchMode = false
        buttonItemEnter.isFocusable = false
        buttonItemEnter.isClickable = true
        increaseTouchArea(buttonItemEnter, 20)
        if (selections == null) {
            buttonItemEnter.visibility = View.VISIBLE
            buttonItemEnter.setOnClickListener(SafeClickListener {
                TreeCommand().itemGoIntoClicked(position, item)
            })
        } else {
            buttonItemEnter.visibility = View.GONE
        }
        return itemView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getParentItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView: View = inflater.inflate(R.layout.tree_item_parent, parent, false)

        val textView: TextView = itemView.findViewById(R.id.tvItemContent)
        textView.text = item.displayName

        val tvItemChildSize: TextView = itemView.findViewById(R.id.tvItemChildSize)
        val contentBuilder = "[" + item.size() + "]"
        tvItemChildSize.text = contentBuilder

        val editButton: ImageButton = itemView.findViewById(R.id.buttonItemEdit)
        editButton.isFocusableInTouchMode = false
        editButton.isFocusable = false
        editButton.isClickable = true
        increaseTouchArea(editButton, 20)
        if (selections == null && !item.isEmpty) {
            editButton.setOnClickListener(SafeClickListener {
                ItemEditorCommand().itemEditClicked(item)
            })
        } else {
            editButton.visibility = View.GONE
        }

        //add new item above
        val addButton: ImageButton = itemView.findViewById(R.id.buttonItemAdd)
        addButton.isFocusableInTouchMode = false
        addButton.isFocusable = false
        addButton.isClickable = true
        increaseTouchArea(addButton, 20)
        if (selections == null) {
            addButton.visibility = View.VISIBLE
            addButton.setOnClickListener(SafeClickListener {
                ItemEditorCommand().addItemHereClicked(position)
            })
        } else {
            addButton.visibility = View.GONE
        }

        val moveButton: ImageButton = itemView.findViewById(R.id.buttonItemMove)
        moveButton.isFocusableInTouchMode = false
        moveButton.isFocusable = false
        moveButton.isClickable = false
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener(View.OnTouchListener { v: View?, event: MotionEvent ->
                event.source = 777 // from moveButton
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder?.onItemMoveButtonPressed(
                            position, item, itemView, event.x, event.y + moveButton.top
                        )
                        return@OnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@OnTouchListener false
                    MotionEvent.ACTION_UP -> {
                        listView.reorder?.onItemMoveButtonReleased(
                            position, item, itemView, event.x, event.y + moveButton.top
                        )
                        return@OnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.visibility = View.INVISIBLE
            moveButton.layoutParams = RelativeLayout.LayoutParams(0, 0)
        }

        val cbItemSelected: CheckBox = itemView.findViewById(R.id.cbItemSelected)
        cbItemSelected.isFocusableInTouchMode = false
        cbItemSelected.isFocusable = false
        if (selections != null) {
            cbItemSelected.visibility = View.VISIBLE
            cbItemSelected.isChecked = selections!!.contains(position)
            cbItemSelected.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                ItemSelectionCommand()
                    .selectedItemClicked(position, isChecked)
            }
        }
        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))
        return itemView
    }

    private fun getAddItemView(position: Int, parent: ViewGroup): View {
        // plus
        val itemPlus: View = inflater.inflate(R.layout.item_plus, parent, false)
        val plusButton: ImageButton = itemPlus.findViewById(R.id.buttonAddNewItem)
        plusButton.isFocusableInTouchMode = false
        plusButton.isFocusable = false
        plusButton.setOnClickListener(SafeClickListener {
            ItemEditorCommand().addItemClicked()
        })
        // redirect long click to tree list view
        plusButton.isLongClickable = true
        plusButton.setOnLongClickListener { v: View? ->
            listView.onItemLongClick(
                null,
                itemPlus,
                position,
                0
            )
        }
        return itemPlus
    }

    private fun increaseTouchArea(component: View, sidePx: Int) {
        val parent = component.parent as View // button: the view you want to enlarge hit area
        parent.post {
            val rect = Rect()
            component.getHitRect(rect)
            rect.top -= sidePx // increase top hit area
            rect.left -= sidePx // increase left hit area
            rect.bottom += sidePx // increase bottom hit area
            rect.right += sidePx // increase right hit area
            parent.touchDelegate = TouchDelegate(rect, component)
        }
    }
}