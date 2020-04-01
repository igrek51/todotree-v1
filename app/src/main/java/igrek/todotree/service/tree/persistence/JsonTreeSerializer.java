package igrek.todotree.service.tree.persistence;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.CheckboxTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.domain.treeitem.RemoteTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;

class JsonTreeSerializer {
	
	String serializeTree(AbstractTreeItem root) {
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
		if (item instanceof RemoteTreeItem) {
			serializeAttribute(output, "name", item.getDisplayName());
		} else if (item instanceof TextTreeItem) {
			serializeAttribute(output, "name", item.getDisplayName());
		} else if (item instanceof LinkTreeItem) {
			LinkTreeItem linkItem = (LinkTreeItem) item;
			serializeAttribute(output, "target", linkItem.getTargetPath());
			if (linkItem.hasCustomName()) {
				serializeAttribute(output, "name", linkItem.getCustomName());
			}
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
	
}
