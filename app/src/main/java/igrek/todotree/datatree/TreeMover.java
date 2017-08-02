package igrek.todotree.datatree;


import igrek.todotree.datatree.item.TreeItem;

public class TreeMover {
	
	private void replace(TreeItem parent, int pos1, int pos2) {
		if (pos1 == pos2)
			return;
		if (pos1 < 0 || pos2 < 0)
			throw new IllegalArgumentException("position < 0");
		if (pos1 >= parent.size() || pos2 >= parent.size()) {
			throw new IllegalArgumentException("position >= size");
		}
		TreeItem item1 = parent.getChild(pos1);
		TreeItem item2 = parent.getChild(pos2);
		//wstawienie na pos1
		parent.remove(pos1);
		parent.add(pos1, item2);
		//wstawienie na pos2
		parent.remove(pos2);
		parent.add(pos2, item1);
	}
	
	/**
	 * przesuwa element z pozycji o jedną pozycję w górę
	 * @param parent   przodek przesuwanego elementu
	 * @param position pozycja elementu przed przesuwaniem
	 */
	private void moveUp(TreeItem parent, int position) {
		if (position <= 0)
			return;
		replace(parent, position, position - 1);
	}
	
	/**
	 * przesuwa element z pozycji o jedną pozycję w dół
	 * @param parent   przodek przesuwanego elementu
	 * @param position pozycja elementu przed przesuwaniem
	 */
	private void moveDown(TreeItem parent, int position) {
		if (position >= parent.size() - 1)
			return;
		replace(parent, position, position + 1);
	}
	
	/**
	 * przesuwa element z pozycji o określoną liczbę pozycji
	 * @param parent   przodek przesuwanego elementu
	 * @param position pozycja elementu przed przesuwaniem
	 * @param step     liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
	 * @return nowa pozycja elementu
	 */
	public int move(TreeItem parent, int position, int step) {
		int targetPosition = position + step;
		if (targetPosition < 0)
			targetPosition = 0;
		if (targetPosition >= parent.size())
			targetPosition = parent.size() - 1;
		while (position < targetPosition) {
			moveDown(parent, position);
			position++;
		}
		while (position > targetPosition) {
			moveUp(parent, position);
			position--;
		}
		return targetPosition;
	}
	
}
