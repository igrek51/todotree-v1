package igrek.todotree.service.tree.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.List;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.CheckboxTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.domain.treeitem.RootTreeItem;
import igrek.todotree.domain.treeitem.SeparatorTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;
import igrek.todotree.exceptions.DeserializationFailedException;

public class GsonTreeDeserializer {
	
	private Gson gson;
	
	GsonTreeDeserializer() {
		GsonBuilder gsonb = new GsonBuilder();
		gson = gsonb.create();
	}
	
	AbstractTreeItem deserializeTree(String data) throws DeserializationFailedException {
		// trim comma at the end
		data = data.trim();
		if (data.endsWith(","))
			data = data.substring(0, data.length() - 1);
		
		try {
			JsonItem rootTreeItem = gson.fromJson(data, JsonItem.class);
			
			if (rootTreeItem == null)
				throw new DeserializationFailedException("root tree item is null");
			
			return mapJsonItemToTreeItem(rootTreeItem);
			
		} catch (JsonSyntaxException e) {
			throw new DeserializationFailedException(e.getMessage());
		}
	}
	
	private AbstractTreeItem mapJsonItemToTreeItem(JsonItem jsonItem) throws DeserializationFailedException {
		AbstractTreeItem treeItem;
		
		if (jsonItem.type == null)
			throw new DeserializationFailedException("property 'type' not found");
		
		switch (jsonItem.type) {
			case "/": {
				treeItem = new RootTreeItem();
				break;
			}
			case "text": {
				if (jsonItem.name == null)
					throw new DeserializationFailedException("property 'name' not found");
				
				treeItem = new TextTreeItem(null, jsonItem.name);
				break;
			}
			case "separator": {
				treeItem = new SeparatorTreeItem(null);
				break;
			}
			case "link": {
				// name is optional, target required
				if (jsonItem.target == null)
					throw new DeserializationFailedException("property 'target' not found");
				
				treeItem = new LinkTreeItem(null, jsonItem.target, jsonItem.name);
				break;
			}
			case "checkbox": {
				if (jsonItem.name == null)
					throw new DeserializationFailedException("property 'name' not found");
				
				boolean checked = "true".equals(jsonItem.checked);
				treeItem = new CheckboxTreeItem(null, jsonItem.name, checked);
				break;
			}
			default:
				throw new DeserializationFailedException("Unknown item type: " + jsonItem.type);
		}
		
		if (jsonItem.items != null) {
			for (JsonItem jsonChild : jsonItem.items) {
				if (jsonChild != null)
					treeItem.add(mapJsonItemToTreeItem(jsonChild));
			}
		}
		
		return treeItem;
	}
	
	private class JsonItem {
		
		String type;
		String name;
		String target;
		String checked;
		List<JsonItem> items;
		
	}
}
