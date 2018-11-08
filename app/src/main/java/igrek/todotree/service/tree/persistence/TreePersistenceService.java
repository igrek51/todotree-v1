package igrek.todotree.service.tree.persistence;


import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.exceptions.DeserializationFailedException;

public class TreePersistenceService {
	
	private GsonTreeDeserializer deserializer = new GsonTreeDeserializer();
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
