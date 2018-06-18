package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.filesystem.ExternalCardService;
import igrek.todotree.services.filesystem.FilesystemService;

public class MockedFilesystemService extends FilesystemService {
	
	public MockedFilesystemService(Logs logger, Activity activity, ExternalCardService externalCardService) {
		super(logger, activity, externalCardService);
	}
	
	@Override
	protected void pathSDInit() {
		pathToExtSD = "/storage/extSdCard";
	}
}
