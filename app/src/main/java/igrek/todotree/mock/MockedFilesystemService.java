package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.services.filesystem.FilesystemService;

public class MockedFilesystemService extends FilesystemService {
	
	public MockedFilesystemService(Activity activity) {
		super(activity);
	}
	
	@Override
	protected void pathSDInit() {
		pathToExtSD = "/storage/extSdCard";
	}
}
