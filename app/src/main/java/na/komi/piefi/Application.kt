package na.komi.piefi


import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.squareup.leakcanary.LeakCanary
import com.topjohnwu.superuser.Shell
import na.komi.piefi.service.ScreenService

class Application : Application() {
    companion object {
        lateinit var instance: Application
        lateinit var APP_NAME: String
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        APP_NAME = getString(R.string.app_name)
        setupShell()

        if (BuildConfig.DEBUG) {
            LeakCanary.install(this)
        }

        ContextCompat.startForegroundService(this, Intent(this, ScreenService::class.java))
        TileService.requestListeningState(this, ComponentName(BuildConfig.APPLICATION_ID, Application::class.java.name))
    }

    fun setupShell() {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
    }

}
