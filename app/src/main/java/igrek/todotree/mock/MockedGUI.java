package igrek.todotree.mock;


import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;
import java.util.Set;

import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.ui.GUI;

public class MockedGUI extends GUI {
	
	public MockedGUI(AppCompatActivity activity) {
		super(activity);
	}
	
	@Override
	public void lazyInit() {
	}
	
	@Override
	public void showItemsList(AbstractTreeItem currentItem) {
		updateItemsList(currentItem, null, null);
	}
	
	@Override
	public void showEditItemPanel(AbstractTreeItem item, AbstractTreeItem parent) {
	}
	
	@Override
	public View showExitScreen() {
		return null;
	}
	
	@Override
	public void updateItemsList(AbstractTreeItem currentItem, List<AbstractTreeItem> items, Set<Integer> selectedPositions) {
	}
	
	@Override
	public void scrollToItem(int itemIndex) {
	}
	
	@Override
	public void scrollToItem(Integer y, int itemIndex) {
	}
	
	@Override
	public void scrollToPosition(int y) {
	}
	
	@Override
	public void scrollToBottom() {
	}
	
	@Override
	public void hideSoftKeyboard() {
	}
	
	@Override
	public boolean editItemBackClicked() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setTitle(String title) {
	}
	
	@Override
	public Integer getCurrentScrollPos() {
		return 0;
	}
	
	@Override
	public void requestSaveEditedItem() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void rotateScreen() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void quickInsertRange() {
		throw new UnsupportedOperationException();
	}
}
