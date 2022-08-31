package igrek.todotree.activity

import android.content.Intent
import android.os.Bundle

class RemotePushActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        postInit = {
            activityData.remotePushService.enableRemotePush()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        stdNewIntent(intent)
        logger.debug("recreating new remote push activity")
        recreate()
        null?.let { // shut up, Kotlin
            super.onNewIntent(intent)
        }
    }
}