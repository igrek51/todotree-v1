package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.services.clipboard.SystemClipboardManager;

public class MockedSystemClipboardManager extends SystemClipboardManager {
	
	public MockedSystemClipboardManager(Activity activity) {
		super(activity);
	}
	
	@Override
	public void copyToSystemClipboard(String text) {
	}
	
	@Override
	public String getSystemClipboard() {
		return null;
	}
}
