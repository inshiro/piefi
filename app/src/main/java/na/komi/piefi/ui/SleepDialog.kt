package na.komi.piefi.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import na.komi.piefi.data.Preferences
import na.komi.piefi.R


class SleepDialog(private val _context: Context, private var _listener: SleepDialogListener? = null) :
    DialogFragment() {

    interface SleepDialogListener {
        fun onPositiveClick(dialog: DialogFragment)
        fun onNegativeClick(dialog: DialogFragment)
        fun onNeutralClick(dialog: DialogFragment)
    }

    fun setListener(listener: SleepDialogListener) {
        _listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertBuilder =
            AlertDialog.Builder(ContextThemeWrapper(this@SleepDialog._context, R.style.CustomDialogTheme))
        val checkedItem = Preferences.sleepSetting
        val choices = arrayOf(
            this@SleepDialog._context.getString(R.string.selector_always),
            this@SleepDialog._context.getString(R.string.selector_plugged_in),
            this@SleepDialog._context.getString(R.string.selector_never)
        )
        alertBuilder.setTitle(this@SleepDialog._context.getString(R.string.dialog_title))
        alertBuilder.setSingleChoiceItems(choices, checkedItem) { dialog, which ->
            Preferences.sleepSetting = which
        }


        return alertBuilder.create().apply {
            window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@SleepDialog._context,
                    R.drawable.dialog_bg
                )
            )
        }
    }
}