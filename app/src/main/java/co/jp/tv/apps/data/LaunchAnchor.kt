package co.jp.tv.apps.data

import android.view.View
import java.lang.ref.WeakReference

/**
 * The view the launcher's window animation is anchored to. The activity attaches its decor view; the
 * catalog adapter reads it when it builds ActivityOptions. Weak, so a destroyed activity is not leaked.
 */
class LaunchAnchor {
    private var ref: WeakReference<View>? = null

    fun attach(view: View) {
        ref = WeakReference(view)
    }

    fun view(): View? = ref?.get()
}
