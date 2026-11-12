package co.jp.tv.apps.data

import android.content.Context
import android.util.Log
import co.jp.tv.apps.domain.AppListCodec
import co.jp.tv.apps.domain.AppListStore
import co.jp.tv.apps.domain.TvApp
import java.io.File

/** Adapter: the snapshot lives in a small text file under noBackupFilesDir. */
class FileAppListStore(context: Context) : AppListStore {
    private val file = File(context.applicationContext.noBackupFilesDir, "apps-snapshot.tsv")

    override fun read(): List<TvApp> = try {
        if (file.isFile) AppListCodec.decode(file.readText()) else emptyList()
    } catch (e: java.io.IOException) {
        Log.w(TAG, "Could not read the app snapshot", e)
        emptyList()
    }

    override fun write(apps: List<TvApp>) {
        try {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(AppListCodec.encode(apps))
            if (!tmp.renameTo(file)) tmp.copyTo(file, overwrite = true)
        } catch (e: java.io.IOException) {
            Log.w(TAG, "Could not write the app snapshot", e)
        }
    }

    private companion object {
        const val TAG = "FileAppListStore"
    }
}
