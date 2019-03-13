package na.komi.piefi.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.NotificationCompat
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import na.komi.piefi.Application
import na.komi.piefi.R
import na.komi.piefi.reciever.ScreenReceiver
import na.komi.piefi.data.Preferences
import na.komi.piefi.ui.AirplaneDialog
import na.komi.piefi.ui.PiefiActivity
import na.komi.piefi.ui.SleepDialog
import na.komi.piefi.util.log
import kotlin.coroutines.CoroutineContext


class ScreenService : TileService(), CoroutineScope {

    override fun onCreate() {
        super.onCreate()
        receiver = ScreenReceiver()
        receiver?.let { receiver ->
            val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            registerReceiver(receiver, filter)

            receiver.prevWifiState = receiver.wifiManager.isWifiEnabled
            var changeJob = Job()
            receiver.setOnScreenChangeListener(object : ScreenReceiver.OnScreenChangeListener {

                override fun onAirplaneModeChanged() {
                    log d "onAirplaneModeChanged in service"
                }

                override fun onScreenOff() {
                    receiver.prevWifiState = receiver.wifiManager.isWifiEnabled
                    if (Preferences.isServiceOn) {
                        changeJob.cancel()
                        changeJob = launch {

                            log d "Screen OFF"
                            //delay((Preferences.disableMin * 1000 * 60).toLong())
                            if (receiver.prevWifiState)
                                when (Preferences.sleepSetting) {
                                    //WIFI_STATE_ALWAYS -> receiver.setWifiState(true)
                                    WIFI_STATE_PLUGGED_IN -> if (!isPlugged(this@ScreenService)) receiver.setWifiState(
                                        false
                                    )
                                    WIFI_STATE_NEVER -> receiver.setWifiState(false)
                                }

                        }
                    }
                }

                override fun onScreenOn() {
                    if (Preferences.isServiceOn) {
                        changeJob.cancel()
                        changeJob = launch {
                            log d "Screen ON"
                            //delay(1000)
                            // Turn back on WiFi if it was on before
                            if (receiver.prevWifiState)
                                when (Preferences.sleepSetting) {
                                    WIFI_STATE_PLUGGED_IN -> receiver.setWifiState(true)
                                    WIFI_STATE_NEVER -> receiver.setWifiState(true)
                                }
                        }
                    }

                }

            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return Service.START_STICKY//super.onStartCommand(intent, flags, startId)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        qsTile?.let {
            // Restart service on every Tile add to prevent it from being disabled
            if (!alreadyExecuted) {
                //stopSelf()
                //stopService(Intent(this, this::class.java))
                //startService(Intent(this@ScreenService, ScreenService::class.java))
                //startActivityAndCollapse(intent)
                it.state = if (Preferences.isServiceOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                if (it.state == Tile.STATE_ACTIVE) {
                    airplaneRootCheck(noRoot = {
                        showDialog(createdAirplaneDialog)
                    },
                        hasRoot = {
                            val intent = Intent(this@ScreenService, PiefiActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }, unavailableRoot = {
                            val intent = Intent(this@ScreenService, PiefiActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                }
                // Update looks
                it.state = Tile.STATE_ACTIVE
                it.updateTile()
                alreadyExecuted = true
            }
        }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
//        stopSelf()
//        stopForeground(true)
        coroutineContext.cancelChildren()
        log d "onTileRemoved"
    }

    override fun onStartListening() {
        super.onStartListening()
        log d "Start listening"
    }

    var isRoot: Boolean? = null
    fun airplaneRootCheck(
        hasRoot: () -> Unit = {},
        noRoot: () -> Unit = {},
        unavailableRoot: () -> Unit = {},
        retry: Boolean = false,
        restart: Boolean = true
    ) {
        if (receiver != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && receiver!!.isAirplaneModeOn) {

            launch(Dispatchers.IO) {
               log d "isRoot: $isRoot"
                if (isRoot == null) isRoot = if (!retry) Shell.getShell().isRoot else Shell.newInstance().isRoot
                withContext(Dispatchers.Main) {

                    if (isRoot!!) {
                        if (restart) {
                            stopSelf()
                            stopForeground(true)
                            startService(Intent(this@ScreenService, ScreenService::class.java))
                        }
                        Preferences.isServiceOn = true
                        qsTile?.apply {
                            label = getString(R.string.tile_label);
                            state = Tile.STATE_ACTIVE
                            updateTile()
                        }
                        hasRoot()
                    } else if (!isRoot!!) {
                        stopSelf()
                        stopForeground(true)
                        Preferences.isServiceOn = false
                        qsTile?.apply {
                            label = getString(R.string.tile_label);
                            state = Tile.STATE_INACTIVE
                            updateTile()
                        }
                        noRoot()
                    }
                }

            }

        } else {
            qsTile?.apply {
                label = getString(R.string.tile_label);
                state = Tile.STATE_ACTIVE
                updateTile()
            }
            unavailableRoot()
        }
    }

    val airplaneDialog by lazy { AirplaneDialog(this@ScreenService) }
    val createdAirplaneDialog by lazy { airplaneDialog.onCreateDialog(null) }
    val createdSleepDialog by lazy { SleepDialog(this@ScreenService).onCreateDialog(null) }
    override fun onClick() {
        Log.d("TAG", "onClick")
        airplaneRootCheck(hasRoot = { showDialog(createdSleepDialog) },
            noRoot = { showDialog(createdAirplaneDialog) },
            unavailableRoot = { showDialog(createdSleepDialog) },
            restart = false)
    }

    private fun remove() {
        try {
            if (receiver != null) {
                unregisterReceiver(receiver)
                receiver = null
            }
        } catch (e: Exception) {
            Log.e(ScreenReceiver::class.java.name, "Exception", e)
        }
        alreadyExecuted = false
        stopSelf()
        stopForeground(true)
    }

    override fun onDestroy() {
        //remove()
        Log.d("tag", "onDestroy")
        coroutineContext.cancelChildren()
        super.onDestroy()
    }

    private fun isPlugged(context: Context): Boolean {
        var isPlugged = false
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = intent!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB
        isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
        return isPlugged
    }

    // Need Foreground Notification to prevent system from killing app
    // You can disable this app's notification after and service will still run
    private fun startForeground() {

        // Starting in Android 8.0 (API level 26), all notifications must be assigned to a channel or it will not appear.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_LOW)

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create pending intent, mention the Activity which needs to be
        //triggered when user clicks on notification(StopScript.class in this case)

        val pendingIntent = PendingIntent.getActivity (
            this@ScreenService,
            0,
            Intent(this@ScreenService, PiefiActivity::class.java).apply {
                action = System.currentTimeMillis().toString()
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this@ScreenService, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_wifi_rotate)
            .setTicker(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentTitle(getString(R.string.tile_label))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            //.setContentInfo("")
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)

        notificationManager.notify(NOTIF_ID, notificationBuilder.build())
        startForeground(NOTIF_ID, notificationBuilder.build())

        // Startforeground will make notification un-dismissible. See: NotificationCompat.FLAG_FOREGROUND_SERVICE
    }

    private val WIFI_STATE_ALWAYS = 0

    private val WIFI_STATE_PLUGGED_IN = 1

    private val WIFI_STATE_NEVER = 2

    private val NOTIF_ID = 1

    private val NOTIFICATION_CHANNEL_ID = "piefi_channel_id_01"

    private var receiver: ScreenReceiver? = null

    private var alreadyExecuted = false

    private val notificationManager by lazy { Application.instance.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val job = Job()

}
