package na.komi.piefi.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import na.komi.piefi.R

@SuppressLint("ValidFragment")
class AirplaneDialog @SuppressLint("ValidFragment") constructor(private val _context: Context, private var _listener: AirplaneDialogListener? = null) : DialogFragment() {

    interface AirplaneDialogListener {
        fun onPositiveClick(dialog: DialogFragment)
    }

    fun setListener(listener: AirplaneDialogListener){
        _listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertBuilder  = AlertDialog.Builder(ContextThemeWrapper(this@AirplaneDialog._context,
            R.style.CustomDialogTheme
        ))

        alertBuilder.setTitle(_context.getString(R.string.root_required))
            .setMessage(_context.getString(R.string.root_required_message))
            .setPositiveButton("OK") { dialog, which ->
                _listener?.onPositiveClick(this@AirplaneDialog)
            }


        return alertBuilder.create()
    }
}