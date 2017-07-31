package igrek.todotree.gui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.List;

import igrek.todotree.R;
import igrek.todotree.controller.LogicActionController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.services.datatree.TreeItem;

public class GUI extends BaseGUI {
	
	private ActionBar actionBar;
	private TreeListView itemsListView;
	private EditItemGUI editItemGUI;
	
	LogicActionController actionController;
	
	public GUI(AppCompatActivity activity) {
		super(activity);
	}
	
	public void lazyInit() {
		
		actionController = DaggerIOC.getAppComponent().getActionController();
		
		activity.setContentView(R.layout.activity_main);
		
		//toolbar
		Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		actionBar = activity.getSupportActionBar();
		showBackButton(true);
		toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionController.toolbarBackClicked();
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
	
	public void showItemsList(final TreeItem currentItem) {
		setOrientationPortrait();
		
		View itemsListLayout = setMainContentLayout(R.layout.items_list);
		
		itemsListView = (TreeListView) itemsListLayout.findViewById(R.id.treeItemsList);
		itemsListView.init(activity);
		itemsListView.setItems(currentItem.getChildren());
		
		updateItemsList(currentItem, null);
	}
	
	public EditItemGUI getEditItemGUI() {
		return editItemGUI;
	}
	
	public void showEditItemPanel(final TreeItem item, TreeItem parent) {
		showBackButton(true);
		editItemGUI = new EditItemGUI(this, item, parent);
	}
	
	public void showExitScreen() {
		
		View exitScreen = setMainContentLayout(R.layout.exit_screen);
		
		//TODO dalej jest zjebane
		final ViewTreeObserver vto = exitScreen.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						actionController.exitAppRequested();
					}
				}, 200);
			}
		});
	}
	
	public void updateItemsList(TreeItem currentItem, List<Integer> selectedPositions) {
		//tytuł gałęzi
		StringBuilder sb = new StringBuilder(currentItem.getContent());
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
}
