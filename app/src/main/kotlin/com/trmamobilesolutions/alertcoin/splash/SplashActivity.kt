package com.trmamobilesolutions.alertcoin.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.login.LoginActivity
import io.fabric.sdk.android.Fabric
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

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    startActivity<LoginActivity>()
                    finish()
                }
            }
        }, 2500)
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
