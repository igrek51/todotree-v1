package igrek.todotree.services.tree.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import igrek.todotree.exceptions.DeserializationFailedException;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.CheckboxTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.RootTreeItem;
import igrek.todotree.model.treeitem.SeparatorTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;

public class JsonTreeSerializer {
	
	Pattern singleItemPattern;
	Pattern multiItemPattern;
	Pattern nameValuePattern;
	
	final String CLOSING_BRACKET = "]},";
	
	public JsonTreeSerializer() {
		singleItemPattern = Pattern.compile("^\\{ \"type\": \"([\\w/]+)\"(, \"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\")* \\},$");
		multiItemPattern = Pattern.compile("^\\{ \"type\": \"([\\w/]+)\"(, \"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\")*, \"items\": \\[$");
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
		} else if (item instanceof CheckboxTreeItem) {
			serializeAttribute(output, "checked", ((CheckboxTreeItem) item).isChecked() ? "true" : "false");
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
		s = s.replace("\\", "\\\\"); // escape \
		s = s.replace("\"", "\\\""); // escape "
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
		String headerLine = lines.get(0).getIndentedLine();
		
		List<ItemAttribute> attributes = extractAttributes(headerLine);
		if (attributes.isEmpty())
			throw new DeserializationFailedException("no attributes in header");
		
		AbstractTreeItem newItem = buildTreeItem(attributes);
		
		if (lines.size() == 1) { // no children: { "type": "text", "name": "dupa" },
			if (!isHeaderMatchingSingleItem(headerLine))
				throw new DeserializationFailedException("invalid header format for single item: " + headerLine);
			
		} else { // with children: { "type": "text", "name": "Dupa", "items": [
			if (!isHeaderMatchingMultiItem(headerLine))
				throw new DeserializationFailedException("invalid header format for item with children: " + headerLine);
			
			if (lines.size() <= 2)
				throw new DeserializationFailedException("Insufficient children lines");
			
			// last line of part must be closing bracket
			if (!lines.get(lines.size() - 1).getIndentedLine().equals(CLOSING_BRACKET))
				throw new DeserializationFailedException("No matching closing bracket found");
			
			// split all child lines to parts
			List<IndentedLine> childLines = lines.subList(1, lines.size() - 1); // assert size() >= 1
			List<List<IndentedLine>> childParts = splitChildParts(childLines, lines.get(0)
					.getIndentation() + 1);
			
			if (childParts.isEmpty())
				throw new DeserializationFailedException("Child parts are empty");
			
			// deserialize all child parts and add them to this item
			for (List<IndentedLine> childPart : childParts) {
				deserializeItem(newItem, childPart);
			}
		}
		
		newItem.setParent(parent);
		parent.add(newItem);
	}
	
	boolean isHeaderMatchingSingleItem(String line) {
		return singleItemPattern.matcher(line).find();
	}
	
	boolean isHeaderMatchingMultiItem(String line) {
		return multiItemPattern.matcher(line).find();
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
	
	private AbstractTreeItem buildTreeItem(List<ItemAttribute> attributes) throws DeserializationFailedException {
		ItemAttribute typeAttr = attributes.get(0);
		if (!typeAttr.getName().equals("type"))
			throw new DeserializationFailedException("first attr not a type attribute");
		
		// build item based on type and attrs
		String type = typeAttr.getValue();
		switch (type) {
			case "/": {
				return new RootTreeItem();
			}
			case "text": {
				String name = getAttributeValue(attributes, "name");
				return new TextTreeItem(null, name);
			}
			case "separator": {
				return new SeparatorTreeItem(null);
			}
			case "link": {
				String name = getOptionalAttributeValue(attributes, "name");
				String targetPath = getAttributeValue(attributes, "target");
				return new LinkTreeItem(null, targetPath, name);
			}
			case "checkbox": {
				String name = getAttributeValue(attributes, "name");
				String checkedStr = getAttributeValue(attributes, "checked");
				return new CheckboxTreeItem(null, name, checkedStr.equals("true"));
			}
			default:
				throw new DeserializationFailedException("Unknown item type: " + type);
		}
	}
	
	private String getAttributeValue(List<ItemAttribute> attributes, String name) throws DeserializationFailedException {
		String value = getOptionalAttributeValue(attributes, name);
		if (value == null)
			throw new DeserializationFailedException("Attribute not found: " + name);
		return value;
	}
	
	private String getOptionalAttributeValue(List<ItemAttribute> attributes, String name) throws DeserializationFailedException {
		for (ItemAttribute attribute : attributes) {
			if (attribute.getName().equals(name))
				return attribute.getValue();
		}
		return null;
	}
	
	private List<List<IndentedLine>> splitChildParts(List<IndentedLine> lines, int headerIndentation) throws DeserializationFailedException {
		List<List<IndentedLine>> childParts = new ArrayList<>();
		// first line should be the header line
		if (lines.get(0).getIndentation() != headerIndentation)
			throw new DeserializationFailedException("invalid indentation of first child header");
		
		for (int i = 0; i < lines.size(); i++) {
			IndentedLine current = lines.get(i);
			if (current.getIndentation() == headerIndentation) {
				if (current.getIndentedLine().endsWith("[")) { // item with children
					int closingIndex = findClosingBracket(lines, i, headerIndentation);
					childParts.add(lines.subList(i, closingIndex + 1));
					i = closingIndex;
				} else { //single item
					childParts.add(lines.subList(i, i + 1));
				}
			}
		}
		
		return childParts;
	}
	
	private int findClosingBracket(List<IndentedLine> lines, int startIndex, int headerIndentation) throws DeserializationFailedException {
		for (int j = startIndex + 1; j < lines.size(); j++) {
			IndentedLine line = lines.get(j);
			if (line.getIndentation() == headerIndentation && line.getIndentedLine()
					.equals(CLOSING_BRACKET)) {
				return j;
			}
		}
		throw new DeserializationFailedException("No matching closing bracket found");
	}
	
}
