package igrek.todotree.services.tree.serializer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.RootTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;

public class SimpleTreeSerializer {
	
	public static AbstractTreeItem loadTree(String data) throws ParseException {
		RootTreeItem rootItem = new RootTreeItem();
		if (!data.isEmpty()) {
			//wyłuskanie wierszy
			String[] lines = data.split("\n");
			List<String> linesList = new ArrayList<>();
			//obcięcie białych znaków
			for (String line : lines) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty()) {
					linesList.add(trimmed);
				}
			}
			loadTreeItems(rootItem, linesList);
		}
		return rootItem;
	}
	
	private static void loadTreeItems(AbstractTreeItem parent, List<String> lines) throws ParseException {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			switch (line) {
				case "{":
					try {
						int closingBracketIndex = findClosingBracket(lines, i);
						//jeśli cokolwiek jest w środku bloku
						if (closingBracketIndex - i >= 2) {
							List<String> subLines = lines.subList(i + 1, closingBracketIndex);
							AbstractTreeItem lastChild = parent.getLastChild();
							if (lastChild == null) {
								throw new ParseException("No matching element before opening bracket", i);
							}
							loadTreeItems(lastChild, subLines);
						}
						//przeskoczenie już przeanalizowanych wierszy
						i = closingBracketIndex;
					} catch (RuntimeException ex) {
						throw new ParseException("No matching closing bracket", i);
					}
					break;
				case "}":
					//nawiasy domykające zostały już przeanalizowane
					throw new ParseException("Redundant closing bracket", i);
				default:
					parent.add(new TextTreeItem(parent, line));
					break;
			}
		}
	}
	
	private static int findClosingBracket(List<String> lines, int startIndex) {
		int bracketDepth = 1;
		for (int j = startIndex + 1; j < lines.size(); j++) {
			String line = lines.get(j);
			if (line.equals("{")) {
				bracketDepth++;
			} else if (line.equals("}")) {
				bracketDepth--;
				if (bracketDepth == 0) {
					return j;
				}
			}
		}
		throw new RuntimeException("NoMatchingBracket");
	}
	
}
