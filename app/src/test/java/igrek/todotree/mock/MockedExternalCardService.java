package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.Logger;
import igrek.todotree.service.filesystem.ExternalCardService;

public class MockedExternalCardService extends ExternalCardService {
	
	public MockedExternalCardService(Logger logger, Activity activity) {
		super(logger, activity);
	}
	
	@Override
	protected String findExternalSDPath() {
		return "/storage/extSdCard";
	}
}
