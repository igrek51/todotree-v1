package igrek.todotree.activity;

import android.os.Bundle;

public class QuickAddActivity extends MainActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		quickAddService.initQuickAdd();
	}
	
}
