package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;
import igrek.todotree.service.resources.InfoBarClickAction;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class MockedUserInfoService extends UserInfoService {
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public MockedUserInfoService(Activity activity, GUI gui) {
		super(activity, gui);
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
