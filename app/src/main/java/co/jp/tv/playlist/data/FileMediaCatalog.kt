package co.jp.tv.playlist.data

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import co.jp.tv.playlist.domain.MediaCatalog
import co.jp.tv.playlist.domain.MediaListing
import co.jp.tv.playlist.domain.MediaLibraryState
import co.jp.tv.playlist.domain.MediaSource
import co.jp.tv.playlist.domain.ScannedFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Adapter: looks for wallpaper media in `JPTV/` on the shared storage (the old `loop.mp4` and `fondo.jpg`,
 * `fondos/` and `protector/`) and in the same folder of every readable removable volume. The scan runs on the
 * IO dispatcher; callers decide when to run it again (app start, Customize opening).
 */
class FileMediaCatalog(context: Context) : MediaCatalog {
    private val appContext = context.applicationContext
    private val state = MutableStateFlow(MediaLibraryState.Empty)
    private val scanning = Mutex()

    override val library: StateFlow<MediaLibraryState> = state.asStateFlow()

    override suspend fun refresh() {
        scanning.withLock {
            val items = withContext(Dispatchers.IO) { MediaListing.build(scan()) }
            state.value = MediaLibraryState(items, loaded = true)
        }
    }

    private fun scan(): List<ScannedFile> {
        val found = mutableListOf<ScannedFile>()
        val primary = Environment.getExternalStorageDirectory()
        val jptv = File(primary, FOLDER)
        for (legacy in LEGACY_FILES) {
            val file = File(jptv, legacy)
            if (file.isFile) found += ScannedFile(file.absolutePath, MediaSource.LEGACY)
        }
        list(File(jptv, FONDOS), MediaSource.FONDOS, found)
        list(File(jptv, PROTECTOR), MediaSource.PROTECTOR, found)
        for (root in removableRoots(primary)) {
            list(File(root, "$FOLDER/$FONDOS"), MediaSource.USB, found)
            list(File(root, "$FOLDER/$PROTECTOR"), MediaSource.PROTECTOR, found)
        }
        return found
    }

    private fun list(dir: File, source: MediaSource, into: MutableList<ScannedFile>) {
        val files = try {
            dir.listFiles()
        } catch (_: SecurityException) {
            null
        } ?: return
        files.asSequence().filter { it.isFile }.take(MAX_FILES_PER_FOLDER).forEach { into += ScannedFile(it.absolutePath, source) }
    }

    /** Mounted removable volumes (USB sticks, SD cards) other than the shared storage, without duplicates. */
    private fun removableRoots(primary: File): List<File> {
        val roots = LinkedHashMap<String, File>()
        fun add(dir: File?) {
            if (dir == null || !dir.isDirectory || !dir.canRead()) return
            val key = runCatching { dir.canonicalPath }.getOrDefault(dir.absolutePath)
            roots.putIfAbsent(key, dir)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val manager = appContext.getSystemService(StorageManager::class.java)
            manager?.storageVolumes
                ?.filter { it.isRemovable && it.state == Environment.MEDIA_MOUNTED }
                ?.forEach { add(it.directory) }
        }
        // Older Android versions do not expose a volume's path; every mounted one is a folder of /storage.
        File(STORAGE_ROOT).listFiles()
            ?.filter { it.name != "emulated" && it.name != "self" }
            ?.forEach { add(it) }
        val own = runCatching { primary.canonicalPath }.getOrDefault(primary.absolutePath)
        roots.remove(own)
        return roots.values.toList()
    }

    private companion object {
        const val FOLDER = "JPTV"
        const val FONDOS = "fondos"
        const val PROTECTOR = "protector"
        val LEGACY_FILES = listOf("loop.mp4", "fondo.jpg")
        const val STORAGE_ROOT = "/storage"
        const val MAX_FILES_PER_FOLDER = 200
    }
}
