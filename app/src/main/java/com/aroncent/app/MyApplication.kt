package com.aroncent.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import cn.jpush.android.api.JPushInterface
import com.aroncent.db.AppDatabase
import com.clj.fastble.BleManager
import com.facebook.FacebookSdk
import com.google.android.gms.common.api.internal.LifecycleActivity
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import com.onesignal.OneSignal
import com.socks.library.KLog
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import pub.devrel.easypermissions.BuildConfig
import java.util.*
import kotlin.properties.Delegates


/**
 * Created by xuhao on 2017/11/16.
 *
 */

class MyApplication : Application(){
    private lateinit var mAppDatabase: AppDatabase
    private val ONESIGNAL_APP_ID = "1831d749-003d-4c23-9adf-6a72322a79db"
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
    fun getAppDatabase() : AppDatabase {
        return mAppDatabase
    }
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this)

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications()

        KLog.init(BuildConfig.DEBUG,"xlite")
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        MultiLanguages.init(this)
        DialogX.init(this)
        sDefault = this
        context = applicationContext
        MMKV.initialize(this)
        MultiLanguages.setAppLanguage(this, Locale.ENGLISH)
        Bugly.init(applicationContext, "4657832c07", false)

        BleManager.getInstance().init(this)
        BleManager.getInstance()
            .enableLog(BuildConfig.DEBUG)
//            .setConnectOverTime(20000)
            .setOperateTimeout(5000)
        mAppDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "xlite_device.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(MultiLanguages.attach(base))
    }
}
