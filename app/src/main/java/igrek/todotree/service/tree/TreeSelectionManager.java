package igrek.todotree.service.tree;


import java.util.TreeSet;

public class TreeSelectionManager {
	
	private TreeSet<Integer> selectedPositions = null;
	
	public TreeSet<Integer> getSelectedItems() {
		return selectedPositions;
	}
	
	public TreeSet<Integer> getSelectedItemsNotNull() {
		return selectedPositions != null ? selectedPositions : new TreeSet<Integer>();
	}
	
	public int getSelectedItemsCount() {
		if (selectedPositions == null)
			return 0;
		return selectedPositions.size();
	}
	
	public boolean isAnythingSelected() {
		return selectedPositions != null && selectedPositions.size() > 0;
	}
	
	public void startSelectionMode() {
		selectedPositions = new TreeSet<>();
	}
	
	public void cancelSelectionMode() {
		selectedPositions = null;
	}
	
	public void setItemSelected(int position, boolean selectedState) {
		if (!isAnythingSelected()) {
			startSelectionMode();
		}
		if (selectedState) {
			if (!isItemSelected(position)) {
				selectedPositions.add(position);
			}
		} else {
			if (isItemSelected(position)) {
				selectedPositions.remove(Integer.valueOf(position));
				if (selectedPositions.isEmpty()) {
					selectedPositions = null;
				}
			}
		}
	}
	
	private boolean isItemSelected(int position) {
		for (Integer pos : selectedPositions) {
			if (pos == position) {
				return true;
			}
		}
		return false;
	}
	
	public void toggleItemSelected(int position) {
		setItemSelected(position, !isItemSelected(position));
	}
	
}
