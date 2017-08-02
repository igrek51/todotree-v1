package igrek.todotree.datatree;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.datatree.serializer.TreeSerializer;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.logger.Logs;
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
	
	// TODO move to EDIT item GUI
	private Integer newItemPosition;
	
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
	
	public void setNewItemPosition(Integer newItemPosition) {
		this.newItemPosition = newItemPosition;
	}
	
	public Integer getNewItemPosition() {
		return newItemPosition;
	}
	
	public void addToCurrent(Integer position, String content) {
		if (position == null) {
			position = currentItem.size();
		}
		currentItem.add(position, content);
	}
	
	public void addToCurrent(Integer position, TreeItem item) {
		if (position == null) {
			position = currentItem.size();
		}
		currentItem.add(position, item);
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
	
	public void goInto(int childIndex) {
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
	
	public TreeSelectionManager selectionManager() {
		return treeSelectionManager;
	}
	
	public TreeClipboardManager clipboardManager() {
		return treeClipboardManager;
	}
	
	public TreeMover mover() {
		return treeMover;
	}
	
	public BigDecimal sumSelected() {
		return numericAdder.sumSelected(treeSelectionManager.getSelectedItems(), getCurrentItem());
	}
}
