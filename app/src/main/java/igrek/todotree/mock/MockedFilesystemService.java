package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.Logger;
import igrek.todotree.service.filesystem.ExternalCardService;
import igrek.todotree.service.filesystem.FilesystemService;

public class MockedFilesystemService extends FilesystemService {
	
	public MockedFilesystemService(Logger logger, Activity activity, ExternalCardService externalCardService) {
		super(logger, activity, externalCardService);
	}
	
}
