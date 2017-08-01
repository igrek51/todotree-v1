package igrek.todotree.services.datatree;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.datatree.serializer.TreeSerializer;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.preferences.Preferences;

//TODO RESPONSIBILITY separation
public class TreeManager {
	
	private FilesystemService filesystem;
	private Preferences preferences;
	private TreeSerializer treeSerializer;
	
	private TreeItem rootItem;
	private TreeItem currentItem;
	private Integer newItemPosition;
	
	private TreeScrollStore scrollStore;
	private NumericAdder numericAdder = new NumericAdder();
	private TreeClipboardManager treeClipboardManager = new TreeClipboardManager();
	private TreeSelectionManager treeSelectionManager = new TreeSelectionManager();
	private TreeMover treeMover = new TreeMover();
	
	public TreeManager(FilesystemService filesystem, Preferences preferences, TreeSerializer treeSerializer) {
		this.filesystem = filesystem;
		this.preferences = preferences;
		this.treeSerializer = treeSerializer;
		reset();
	}
	
	public void reset() {
		rootItem = new TreeItem(null, "/");
		currentItem = rootItem;
		scrollStore = new TreeScrollStore();
	}
	
	private TreeItem getRootItem() {
		return rootItem;
	}
	
	private void setRootItem(TreeItem rootItem) {
		this.rootItem = rootItem;
		this.currentItem = rootItem;
	}
	
	public TreeItem getCurrentItem() {
		return currentItem;
	}
	
	public void setEditItem() {
		this.newItemPosition = null;
	}
	
	public void setNewItemPosition(Integer newItemPosition) {
		this.newItemPosition = newItemPosition;
	}
	
	public Integer getNewItemPosition() {
		return newItemPosition;
	}
	
	//  NAWIGACJA
	
	public void goUp() throws NoSuperItemException {
		if (currentItem == rootItem) {
			throw new NoSuperItemException();
		} else if (currentItem.getParent() == null) {
			throw new IllegalStateException("parent = null. This should not happen");
		} else {
			currentItem = currentItem.getParent();
		}
	}
	
	public void goInto(int childIndex, Integer scrollPos) {
		if (scrollPos != null) {
			scrollStore.storeScrollPosition(currentItem, scrollPos);
		}
		TreeItem item = currentItem.getChild(childIndex);
		goTo(item);
	}
	
	private void goTo(TreeItem child) {
		currentItem = child;
	}
	
	public void addToCurrent(TreeItem newItem) {
		currentItem.add(newItem);
	}
	
	//  ZAPIS / ODCZYT Z PLIKU
	
	public void loadRootTree() {
		
		filesystem.mkdirIfNotExist(filesystem.pathSD().toString());
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		Logs.info("Loading database from file: " + dbFilePath.toString());
		if (!filesystem.exists(dbFilePath.toString())) {
			Logs.warn("Database file does not exist. Default empty database created.");
			return;
		}
		try {
			String fileContent = filesystem.openFileString(dbFilePath.toString());
			TreeItem rootItem = treeSerializer.loadTree(fileContent);
			setRootItem(rootItem);
			Logs.info("Database loaded.");
		} catch (IOException | ParseException e) {
			Logs.error(e);
		}
	}
	
	public void saveRootTree() {
		//TODO: wyjście bez zapisywania bazy jeśli nie było zmian
		
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		//        Logs.info("Zapisywanie bazy danych do pliku: " + dbFilePath.toString());
		try {
			String output = treeSerializer.saveTree(getRootItem());
			filesystem.saveFile(dbFilePath.toString(), output);
		} catch (IOException e) {
			Logs.error(e);
		}
	}
	
	/**
	 * obcięcie białych znaków na początku i na końcu, usunięcie niedozwolonych znaków
	 * @param content zawartość elementu
	 * @return zawartość z obciętymi znakami
	 */
	public String trimContent(String content) {
		final String WHITE_CHARS = " ";
		final String INVALID_CHARS = "{}[]\n\t";
		//usunięcie niedozwolonych znaków ze środka
		for (int i = 0; i < content.length(); i++) {
			if (isCharInSet(content.charAt(i), INVALID_CHARS)) {
				content = content.substring(0, i) + content.substring(i + 1);
				i--;
			}
		}
		//obcinanie białych znaków na początku
		while (content.length() > 0 && isCharInSet(content.charAt(0), WHITE_CHARS)) {
			content = content.substring(1);
		}
		//obcinanie białych znaków na końcu
		while (content.length() > 0 && isCharInSet(content.charAt(content.length() - 1), WHITE_CHARS)) {
			content = content.substring(0, content.length() - 1);
		}
		return content;
	}
	
	private boolean isCharInSet(char c, String set) {
		for (int i = 0; i < set.length(); i++) {
			if (set.charAt(i) == c)
				return true;
		}
		return false;
	}
	
	
	public TreeSelectionManager selectionManager() {
		return treeSelectionManager;
	}
	
	public TreeClipboardManager clipboardManager() {
		return treeClipboardManager;
	}
	
	public TreeScrollStore scrollStore() {
		return scrollStore;
	}
	
	public TreeMover mover() {
		return treeMover;
	}
	
	public BigDecimal sumSelected() {
		return numericAdder.sumSelected(treeSelectionManager.getSelectedItems(), getCurrentItem());
	}
}
