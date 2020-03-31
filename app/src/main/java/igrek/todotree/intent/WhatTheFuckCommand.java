package igrek.todotree.intent;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.service.access.QuickAddService;
import igrek.todotree.service.remote.RemotePushService;

public class WhatTheFuckCommand {
	
	@Inject
	QuickAddService quickAddService;
	@Inject
	RemotePushService remotePushService;
	
	public WhatTheFuckCommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	public boolean isQuickAddModeEnabled() {
		return quickAddService.isQuickAddMode();
	}
	
	public boolean isRemotePushEnabled() {
		return remotePushService.isRemotePushingEnabled();
	}
}
