package igrek.todotree.model.treeitem;


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTreeItem {
	
	protected AbstractTreeItem parent = null;
	protected List<AbstractTreeItem> children;
	
	public AbstractTreeItem(AbstractTreeItem parent) {
		children = new ArrayList<>();
		this.parent = parent;
	}
	
	/**
	 * copy constructor, creates cloned item detached from parent
	 * @param from source item
	 */
	protected <T extends AbstractTreeItem> T copyChildren(T from) {
		this.parent = null;
		this.children = new ArrayList<>();
		for (AbstractTreeItem sourceChild : from.children) {
			AbstractTreeItem childCopy = sourceChild.clone();
			childCopy.parent = this;
			this.children.add(childCopy);
		}
		return (T) this;
	}
	
	public abstract AbstractTreeItem clone();
	
	public abstract String getDisplayName();
	
	public abstract String getTypeName();
	
	public List<AbstractTreeItem> getChildren() {
		return children;
	}
	
	public void setParent(AbstractTreeItem parent) {
		this.parent = parent;
	}
	
	public AbstractTreeItem getParent() {
		return parent;
	}
	
	public AbstractTreeItem getChild(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("index < 0");
		if (index >= children.size())
			throw new IndexOutOfBoundsException("index > size = " + children.size());
		return children.get(index);
	}
	
	private int getChildIndex(AbstractTreeItem child) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == child) {
				return i;
			}
		}
		return -1;
	}
	
	public int getIndexInParent() {
		if (parent == null)
			return -1;
		return parent.getChildIndex(this);
	}
	
	public AbstractTreeItem getLastChild() {
		if (children.isEmpty())
			return null;
		return children.get(children.size() - 1);
	}
	
	public AbstractTreeItem findChildByName(String name) {
		for (AbstractTreeItem child : children) {
			if (child.getDisplayName().equals(name))
				return child;
		}
		return null;
	}
	
	public int size() {
		return children.size();
	}
	
	public boolean isEmpty() {
		return children.isEmpty();
	}
	
	public <T extends AbstractTreeItem> void add(T newItem) {
		newItem.setParent(this);
		children.add(newItem);
	}
	
	public <T extends AbstractTreeItem> void add(int location, T newItem) {
		newItem.setParent(this);
		children.add(location, newItem);
	}
	
	public void remove(int location) {
		children.remove(location);
	}
	
	public boolean remove(AbstractTreeItem item) {
		return children.remove(item);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getTypeName());
		sb.append(": ");
		sb.append(getDisplayName());
		if (!children.isEmpty()) {
			sb.append(" [");
			sb.append(children.size());
			sb.append("]");
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
