package co.jp.tv.inputs.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.util.Log
import co.jp.tv.inputs.domain.InputRules
import co.jp.tv.inputs.domain.InputSource

/**
 * Lists the device's external video inputs (HDMI and friends) through Android TV's `TvInputManager`
 * and switches to one. Both calls only read the list and start the system's own TV app: nothing is changed.
 */
class TvInputCatalog(context: Context) {
    private val appContext = context.applicationContext

    /** Passthrough inputs only; empty when the firmware has no TV input framework. */
    fun list(): List<InputSource> {
        val manager = manager() ?: return emptyList()
        return try {
            InputRules.sorted(
                manager.tvInputList.filter { it.isPassthroughInput }.map { info ->
                    InputSource(
                        id = info.id,
                        kind = InputRules.kindOfType(info.type),
                        label = InputRules.ownLabel(info.loadCustomLabel(appContext) ?: info.loadLabel(appContext)),
                        signal = InputRules.signalOf(manager.getInputState(info.id)),
                    )
                },
            )
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not read the TV inputs", e)
            emptyList()
        }
    }

    /** Opens [inputId] in the system TV app; false when nothing on the device can show it. */
    fun open(inputId: String): Boolean = try {
        val uri = TvContract.buildChannelUriForPassthroughInput(inputId)
        appContext.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No TV app to show $inputId")
        false
    } catch (e: SecurityException) {
        Log.w(TAG, "Not allowed to open $inputId", e)
        false
    }

    private fun manager(): TvInputManager? = appContext.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager

    private companion object {
        const val TAG = "TvInputCatalog"
    }
}
