package igrek.todotree.services.tree.persistence;


class ItemAttribute {
	
	private String name;
	private String value;
	
	ItemAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ItemAttribute))
			return false;
		ItemAttribute attr2 = (ItemAttribute) obj;
		return name.equals(attr2.name) && value.equals(attr2.value);
	}
	
	@Override
	public String toString() {
		return name + " = " + value;
	}
}
