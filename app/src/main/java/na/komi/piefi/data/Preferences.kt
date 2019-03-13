package na.komi.piefi.data

import android.content.Context.MODE_PRIVATE
import na.komi.piefi.Application

object Preferences {
    private val PREFS_FILENAME by lazy { "na.komi.piefi.preferences" }
    private val ID_SLEEP_SETTING by lazy { "ID_SLEEP_SETTING" }
    private val ID_SERVICE by lazy { "ID_SERVICE" }
    private val prefs by lazy {
        Application.instance.applicationContext.getSharedPreferences(
            PREFS_FILENAME,
            MODE_PRIVATE
        )
    }

    var isServiceOn: Boolean
        get() = prefs.getBoolean(ID_SERVICE, false)
        set(value) = prefs.edit().putBoolean(ID_SERVICE, value).apply()
    var sleepSetting: Int
        get() = prefs.getInt(ID_SLEEP_SETTING, 0)
        set(value) = prefs.edit().putInt(ID_SLEEP_SETTING, value).apply()
}