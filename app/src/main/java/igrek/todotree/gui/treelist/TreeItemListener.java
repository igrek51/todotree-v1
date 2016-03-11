package igrek.todotree.gui.treelist;

import igrek.todotree.logic.datatree.TreeItem;

public interface TreeItemListener {
    void onTreeItemEditClicked(int position, TreeItem item);

    void onTreeItemRemoveClicked(int position, TreeItem item);
}
