package igrek.todotree.logic.tree;

public interface TreeItemListener {
    void onTreeItemEditClicked(int position, TreeItem item);

    void onTreeItemRemoveClicked(int position, TreeItem item);
}
