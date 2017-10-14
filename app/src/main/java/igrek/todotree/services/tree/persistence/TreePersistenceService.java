package igrek.todotree.services.tree.persistence;


import igrek.todotree.exceptions.DeserializationFailedException;
import igrek.todotree.model.treeitem.AbstractTreeItem;

public class TreePersistenceService {
	
	private JsonTreeDeserializer deserializer = new JsonTreeDeserializer();
	private JsonTreeSerializer serializer = new JsonTreeSerializer();
	
	public TreePersistenceService() {
	}
	
	public String serializeTree(AbstractTreeItem root) {
		return serializer.serializeTree(root);
	}
	
	public AbstractTreeItem deserializeTree(String data) throws DeserializationFailedException {
		return deserializer.deserializeTree(data);
	}
	
}
