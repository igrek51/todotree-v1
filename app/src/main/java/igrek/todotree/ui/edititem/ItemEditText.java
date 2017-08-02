package igrek.todotree.ui.edititem;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import igrek.todotree.ui.numkeyboard.NumKeyboardListener;

public class ItemEditText extends EditText {
	
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
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ItemEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
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
