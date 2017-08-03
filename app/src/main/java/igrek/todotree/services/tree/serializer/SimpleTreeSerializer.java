package igrek.todotree.services.tree.serializer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import igrek.todotree.exceptions.NoMatchingBracketException;
import igrek.todotree.model.treeitem.TreeItem;

public class SimpleTreeSerializer {
	
	public TreeItem loadTree(String data) throws ParseException {
		TreeItem rootItem = new TreeItem(null, "/");
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
	
	
	/**
	 * loads items ładuje zawartość elementów z tekstowych wierszy i dodaje do wybranego elementu
	 * @param parent element, do którego dodane odczytane potomki
	 * @param lines  lista wierszy, z których zostaną dodane elementy
	 * @throws ParseException in case of invalid format
	 */
	private void loadTreeItems(TreeItem parent, List<String> lines) throws ParseException {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			switch (line) {
				case "{":
					try {
						int closingBracketIndex = findClosingBracket(lines, i);
						//jeśli cokolwiek jest w środku bloku
						if (closingBracketIndex - i >= 2) {
							List<String> subLines = lines.subList(i + 1, closingBracketIndex);
							TreeItem lastChild = parent.getLastChild();
							if (lastChild == null) {
								throw new ParseException("No matching element before opening bracket", i);
							}
							loadTreeItems(lastChild, subLines);
						}
						//przeskoczenie już przeanalizowanych wierszy
						i = closingBracketIndex;
					} catch (NoMatchingBracketException ex) {
						throw new ParseException("No matching closing bracket", i);
					}
					break;
				case "}":
					//nawiasy domykające zostały już przeanalizowane
					throw new ParseException("Redundant closing bracket", i);
				default:
					parent.add(line);
					break;
			}
		}
	}
	
	/**
	 * @param lines      lista wierszy
	 * @param startIndex indeks wiersza będącego klamrą otwierającą
	 * @return indeks wiersza będącego pasującą klamrą zamykającą
	 * @throws NoMatchingBracketException jeśli nie znaleziono poprawnego nawiasu domykającego
	 */
	private int findClosingBracket(List<String> lines, int startIndex) throws NoMatchingBracketException {
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
		throw new NoMatchingBracketException();
	}
	
	private void saveTreeItems(TreeItem parent, int level, StringBuilder output) {
		StringBuilder indentBuilder = new StringBuilder();
		for (int i = 0; i < level; i++)
			indentBuilder.append("\t");
		String indents = indentBuilder.toString();
		
		output.append(indents);
		output.append(parent.getContent());
		output.append("\n");
		if (!parent.isEmpty()) {
			output.append(indents);
			output.append("{\n");
			for (TreeItem item : parent.getChildren()) {
				saveTreeItems(item, level + 1, output);
			}
			output.append(indents);
			output.append("}\n");
		}
	}
	
	public String saveTree(TreeItem root) {
		StringBuilder output = new StringBuilder();
		for (TreeItem child : root.getChildren()) {
			saveTreeItems(child, 0, output);
		}
		return output.toString();
	}
	
}
