package igrek.todotree.persistence

import android.annotation.SuppressLint
import android.content.Context
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import java.io.File

class LocalDataService(
    context: LazyInject<Context> = appFactory.context,
) {
    private val context by LazyExtractor(context)

    val appFilesDir: File
        @SuppressLint("SdCardPath")
        get() {
            // 1. /data/data/PACKAGE/files or /data/user/0/PACKAGE/files
            var dir: File? = context.filesDir
            if (dir != null && dir.isDirectory)
                return dir

            // 2. INTERNAL_STORAGE/Android/data/PACKAGE/files/data
            dir = context.getExternalFilesDir("data")
            if (dir != null && dir.isDirectory)
                return dir
            // 3. /data/data/PACKAGE/files
            return File("/data/data/" + context.packageName + "/files")
        }

}
