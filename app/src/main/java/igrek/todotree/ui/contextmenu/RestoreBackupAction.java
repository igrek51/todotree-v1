package igrek.todotree.ui.contextmenu;


public abstract class RestoreBackupAction {
	
	private String name;
	
	RestoreBackupAction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void execute();
	
}
