package igrek.todotree.app;


public class AppData {
	
	private AppState state;
	
	public AppData() {
		state = AppState.ITEMS_LIST;
	}
	
	public AppState getState() {
		return state;
	}
	
	public void setState(AppState state) {
		this.state = state;
	}
	
	public boolean isState(AppState state){
		return this.state == state;
	}
}
