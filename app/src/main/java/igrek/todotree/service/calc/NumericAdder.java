package igrek.todotree.service.calc;


import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import igrek.todotree.domain.treeitem.AbstractTreeItem;

public class NumericAdder {
	
	public BigDecimal calculateSum(Set<Integer> selectedPositions, AbstractTreeItem currentItem) throws NumberFormatException {
		BigDecimal sum = new BigDecimal(0);
		for (Integer selectedPos : selectedPositions) {
			AbstractTreeItem selectedItem = currentItem.getChild(selectedPos);
			BigDecimal itemValue = getItemNumericValue(selectedItem.getDisplayName());
			if (itemValue != null) {
				sum = sum.add(itemValue);
			}
		}
		return sum;
	}
	
	private BigDecimal getItemNumericValue(String content) throws NumberFormatException {
		
		String valueStr = null;
		
		content = content.replace(',', '.');
		
		Pattern pattern = Pattern.compile("^(-? ?\\d+(\\.\\d+)?)(.*?)$");
		Matcher matcher = pattern.matcher(content);
		if (matcher.matches()) {
			valueStr = matcher.group(1);
			valueStr = valueStr.replaceAll(" ", "");
		} else {
			pattern = Pattern.compile("^(.*?)(-? ?\\d+(\\.\\d+)?)$");
			matcher = pattern.matcher(content);
			if (matcher.matches()) {
				valueStr = matcher.group(2);
				valueStr = valueStr.replaceAll(" ", "");
			}
		}
		
		if (valueStr != null) {
			try {
				return new BigDecimal(valueStr);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Invalid number format:\n" + valueStr);
			}
		} else {
			throw new NumberFormatException("No number value:\n" + content);
		}
	}
}
