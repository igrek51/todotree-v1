package igrek.todotree.activity

import android.content.Intent
import android.os.Bundle

class QuickAddActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        postInit = {
            activityData.quickAddService.enableQuickAdd()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        stdNewIntent(intent)
        logger.debug("recreating new quick add activity")
        recreate()
        null?.let { // shut up, Kotlin
            super.onNewIntent(intent)
        }
    }
}