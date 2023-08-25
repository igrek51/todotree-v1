package igrek.todotree.service.tree

import java.util.TreeSet

class TreeSelectionManager {

    var selectedItems: TreeSet<Int>? = null
        private set
    val selectedItemsNotNull: TreeSet<Int>
        get() = if (selectedItems != null) selectedItems!! else TreeSet()
    val selectedItemsCount: Int
        get() = if (selectedItems == null) 0 else selectedItems!!.size
    val isAnythingSelected: Boolean
        get() = selectedItems != null && selectedItems!!.size > 0

    fun startSelectionMode() {
        selectedItems = TreeSet()
    }

    fun cancelSelectionMode() {
        selectedItems = null
    }

    fun setItemSelected(position: Int, selectedState: Boolean): Boolean {
        // Return true if all elements has to be refreshed
        var refreshAll = false
        if (!isAnythingSelected) {
            startSelectionMode()
            refreshAll = true
        }
        if (selectedState) {
            if (!isItemSelected(position)) {
                selectedItems?.add(position)
            }
        } else {
            if (isItemSelected(position)) {
                selectedItems?.remove(Integer.valueOf(position))
                if (selectedItems.isNullOrEmpty()) {
                    selectedItems = null
                    refreshAll = true
                }
            }
        }
        return refreshAll
    }

    private fun isItemSelected(position: Int): Boolean {
        for (pos in selectedItems!!) {
            if (pos == position) {
                return true
            }
        }
        return false
    }

    fun toggleItemSelected(position: Int): Boolean {
        return setItemSelected(position, !isItemSelected(position))
    }
}