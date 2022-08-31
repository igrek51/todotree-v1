package igrek.todotree.ui.treelist

import android.content.Context
import android.graphics.Rect
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import igrek.todotree.intent.ItemSelectionCommand
import igrek.todotree.ui.errorcheck.SafeClickListener

class TreeItemAdapter(
    context: Context,
    dataSource: List<AbstractTreeItem>?,
    listView: TreeListView
) : ArrayAdapter<AbstractTreeItem?>(context, 0, ArrayList<AbstractTreeItem>()) {
    private var dataSource: List<AbstractTreeItem>?
    private var selections: Set<Int>? = null // selected indexes
    private val listView: TreeListView
    private val storedViews: SparseArray<View>
    private val inflater: LayoutInflater
    fun setDataSource(dataSource: List<AbstractTreeItem>?) {
        this.dataSource = dataSource
        storedViews.clear()
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): AbstractTreeItem? {
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

    val count: Int
        get() = dataSource!!.size + 1

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
        val itemView: View
        itemView = if (!item.isEmpty) {
            getParentItemView(item, position, parent)
        } else {
            getSingleItemView(item, position, parent)
        }

        // store view
        storedViews.put(position, itemView)
        return itemView
    }

    private fun getSingleItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView: View = inflater.inflate(R.layout.tree_item_single, parent, false)

        val textView: TextView = itemView.findViewById<TextView>(R.id.tvItemContent)
        textView.setText(item.displayName)

        // link
        if (item is LinkTreeItem) {
            val content = SpannableString(item.displayName)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            textView.setText(content)
        }

        val moveButton: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemMove)
        moveButton.setFocusableInTouchMode(false)
        moveButton.setFocusable(false)
        moveButton.setClickable(false)
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent ->
                event.setSource(777) // from moveButton
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder
                            .onItemMoveButtonPressed(
                                position, item, itemView, event.getX(), event
                                    .getY() + moveButton.getTop()
                            )
                        return@setOnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@setOnTouchListener false
                    MotionEvent.ACTION_UP -> {
                        listView.reorder
                            .onItemMoveButtonReleased(
                                position, item, itemView, event.getX(), event
                                    .getY() + moveButton.getTop()
                            )
                        return@setOnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.setVisibility(View.INVISIBLE)
            moveButton.setLayoutParams(RelativeLayout.LayoutParams(0, 0))
        }

        val cbItemSelected: CheckBox = itemView.findViewById<CheckBox>(R.id.cbItemSelected)
        cbItemSelected.setFocusableInTouchMode(false)
        cbItemSelected.setFocusable(false)
        if (selections != null) {
            cbItemSelected.setVisibility(View.VISIBLE)
            if (selections!!.contains(position)) {
                cbItemSelected.setChecked(true)
            } else {
                cbItemSelected.setChecked(false)
            }
            cbItemSelected.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                ItemSelectionCommand()
                    .selectedItemClicked(position, isChecked)
            })
        }
        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))

        //add new item above
        val addButton: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemAdd)
        addButton.setFocusableInTouchMode(false)
        addButton.setFocusable(false)
        addButton.setClickable(true)
        increaseTouchArea(addButton, 20)
        if (selections == null) {
            addButton.setVisibility(View.VISIBLE)
            addButton.setOnClickListener(object : SafeClickListener() {
                fun onClick() {
                    ItemEditorCommand().addItemHereClicked(position)
                }
            })
        } else {
            addButton.setVisibility(View.GONE)
        }

        // button: enter item
        val buttonItemEnter: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemEnter)
        buttonItemEnter.setFocusableInTouchMode(false)
        buttonItemEnter.setFocusable(false)
        buttonItemEnter.setClickable(true)
        increaseTouchArea(buttonItemEnter, 20)
        if (selections == null) {
            buttonItemEnter.setVisibility(View.VISIBLE)
            buttonItemEnter.setOnClickListener(object : SafeClickListener() {
                fun onClick() {
                    TreeCommand().itemGoIntoClicked(position, item)
                }
            })
        } else {
            buttonItemEnter.setVisibility(View.GONE)
        }
        return itemView
    }

    private fun getParentItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView: View = inflater.inflate(R.layout.tree_item_parent, parent, false)

        val textView: TextView = itemView.findViewById<TextView>(R.id.tvItemContent)
        textView.setText(item.displayName)

        val tvItemChildSize: TextView = itemView.findViewById<TextView>(R.id.tvItemChildSize)
        val contentBuilder = "[" + item.size() + "]"
        tvItemChildSize.setText(contentBuilder)

        val editButton: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemEdit)
        editButton.setFocusableInTouchMode(false)
        editButton.setFocusable(false)
        editButton.setClickable(true)
        increaseTouchArea(editButton, 20)
        if (selections == null && !item.isEmpty) {
            editButton.setOnClickListener(object : SafeClickListener() {
                fun onClick() {
                    ItemEditorCommand().itemEditClicked(item)
                }
            })
        } else {
            editButton.setVisibility(View.GONE)
        }

        //add new item above
        val addButton: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemAdd)
        addButton.setFocusableInTouchMode(false)
        addButton.setFocusable(false)
        addButton.setClickable(true)
        increaseTouchArea(addButton, 20)
        if (selections == null) {
            addButton.setVisibility(View.VISIBLE)
            addButton.setOnClickListener(object : SafeClickListener() {
                fun onClick() {
                    ItemEditorCommand().addItemHereClicked(position)
                }
            })
        } else {
            addButton.setVisibility(View.GONE)
        }

        val moveButton: ImageButton = itemView.findViewById<ImageButton>(R.id.buttonItemMove)
        moveButton.setFocusableInTouchMode(false)
        moveButton.setFocusable(false)
        moveButton.setClickable(false)
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent ->
                event.setSource(777) // from moveButton
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder
                            .onItemMoveButtonPressed(
                                position, item, itemView, event.getX(), event
                                    .getY() + moveButton.getTop()
                            )
                        return@setOnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@setOnTouchListener false
                    MotionEvent.ACTION_UP -> {
                        listView.reorder
                            .onItemMoveButtonReleased(
                                position, item, itemView, event.getX(), event
                                    .getY() + moveButton.getTop()
                            )
                        return@setOnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.setVisibility(View.INVISIBLE)
            moveButton.setLayoutParams(RelativeLayout.LayoutParams(0, 0))
        }

        val cbItemSelected: CheckBox = itemView.findViewById<CheckBox>(R.id.cbItemSelected)
        cbItemSelected.setFocusableInTouchMode(false)
        cbItemSelected.setFocusable(false)
        if (selections != null) {
            cbItemSelected.setVisibility(View.VISIBLE)
            if (selections!!.contains(position)) {
                cbItemSelected.setChecked(true)
            } else {
                cbItemSelected.setChecked(false)
            }
            cbItemSelected.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                ItemSelectionCommand()
                    .selectedItemClicked(position, isChecked)
            })
        }
        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))
        return itemView
    }

    private fun getAddItemView(position: Int, parent: ViewGroup): View {
        // plus
        val itemPlus: View = inflater.inflate(R.layout.item_plus, parent, false)
        val plusButton: ImageButton = itemPlus.findViewById<ImageButton>(R.id.buttonAddNewItem)
        plusButton.setFocusableInTouchMode(false)
        plusButton.setFocusable(false)
        plusButton.setOnClickListener(object : SafeClickListener() {
            fun onClick() {
                ItemEditorCommand().addItemClicked()
            }
        })
        // redirect long click to tree list view
        plusButton.setLongClickable(true)
        plusButton.setOnLongClickListener(OnLongClickListener { v: View? ->
            listView.onItemLongClick(
                null,
                itemPlus,
                position,
                0
            )
        })
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

    init {
        var dataSource: List<AbstractTreeItem>? = dataSource
        if (dataSource == null) dataSource = ArrayList<AbstractTreeItem>()
        this.dataSource = dataSource
        this.listView = listView
        storedViews = SparseArray<View>()
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}