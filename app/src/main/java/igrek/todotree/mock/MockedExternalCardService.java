package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.filesystem.ExternalCardService;

public class MockedExternalCardService extends ExternalCardService {
	
	public MockedExternalCardService(Logs logger, Activity activity) {
		super(logger, activity);
	}
	
	@Override
	protected String findExternalSDPath() {
		return "/storage/extSdCard";
	}
}
