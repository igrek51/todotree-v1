package igrek.todotree.intent;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.service.access.QuickAddService;

public class WhatTheFuckCommand {
	
	@Inject
	QuickAddService quickAddService;
	
	public WhatTheFuckCommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	public boolean isQuickAddModeEnabled() {
		return quickAddService.isQuickAddMode();
	}
}
