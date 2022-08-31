package igrek.todotree.inject

import android.content.Context

var appFactory: AppFactory = AppFactory(null)

object AppContextFactory {
    fun createAppContext(context: Context) {
        appFactory = AppFactory(context)
    }
}
