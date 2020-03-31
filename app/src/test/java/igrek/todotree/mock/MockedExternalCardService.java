package igrek.todotree.mock;


import igrek.todotree.logger.Logger;

public class MockedExternalCardService extends ExternalCardService {
	
	public MockedExternalCardService(Logger logger) {
		super(logger);
	}
	
	@Override
	protected String findExternalSDPath() {
		return "/storage/extSdCard";
	}
}
