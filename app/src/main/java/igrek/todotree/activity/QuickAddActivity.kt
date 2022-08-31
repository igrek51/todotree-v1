package igrek.todotree.activity

import android.content.Intent
import android.os.Bundle

class QuickAddActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityData.quickAddService.enableQuickAdd()
    }

    override fun onNewIntent(intent: Intent?) {
        stdNewIntent(intent)
        logger.debug("recreating new quick add activity")
        recreate()
        null?.let {
            super.onNewIntent(intent)
        }
    }
}