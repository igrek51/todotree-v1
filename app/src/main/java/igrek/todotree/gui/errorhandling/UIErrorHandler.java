package igrek.todotree.gui.errorhandling;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.resources.UserInfoService;

public class UIErrorHandler {
	
	@Inject
	UserInfoService userInfoService;
	
	private void _handleError(Throwable t) {
		Logs.error(t);
		DaggerIOC.getAppComponent().inject(this);
		userInfoService.showInfo("Error occurred: " + t.getMessage());
	}
	
	public static void showError(Throwable t){
		new UIErrorHandler()._handleError(t);
	}
	
}
