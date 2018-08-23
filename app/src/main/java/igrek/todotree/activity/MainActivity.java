package igrek.todotree.activity;

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

public class MainActivity extends AppCompatActivity {
	
	@Inject
	AppControllerService appControllerService;
	@Inject
	Logger logger;
	
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
			appControllerService.init();
		} catch (Exception ex) {
			logger.fatal(this, ex);
		}
		logger.debug("Activity has been created: " + this.getClass().getName());
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			appControllerService.onResizeEvent(newConfig);
		}
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

