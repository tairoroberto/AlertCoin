package com.trmamobilesolutions.alertcoin.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.home.view.MainActivity
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import java.util.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fabric = Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(true)
                .build()

        Fabric.with(fabric)

        setContentView(R.layout.activity_splash)

        Timber.i(stringFromJNI())

        try {
            val path = Uri.parse("android.resource://" + packageName + "/" + +R.raw.bg)
            videoView.setVideoURI(path)

            videoView.setOnCompletionListener { jump() }

            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val params = ConstraintLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels)
            params.bottomToBottom = content.id
            params.endToEnd = content.id
            params.startToStart = content.id
            params.topToTop = content.id
            videoView.layoutParams = params

            videoView.start()
        } catch (e: Exception) {
            jump()
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    startActivity<MainActivity>()
                    finish()
                }
            }
        }, 2500)
    }

    private fun jump() {
        if (isFinishing) {
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
