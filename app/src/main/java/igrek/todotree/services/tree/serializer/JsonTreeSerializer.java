package igrek.todotree.services.tree.serializer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import igrek.todotree.exceptions.DeserializationFailedException;
import igrek.todotree.exceptions.NoMatchingBracketException;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.RootTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.model.treeitem.TreeItem;

public class JsonTreeSerializer {
	
	Pattern singleItemPattern;
	Pattern nameValuePattern;
	
	public JsonTreeSerializer() {
		singleItemPattern = Pattern.compile("^\\{ \"type\": \"(\\w+)\"(, \"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\")* \\},$");
		nameValuePattern = Pattern.compile("\"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\"");
	}
	
	public String serializeTree(AbstractTreeItem root) {
		StringBuilder output = new StringBuilder();
		serializeItem(output, root, 0);
		return output.toString();
	}
	
	private void serializeItem(StringBuilder output, AbstractTreeItem item, int indentLevel) {
		indent(output, indentLevel);
		// item type
		output.append("{ \"type\": \"");
		output.append(item.getTypeName());
		output.append("\"");
		// additional attributes
		serializeAttributes(output, item);
		// child items
		if (item.isEmpty()) {
			output.append(" },\n");
		} else {
			output.append(", \"items\": [\n");
			for (AbstractTreeItem child : item.getChildren()) {
				serializeItem(output, child, indentLevel + 1);
			}
			// end of children list
			indent(output, indentLevel);
			output.append("]},\n");
		}
	}
	
	private void indent(StringBuilder output, int indentLevel) {
		for (int i = 0; i < indentLevel; i++)
			output.append("\t");
	}
	
	private void serializeAttributes(StringBuilder output, AbstractTreeItem item) {
		if (item instanceof TextTreeItem) {
			serializeAttribute(output, "name", ((TextTreeItem) item).getDisplayName());
		} else if (item instanceof LinkTreeItem) {
			serializeAttribute(output, "target", ((LinkTreeItem) item).getTargetPath());
		}
	}
	
	private void serializeAttribute(StringBuilder output, String name, String value) {
		output.append(", \"");
		output.append(name);
		output.append("\": \"");
		output.append(escape(value));
		output.append("\"");
	}
	
	private String escape(String s) {
		s = s.replace("\"", "\\\""); // escape "
		s = s.replace("\\", "\\\\"); // escape \
		return s;
	}
	
	private String unescape(String s) {
		s = s.replace("\\\"", "\""); // unescape \"
		s = s.replace("\\\\", "\\"); // unescape \\
		return s;
	}
	
	
	public AbstractTreeItem deserializeTree(String data) throws DeserializationFailedException {
		RootTreeItem rootItem = new RootTreeItem();
		if (data.isEmpty())
			throw new DeserializationFailedException("empty data");
		
		String[] lines = data.split("\\n");
		List<IndentedLine> linesList = new ArrayList<>();
		//trim whitespaces and indents
		for (String unindentedLine : lines) {
			IndentedLine line = new IndentedLine(unindentedLine);
			if (!line.getIndentedLine().isEmpty()) {
				linesList.add(line);
			}
		}
		
		deserializeItem(rootItem, linesList);
		
		return rootItem.getChild(0); // extract real root loaded from file
	}
	
	private void deserializeItem(AbstractTreeItem parent, List<IndentedLine> lines) throws DeserializationFailedException {
		if (lines.isEmpty())
			throw new DeserializationFailedException("no lines for item");
		// first line - main attributes
		IndentedLine headerLine = lines.get(0);
		if (lines.size() == 1) {
			
			
			// no children: { "type": "text", "name": "dupa" },
			
			
		} else {
			// with children: { "type": "text", "name": "Dupa", "items": [
			
			// deserialize all children
		}
		
		AbstractTreeItem newItem = null;
		
		parent.add(newItem);
		
	}
	
	boolean isMatchingSingleItem(String line) {
		Matcher m = singleItemPattern.matcher(line);
		return m.find();
	}
	
	List<ItemAttribute> extractAttributes(String line) {
		List<ItemAttribute> attrs = new ArrayList<>();
		Matcher m = nameValuePattern.matcher(line);
		while (m.find()) {
			String name = unescape(m.group(1));
			String value = unescape(m.group(2));
			attrs.add(new ItemAttribute(name, value));
		}
		return attrs;
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
	
}
