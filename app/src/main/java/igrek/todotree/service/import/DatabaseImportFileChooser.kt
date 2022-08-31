package igrek.todotree.service.import

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import com.google.common.io.CharStreams
import igrek.todotree.R
import igrek.todotree.activity.ActivityResultDispatcher
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.errorcheck.SafeExecutor
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.PersistenceCommand
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class DatabaseImportFileChooser (
    activity: LazyInject<Activity> = appFactory.activityMust,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) {
    private val activity by LazyExtractor(activity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)

    private var fileChooserLauncher: ActivityResultLauncher<Intent>? = null

    companion object {
        const val MAX_FILE_BYTES = 10 * 1024 * 1024
    }

    fun init() {
        fileChooserLauncher = activityResultDispatcher.registerActivityResultLauncher { resultCode: Int, data: Intent? ->
            when (resultCode) {
                Activity.RESULT_OK -> {
                    onFileSelect(data?.data)
                }
                Activity.RESULT_CANCELED -> {
                    uiInfoService.showToast(R.string.import_operation_canceled)
                }
                else -> {
                    UiErrorHandler().handleError(RuntimeException("Unknown operation result"))
                }
            }
        }
    }

    fun showFileChooser() {
        SafeExecutor {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                // val title = uiResourceService.resString(R.string.select_file_to_import)
                // DON'T USE: val activityIntent = Intent.createChooser(intent, title)

                fileChooserLauncher.let { fileChooserLauncher ->
                    if (fileChooserLauncher != null) {
                        fileChooserLauncher.launch(intent)

                    } else {
                        activityResultDispatcher.startActivityForResult(intent) { resultCode: Int, data: Intent? ->
                            when (resultCode) {
                                Activity.RESULT_OK -> {
                                    onFileSelect(data?.data)
                                }
                                Activity.RESULT_CANCELED -> {
                                    uiInfoService.showToast(R.string.import_operation_canceled)
                                }
                                else -> {
                                    UiErrorHandler().handleError(RuntimeException("Unknown operation result"))
                                }
                            }
                        }
                    }
                }
            } catch (ex: ActivityNotFoundException) {
                uiInfoService.showToast(R.string.file_manager_not_found)
            }
        }
    }

    private fun onFileSelect(selectedUri: Uri?) {
        SafeExecutor {
            if (selectedUri != null) {
                activity.contentResolver.openInputStream(selectedUri)?.use { inputStream: InputStream ->
                    val filename = getFileNameFromUri(selectedUri)

                    val length = inputStream.available()
                    if (length > MAX_FILE_BYTES) {
                        uiInfoService.showToast(R.string.selected_file_is_too_big)
                        return@SafeExecutor
                    }

                    val fileContent: String = inputStream.bufferedReader().use { it.readText() }

                    logger.info("Loading database from file: ${selectedUri.path}")

                    PersistenceCommand().loadRootTreeFromImportedFile(fileContent, filename)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        if (uri.scheme == "content") {
            activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))?.let {
                        return it
                    }
                }
            }
        }

        val result = uri.path ?: ""
        val cut = result.lastIndexOf('/')
        if (cut == -1)
            return ""

        return result.substring(cut + 1)
    }
}