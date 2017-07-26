package igrek.todotree.logic;


import android.app.Activity;
import android.content.ClipData;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ClipboardManager {
	
	private Activity activity;
	
	public ClipboardManager(Activity activity) {
		this.activity = activity;
	}
	
	public void copyToSystemClipboard(String text) {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Copied Text", text);
		clipboard.setPrimaryClip(clip);
	}
	
	public String getSystemClipboard() {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription()
				.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			if (item == null)
				return null;
			if (item.getText() == null)
				return null;
			return item.getText().toString();
		}
		return null;
	}
	
}
