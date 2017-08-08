package igrek.todotree.dagger.test;


import android.app.Activity;

import javax.inject.Inject;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.clipboard.TreeClipboardManager;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.serializer.JsonTreeSerializer;

public abstract class BaseDaggerTest {
	
	@Inject
	protected Activity activity;
	
	@Inject
	protected JsonTreeSerializer jsonTreeSerializer;
	
	@Inject
	protected TreeManager treeManager;
	
	@Inject
	protected TreeClipboardManager treeClipboardManager;
	
	@Inject
	protected TreeScrollCache scrollCache;
	
	@Inject
	protected JsonTreeSerializer serializer;
	
	@Inject
	protected UserInfoService infoService;
	
	@Inject
	protected Logs logger;
	
}
