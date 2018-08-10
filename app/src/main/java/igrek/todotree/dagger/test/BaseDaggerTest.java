package igrek.todotree.dagger.test;


import android.app.Activity;

import javax.inject.Inject;

import igrek.todotree.logger.Logger;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.clipboard.TreeClipboardManager;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeScrollCache;
import igrek.todotree.service.tree.persistence.TreePersistenceService;

public abstract class BaseDaggerTest {
	
	@Inject
	protected Activity activity;
	
	@Inject
	protected TreeManager treeManager;
	
	@Inject
	protected TreeClipboardManager treeClipboardManager;
	
	@Inject
	protected TreeScrollCache scrollCache;
	
	@Inject
	protected TreePersistenceService persistenceService;
	
	@Inject
	protected UserInfoService infoService;
	
	@Inject
	protected Logger logger;
	
	@Inject
	protected DatabaseLock dbLock;
	
}
