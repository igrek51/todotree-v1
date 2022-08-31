package igrek.todotree.ui.edititem

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import igrek.todotree.ui.numkeyboard.NumKeyboardListener
import android.util.AttributeSet

class ItemEditText : AppCompatEditText {
    private var listener: NumKeyboardListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (listener != null) {
            listener!!.onSelectionChanged(selStart, selEnd)
        }
    }

    fun setNumKeyboardListener(listener: NumKeyboardListener?) {
        this.listener = listener
    }
}