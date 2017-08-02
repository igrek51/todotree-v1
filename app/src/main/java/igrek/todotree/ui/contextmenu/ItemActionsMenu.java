package igrek.todotree.ui.contextmenu;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.controller.ItemActionsController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeClipboardManager;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.ui.errorhandling.UIErrorHandler;

public class ItemActionsMenu {
	
	private View view;
	private int position;
	
	@Inject
	Activity activity;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	TreeClipboardManager treeClipboardManager;
	
	public ItemActionsMenu(View view, int position) {
		this.view = view;
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
		
		// TODO dynamic action names and execution based on app state (selection, empty clipboard)
		
		actions.add(new ItemAction("Select") {
			@Override
			public void execute() {
				new ItemActionsController().actionSelect(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Edit") {
			@Override
			public void execute() {
				new ItemActionsController().actionEdit(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Add above") {
			@Override
			public void execute() {
				new ItemActionsController().actionAddAbove(position);
			}
		});
		
		actions.add(new ItemAction("Cut") {
			@Override
			public void execute() {
				new ItemActionsController().actionCut(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Copy") {
			@Override
			public void execute() {
				new ItemActionsController().actionCopy(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Paste above") {
			@Override
			public void execute() {
				new ItemActionsController().actionPasteAbove(position);
			}
		});
		
		actions.add(new ItemAction("Paste as link") {
			@Override
			public void execute() {
				new ItemActionsController().actionPasteAboveAsLink(position);
			}
			
			@Override
			public boolean isVisible() {
				return !treeClipboardManager.isClipboardEmpty();
			}
		});
		
		actions.add(new ItemAction("Remove") {
			@Override
			public void execute() {
				new ItemActionsController().actionRemove(position);
			}
			
			@Override
			public boolean isVisible() {
				return treeManager.isPositionAtItem(position);
			}
		});
		
		actions.add(new ItemAction("Select all") {
			@Override
			public void execute() {
				new ItemActionsController().actionSelectAll(position);
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
