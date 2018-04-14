package igrek.todotree.commands;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.services.statistics.StatisticsLogService;

//TODO when deleted add event to interim buffer only, save to log when db is really saved
public class StatisticsCommand {
	
	@Inject
	StatisticsLogService statisticsLogService;
	
	public StatisticsCommand() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void onTaskCreated(AbstractTreeItem item) {
		if (item instanceof TextTreeItem) {
			statisticsLogService.logTaskCreate(item.getDisplayName());
		}
	}
	
	public void onTaskRemoved(AbstractTreeItem item) {
		if (item instanceof TextTreeItem) {
			// log item and its children
			statisticsLogService.logTaskComplete(item.getDisplayName());
			for (AbstractTreeItem child : item.getChildren()) {
				onTaskRemoved(child);
			}
		}
	}
	
}
