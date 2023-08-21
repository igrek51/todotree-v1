package igrek.todotree.service.clipboard

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory

open class SystemClipboardManager (
) {
    private val activity: Activity by LazyExtractor(appFactory.activity)

    open fun copyToSystemClipboard(text: String?) {
        val clipboard = activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    val systemClipboard: String?
        get() {
            val clipboard =
                activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip() &&
                clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                val item = clipboard.primaryClip!!.getItemAt(0) ?: return null
                return if (item.text == null) null else item.text.toString()
            }
            return null
        }
}