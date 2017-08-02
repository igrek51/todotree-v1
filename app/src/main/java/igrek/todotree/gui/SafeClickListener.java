package igrek.todotree.gui;


import android.view.View;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.resources.UserInfoService;

public abstract class SafeClickListener implements View.OnClickListener {
	
	@Inject
	UserInfoService userInfoService;
	
	@Override
	public void onClick(View var1) {
		try {
			onClick();
		} catch (Throwable t) {
			handleError(t);
		}
	}
	
	public abstract void onClick() throws Throwable;
	
	private void handleError(Throwable t) {
		Logs.error(t);
		DaggerIOC.getAppComponent().inject(this);
		userInfoService.showInfo("Error occurred: " + t.getMessage());
	}
}
