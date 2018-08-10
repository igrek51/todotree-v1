package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.Logger;
import igrek.todotree.service.resources.InfoBarClickAction;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class MockedUserInfoService extends UserInfoService {
	
	public MockedUserInfoService(Activity activity, GUI gui, Logger logger) {
		super(activity, gui, logger);
	}
	
	@Override
	public void showInfo(String info) {
		logger.info("info: " + info);
	}
	
	@Override
	public void showInfoCancellable(String info, InfoBarClickAction cancelCallback) {
		logger.info("cancellable info: " + info);
	}
}
