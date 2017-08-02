package igrek.todotree.ui.numkeyboard;

public interface NumKeyboardListener {
	
	void onNumKeyboardClosed();
	
	void onSelectionChanged(int selStart, int selEnd);
}
