package igrek.todotree.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.app.AppControllerService;
import igrek.todotree.commands.NavigationCommand;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.access.QuickAddService;

public class MainActivity extends AppCompatActivity {
	
	@Inject
	AppControllerService appControllerService;
	@Inject
	QuickAddService quickAddService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public static final String EXTRA_ACTION_KEY = "extraAction";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// prohibit from creating the thumbnails
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		
		super.onCreate(savedInstanceState);
		
		// Dagger Container init
		DaggerIOC.init(this);
		DaggerIOC.getFactoryComponent().inject(this);
		logger.debug("Creating activity " + this.getClass().getName());
		
		try {
			// running extra action directly from launcher - finish activity
			if (runExtraAction(getIntent())) {
				moveTaskToBack(true);
				finish();
			}
			
			appControllerService.init();
		} catch (Exception ex) {
			logger.fatal(this, ex);
		}
		logger.debug("Activity has been created: " + this.getClass().getName());
	}
	
	private boolean runExtraAction(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String action = extras.getString(EXTRA_ACTION_KEY);
			if (action != null) {
				appControllerService.runExtraAction(action);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		logger.debug("new intent received");
		runExtraAction(intent);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			appControllerService.onResizeEvent(newConfig);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return new NavigationCommand().optionsSelect(item.getItemId()) || super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (onKeyBack())
				return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (onKeyMenu())
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean onKeyMenu() {
		return false;
	}
	
	private boolean onKeyBack() {
		return new NavigationCommand().backClicked();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
}


