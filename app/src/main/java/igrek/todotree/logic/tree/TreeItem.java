package igrek.todotree.logic.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeItem {

    private String content = "";
    private List<TreeItem> children;
    private TreeItem parent = null;

    public TreeItem(TreeItem parent) {
        children = new ArrayList<>();
        this.parent = parent;
    }

    public TreeItem(TreeItem parent, String content) {
        this(parent);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<TreeItem> getChildren() {
        return children;
    }

    public TreeItem getParent() {
        return parent;
    }

    public TreeItem getChild(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index < 0");
        }
        if (index >= children.size()) {
            throw new IndexOutOfBoundsException("index > size = " + children.size());
        }
        return children.get(index);
    }

    public TreeItem getLastChild() {
        if (children.isEmpty()) return null;
        return children.get(children.size() - 1);
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void add(TreeItem newItem) {
        children.add(newItem);
    }

    public void add(int location, TreeItem newItem) {
        children.add(location, newItem);
    }

    public TreeItem add(String content) {
        TreeItem newItem = new TreeItem(this, content);
        children.add(newItem);
        return newItem;
    }

    public void remove(int location) {
        children.remove(location);
    }

    public boolean remove(TreeItem item) {
        return children.remove(item);
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return content;
        } else {
            return content + " [" + children.size() + "]";
        }
    }
}
