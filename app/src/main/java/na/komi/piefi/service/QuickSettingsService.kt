package na.komi.piefi.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import na.komi.piefi.ui.SleepDialog
import na.komi.piefi.util.log

class QuickSettingsService : TileService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log d "onStartCommand"
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        log d "onCreate"
        super.onCreate()
    }

    override fun onDestroy() {
        log d "onDestroy"
        super.onDestroy()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        log d "onTileAdded"
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            updateTile()
        }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        log d "onTileRemoved"
    }


    override fun onStartListening() {
        super.onStartListening()
        log d "onStartListening"
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            updateTile()
        }

    }

    override fun onStopListening() {
        super.onStopListening()
        log d "onStopListening"
    }

    override fun onClick() {
        super.onClick()
        log d "onClick"
        showDialog(dialog)
    }

    private val dialog by lazy { SleepDialog(this@QuickSettingsService).onCreateDialog(null) }

}