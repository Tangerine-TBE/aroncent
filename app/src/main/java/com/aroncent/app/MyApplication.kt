package com.aroncent.app

import android.app.Application
import android.content.Context
import com.clj.fastble.BleManager
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import com.socks.library.KLog
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import pub.devrel.easypermissions.BuildConfig
import kotlin.properties.Delegates


/**
 * Created by xuhao on 2017/11/16.
 *
 */

class MyApplication : Application(){

    companion object {
        private val TAG = "MyApplication"
        var context: Context by Delegates.notNull()
            private set

        private lateinit var sDefault: MyApplication

        @JvmStatic
        fun getDefault(): MyApplication {
            return sDefault
        }
    }

    override fun onCreate() {
        super.onCreate()
        KLog.init(BuildConfig.DEBUG,"xlite")
        MultiLanguages.init(this)
        DialogX.init(this)
        sDefault = this
        context = applicationContext
        MMKV.initialize(this)
        Bugly.init(applicationContext, "4657832c07", false)

        BleManager.getInstance().init(this)
        BleManager.getInstance()
            .enableLog(BuildConfig.DEBUG)
//            .setConnectOverTime(20000)
            .setOperateTimeout(5000)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(MultiLanguages.attach(base))
    }
}
