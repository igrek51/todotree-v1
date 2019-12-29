package igrek.todotree.activity;

import android.content.Intent;
import android.os.Bundle;

public class QuickAddActivity extends MainActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		quickAddService.enableQuickAdd();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		stdNewIntent(intent);
		logger.debug("recreating new quick add activity");
		recreate();
	}
	
}
