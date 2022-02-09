package igrek.todotree.app

class AppData {
    var state: AppState = AppState.ITEMS_LIST

    fun isState(state: AppState): Boolean {
        return this.state === state
    }

}