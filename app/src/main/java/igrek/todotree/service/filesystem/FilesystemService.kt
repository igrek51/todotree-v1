package igrek.todotree.service.filesystem

import android.content.Context
import android.os.Environment
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import java.io.*
import java.nio.charset.Charset

open class FilesystemService(
    context: LazyInject<Context> = appFactory.context,
) {
    val context by LazyExtractor(context)

    private val logger: Logger = LoggerFactory.logger

    fun appDataRootDir(): File {
        context.filesDir.takeIf { it.isDirectory }?.let { return it }
        return File("/data/data/${context.packageName}/files").createIfMissing()
    }

    fun externalAppDataRootDir(): File {
        return context.getExternalFilesDir("files")!!.createIfMissing()
    }

    fun internalStorageAppDir(): File {
        val internalStorageDir = Environment.getExternalStorageDirectory()
        val dir = internalStorageDir.resolve(".todotree")
        dir.mkdirs()
        return dir
    }

    private fun File.createIfMissing(): File {
        return this.also {
            it.takeIf { !it.exists() }?.let { file ->
                file.mkdir()
                logger.info("missing directory created: ${file.absolutePath}")
            }
        }
    }

    fun appDataSubDir(name: String): File {
        return appDataRootDir().resolve(name).createIfMissing()
    }

    fun listDirFilenames(dir: File): List<String> {
        return dir.listFiles().map { it.name }
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
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.flush()
        fos.close()
    }

    private fun createMissingParentDir(file: File) {
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

}