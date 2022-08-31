package igrek.todotree.service.calc

import igrek.todotree.domain.treeitem.AbstractTreeItem
import java.math.BigDecimal
import java.util.regex.Pattern

class NumericAdder {

    @Throws(NumberFormatException::class)
    fun calculateSum(selectedPositions: Set<Int>, currentItem: AbstractTreeItem): BigDecimal {
        var sum = BigDecimal(0)
        for (selectedPos in selectedPositions) {
            val selectedItem = currentItem.getChild(selectedPos)
            val itemValue = getItemNumericValue(selectedItem.displayName)
            sum = sum.add(itemValue)
        }
        return sum
    }

    @Throws(NumberFormatException::class)
    private fun getItemNumericValue(content: String): BigDecimal {
        var content = content
        var valueStr: String? = null
        content = content.replace(',', '.')
        var pattern = Pattern.compile("^(-? ?\\d+(\\.\\d+)?)(.*?)$")
        var matcher = pattern.matcher(content)
        if (matcher.matches()) {
            valueStr = matcher.group(1)
            valueStr = valueStr.replace(" ".toRegex(), "")
        } else {
            pattern = Pattern.compile("^(.*?)(-? ?\\d+(\\.\\d+)?)$")
            matcher = pattern.matcher(content)
            if (matcher.matches()) {
                valueStr = matcher.group(2)
                valueStr = valueStr.replace(" ".toRegex(), "")
            }
        }
        return if (valueStr != null) {
            try {
                BigDecimal(valueStr)
            } catch (e: NumberFormatException) {
                throw NumberFormatException("Invalid number format:\n$valueStr")
            }
        } else {
            throw NumberFormatException("No number value:\n$content")
        }
    }
}