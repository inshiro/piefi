package na.komi.piefi.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.topjohnwu.superuser.Shell
import na.komi.piefi.Application

/**
 * https://gist.github.com/ishitcno1/7261765
 */
class ScreenReceiver : BroadcastReceiver() {

    interface OnScreenChangeListener {
        fun onScreenOff()
        fun onScreenOn()
        fun onAirplaneModeChanged()
    }

    private var listener: OnScreenChangeListener? = null
    fun setOnScreenChangeListener(mListener: OnScreenChangeListener) {
        listener = mListener
    }

    var wasScreenOn = true
    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            intent?.action.equals(Intent.ACTION_SCREEN_OFF) -> {
                listener?.onScreenOff()
                wasScreenOn = false
            }
            intent?.action.equals(Intent.ACTION_SCREEN_ON) -> {
                listener?.onScreenOn()
                wasScreenOn = true
            }
            intent?.action?.intern() == Intent.ACTION_AIRPLANE_MODE_CHANGED -> listener?.onAirplaneModeChanged()
        }

    }


    val wifiManager by lazy { Application.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    var prevWifiState = false
    @Suppress("DEPRECATION")
    val isAirplaneModeOn: Boolean
        get() = Settings.Global.getInt(
            Application.instance.applicationContext.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0

    fun setWifiState(state: Boolean) {
        wifiManager.setWifiState(state)
    }

    private fun WifiManager.setWifiState(state: Boolean) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && isAirplaneModeOn) {
            try {
                Shell.su("svc wifi ${if (state) "enable" else "disable"}").submit { result ->
                    if (!result.isSuccess)
                        Log.e(this::class.java.simpleName, result.err.toString())
                }
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "NoShellException", e)
            }
        } else {
            isWifiEnabled = state
        }
    }
}