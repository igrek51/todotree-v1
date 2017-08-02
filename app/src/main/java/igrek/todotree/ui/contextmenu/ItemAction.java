package igrek.todotree.ui.contextmenu;


abstract class ItemAction {
	
	private String name;
	
	ItemAction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void execute();
	
	public boolean isVisible() {
		return true;
	}
}
