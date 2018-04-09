package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;

public class MockedFilesystemService extends FilesystemService {
	
	public MockedFilesystemService(Activity activity) {
		super(activity);
	}
	
	@Override
	public PathBuilder externalAndroidDir() {
		return pathSD();
	}
}
