package igrek.todotree.activity

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import java.util.concurrent.atomic.AtomicInteger


class ActivityResultDispatcher(
    private val appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
) {
    private val requestCodeSequence: AtomicInteger = AtomicInteger(10)
    private val requestCodeReactions: HashMap<Int, (resultCode: Int, data: Intent?) -> Unit> = hashMapOf()

    fun registerActivityResultLauncher(onResult: (resultCode: Int, data: Intent?) -> Unit): ActivityResultLauncher<Intent> {
        val activityResultLauncher: ActivityResultLauncher<Intent> =
            appCompatActivity.get()!!.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                onResult(result.resultCode, result.data)
            }
        return activityResultLauncher
    }

    @Suppress("DEPRECATION")
    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = requestCodeSequence.incrementAndGet()
        requestCodeReactions[requestCode] = onResult
        appCompatActivity.get()?.startActivityForResult(intent, requestCode)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        requestCodeReactions[requestCode]?.let { onResult ->
            onResult(resultCode, data)
        }
    }
}