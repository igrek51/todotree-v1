package igrek.todotree.services.datatree;


import java.util.ArrayList;
import java.util.List;

public class TreeSelectionManager {
	
	private List<Integer> selectedPositions = null;
	
	public List<Integer> getSelectedItems() {
		return selectedPositions;
	}
	
	public int getSelectedItemsCount() {
		if (selectedPositions == null)
			return 0;
		return selectedPositions.size();
	}
	
	public boolean isSelectionMode() {
		return selectedPositions != null && selectedPositions.size() > 0;
	}
	
	public void startSelectionMode() {
		selectedPositions = new ArrayList<>();
	}
	
	public void cancelSelectionMode() {
		selectedPositions = null;
	}
	
	public void setItemSelected(int position, boolean selectedState) {
		if (!isSelectionMode()) {
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
