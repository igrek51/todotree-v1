package igrek.todotree.ui.contextmenu;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.actions.ItemActionController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.services.clipboard.TreeClipboardManager;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

public class ItemActionsMenu {
	
	private int position;
	
	@Inject
	Activity activity;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	TreeClipboardManager treeClipboardManager;
	
	public ItemActionsMenu(int position) {
		this.position = position;
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void show() {
		
		final List<ItemAction> actions = filterVisibleOnly(buildActionsList());
		CharSequence[] actionNames = convertToNamesArray(actions);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Choose action");
		builder.setItems(actionNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				try {
					actions.get(item).execute();
				} catch (Throwable t) {
					UIErrorHandler.showError(t);
				}
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private List<ItemAction> buildActionsList() {
		List<ItemAction> actions = new ArrayList<>();
		
		actions.add(new ItemAction("Remove") {
			@Override
			public void execute() {
				new ItemActionController().actionRemove(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Select") {
			@Override
			public void execute() {
				new ItemActionController().actionSelect(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Edit") {
			@Override
			public void execute() {
				new ItemActionController().actionEdit(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Add above") {
			@Override
			public void execute() {
				new ItemActionController().actionAddAbove(position);
			}
		});
		
		actions.add(new ItemAction("Cut") {
			@Override
			public void execute() {
				new ItemActionController().actionCut(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Copy") {
			@Override
			public void execute() {
				new ItemActionController().actionCopy(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Paste above") {
			@Override
			public void execute() {
				new ItemActionController().actionPasteAbove(position);
			}
		});
		
		actions.add(new ItemAction("Paste as link") {
			@Override
			public void execute() {
				new ItemActionController().actionPasteAboveAsLink(position);
			}
			
			@Override
			public boolean isVisible() {
				return !treeClipboardManager.isClipboardEmpty();
			}
		});
		
		actions.add(new ItemAction("Select all") {
			@Override
			public void execute() {
				new ItemActionController().actionSelectAll(position);
			}
		});
		
		return actions;
	}
	
	private List<ItemAction> filterVisibleOnly(List<ItemAction> actions) {
		List<ItemAction> visibleActions = new ArrayList<>();
		
		for (ItemAction action : actions) {
			if (action.isVisible()) {
				visibleActions.add(action);
			}
		}
		
		return visibleActions;
	}
	
	private CharSequence[] convertToNamesArray(List<ItemAction> actions) {
		CharSequence[] actionNames = new CharSequence[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			actionNames[i] = actions.get(i).getName();
		}
		return actionNames;
	}
	
}
