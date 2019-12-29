package igrek.todotree.commands;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.service.access.QuickAddService;

public class WhatTheFuckCommand {
	
	@Inject
	QuickAddService quickAddService;
	
	public WhatTheFuckCommand() {
		DaggerIOC.getFactoryComponent().inject(this);
	}
	
	public boolean isQuickAddModeEnabled() {
		return quickAddService.isQuickAddMode();
	}
}
