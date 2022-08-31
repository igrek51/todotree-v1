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

    fun setItemSelected(position: Int, selectedState: Boolean) {
        if (!isAnythingSelected) {
            startSelectionMode()
        }
        if (selectedState) {
            if (!isItemSelected(position)) {
                selectedItems!!.add(position)
            }
        } else {
            if (isItemSelected(position)) {
                selectedItems!!.remove(Integer.valueOf(position))
                if (selectedItems!!.isEmpty()) {
                    selectedItems = null
                }
            }
        }
    }

    private fun isItemSelected(position: Int): Boolean {
        for (pos in selectedItems!!) {
            if (pos == position) {
                return true
            }
        }
        return false
    }

    fun toggleItemSelected(position: Int) {
        setItemSelected(position, !isItemSelected(position))
    }
}