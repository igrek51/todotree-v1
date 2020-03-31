package igrek.todotree.ui.edititem;


import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;
import igrek.todotree.ui.numkeyboard.NumKeyboardListener;

public class ItemEditText extends AppCompatEditText {
	
	private NumKeyboardListener listener = null;
	
	public ItemEditText(Context context) {
		super(context);
	}
	
	public ItemEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ItemEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		if (listener != null) {
			listener.onSelectionChanged(selStart, selEnd);
		}
	}
	
	public void setNumKeyboardListener(NumKeyboardListener listener) {
		this.listener = listener;
	}
}
