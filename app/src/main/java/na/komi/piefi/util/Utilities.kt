package na.komi.piefi.util

import android.util.Log
import na.komi.piefi.Application
import na.komi.piefi.BuildConfig

object log {

    infix fun d(message: String) {
        if (BuildConfig.DEBUG)
            Log.d(Application.APP_NAME, message)
    }

    infix fun v(message: String) {
        if (BuildConfig.DEBUG)
            Log.v(Application.APP_NAME, message)
    }

    infix fun i(message: String) {
        if (BuildConfig.DEBUG)
            Log.i(Application.APP_NAME, message)
    }

    infix fun w(message: String) {
        if (BuildConfig.DEBUG)
        Log.w(Application.APP_NAME, message)
    }

    infix fun e(message: String) {
        if (BuildConfig.DEBUG)
        Log.e(Application.APP_NAME, message)
    }

    infix fun wtf(message: String) {
        if (BuildConfig.DEBUG)
        Log.wtf(Application.APP_NAME, message)
    }
}