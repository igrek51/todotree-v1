package igrek.todotree.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Set;

import igrek.todotree.R;
import igrek.todotree.controller.MainController;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.ui.edititem.EditItemGUI;
import igrek.todotree.ui.errorcheck.SafeClickListener;
import igrek.todotree.ui.treelist.TreeListView;

public class GUI extends BaseGUI {
	
	private ActionBar actionBar;
	private TreeListView itemsListView;
	private EditItemGUI editItemGUI;
	
	public GUI(AppCompatActivity activity) {
		super(activity);
	}
	
	public void lazyInit() {
		
		activity.setContentView(R.layout.activity_main);
		
		//toolbar
		Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		actionBar = activity.getSupportActionBar();
		showBackButton(true);
		toolbar1.setNavigationOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				new MainController().backClicked();
			}
		});
		
		//  główna zawartość
		mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
	}
	
	private void showBackButton(boolean show) {
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(show);
			actionBar.setDisplayShowHomeEnabled(show);
		}
	}
	
	public void showItemsList(final AbstractTreeItem currentItem) {
		setOrientationPortrait();
		
		View itemsListLayout = setMainContentLayout(R.layout.items_list);
		
		itemsListView = (TreeListView) itemsListLayout.findViewById(R.id.treeItemsList);
		itemsListView.init(activity);
		itemsListView.setItems(currentItem.getChildren());
		
		updateItemsList(currentItem, null);
	}
	
	public void showEditItemPanel(final AbstractTreeItem item, AbstractTreeItem parent) {
		showBackButton(true);
		if (item instanceof TextTreeItem) {
			editItemGUI = new EditItemGUI(this, (TextTreeItem) item, parent);
		}
	}
	
	public View showExitScreen() {
		return setMainContentLayout(R.layout.exit_screen);
	}
	
	public void updateItemsList(AbstractTreeItem currentItem, Set<Integer> selectedPositions) {
		//tytuł gałęzi
		StringBuilder sb = new StringBuilder(currentItem.getDisplayName());
		if (!currentItem.isEmpty()) {
			sb.append(" [");
			sb.append(currentItem.size());
			sb.append("]");
		}
		setTitle(sb.toString());
		
		// back button visiblity
		showBackButton(currentItem.getParent() != null);
		
		//lista elementów
		itemsListView.setItemsAndSelected(currentItem.getChildren(), selectedPositions);
	}
	
	public void scrollToItem(int position) {
		itemsListView.scrollToItem(position);
	}
	
	public void scrollToPosition(int y) {
		itemsListView.scrollToPosition(y);
	}
	
	public void scrollToBottom() {
		itemsListView.scrollToBottom();
	}
	
	public void hideSoftKeyboard() {
		editItemGUI.hideKeyboards();
	}
	
	public boolean editItemBackClicked() {
		return editItemGUI.editItemBackClicked();
	}
	
	public void setTitle(String title) {
		actionBar.setTitle(title);
	}
	
	public Integer getCurrentScrollPos() {
		return itemsListView.getCurrentScrollPosition();
	}
	
	public void requestSaveEditedItem() {
		editItemGUI.requestSaveEditedItem();
	}
	
	public void rotateScreen() {
		int orientation = activity.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
	}
	
	private void setOrientationPortrait() {
		int orientation = activity.getResources().getConfiguration().orientation;
		if (orientation != Configuration.ORIENTATION_PORTRAIT) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	
	public void quickInsertRange() {
		if (editItemGUI != null) {
			editItemGUI.quickInsertRange();
		}
	}
}
