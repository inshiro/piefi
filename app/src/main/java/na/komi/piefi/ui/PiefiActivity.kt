package na.komi.piefi.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import na.komi.piefi.BuildConfig
import na.komi.piefi.data.Preferences
import na.komi.piefi.R


class PiefiActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = null
        TileService.requestListeningState(this, ComponentName(BuildConfig.APPLICATION_ID, PiefiActivity::class.java.name))
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomDialogTheme))

        builder.setTitle(getString(R.string.dialog_title))
        val choices = arrayOf(
            getString(R.string.selector_always),
            getString(R.string.selector_plugged_in),
            getString(R.string.selector_never)
        )
        val checkedItem = Preferences.sleepSetting
        builder.setSingleChoiceItems(choices, checkedItem) { dialog, which ->
            Preferences.sleepSetting = which
        }
        val dialog = builder.create().apply {
            setOnCancelListener {
                dismiss()
                setOnCancelListener(null)
                setOnDismissListener(null)
                window?.exitTransition = null
                finish()
            }
            setOnDismissListener {
                dismiss()
                setOnCancelListener(null)
                setOnDismissListener(null)
                window?.exitTransition = null
                finish()
            }
            window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@PiefiActivity,
                    R.drawable.dialog_bg
                )
            )
        }
        if(!dialog.isShowing)
            dialog.show()
    }

    override fun finish() {
        super.finish()
        System.gc()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}