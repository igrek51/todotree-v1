package igrek.todotree.service.filesystem

import android.content.Context
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.system.filesystem.ExternalCardService
import java.io.*
import java.nio.charset.Charset
import java.util.*

open class FilesystemService(
        private val context: Context,
        private val externalCardService: ExternalCardService,
) {

    private val logger: Logger = LoggerFactory.logger

    fun externalSDPath(): PathBuilder {
        // returns internal dir but creates also /storage/extSdCard/Android/data/pkg - WTF?!
        //		activity.getExternalFilesDir("data");
        return PathBuilder(externalCardService.findExternalSDPath())
    }

    fun mkdirIfNotExist(path: String?): Boolean {
        val f = File(path)
        return !f.exists() && f.mkdirs()
    }

    private fun listDir(path: String): List<String> {
        val lista: MutableList<String> = ArrayList()
        val f = File(path)
        val file = f.listFiles()
        for (aFile in file) {
            lista.add(aFile.name)
        }
        return lista
    }

    fun listDir(path: PathBuilder): List<String> {
        return listDir(path.toString())
    }

    @Throws(IOException::class)
    private fun openFile(filename: String): ByteArray {
        val f = RandomAccessFile(File(filename), "r")
        val length = f.length().toInt()
        val data = ByteArray(length)
        f.readFully(data)
        f.close()
        return data
    }

    @Throws(IOException::class)
    fun openFileString(filename: String): String {
        val bytes = openFile(filename)
        return String(bytes, Charset.forName("UTF-8"))
    }

    @Throws(IOException::class)
    private fun saveFile(filename: String, data: ByteArray) {
        val file = File(filename)
        createMissingParentDir(file)
        val fos: FileOutputStream
        fos = FileOutputStream(file)
        fos.write(data)
        fos.flush()
        fos.close()
    }

    fun createMissingParentDir(file: File) {
        val parentDir = file.parentFile
        if (!parentDir.exists()) {
            parentDir.mkdir()
            logger.debug("missing dir created: $parentDir")
        }
    }

    @Throws(IOException::class)
    fun saveFile(filename: String, str: String) {
        saveFile(filename, str.toByteArray())
    }

    fun exists(path: String?): Boolean {
        val f = File(path)
        return f.exists()
    }

    private fun delete(path: String): Boolean {
        val file = File(path)
        return file.delete()
    }

    fun delete(path: PathBuilder): Boolean {
        return delete(path.toString())
    }

    @Throws(IOException::class)
    fun copy(source: File?, dest: File?) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = FileInputStream(source)
            os = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length: Int
            while (`is`.read(buffer).also { length = it } > 0) {
                os.write(buffer, 0, length)
            }
        } finally {
            `is`?.close()
            os?.close()
        }
    }

    fun ensureAppDataDirExists() {
        val externalSD = File(externalSDPath().toString())
        val appDataDir = File(externalSD, "Android/data/igrek.todotree")
        if (!appDataDir.exists()) {
            // WTF!?? getExternalFilesDir creates dir on SD card but returns Internal storage path
            logger.info(context.getExternalFilesDir("data").absolutePath)
            appDataDir.mkdirs()
            if (appDataDir.exists()) {
                logger.info("Android app data directory has been created")
            } else {
                logger.error("Failed to create Android app data directory")
            }
        }
    }

}