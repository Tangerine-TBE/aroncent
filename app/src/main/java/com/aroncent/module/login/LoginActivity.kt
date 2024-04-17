package com.aroncent.module.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import com.aroncent.R
import com.aroncent.api.RetrofitManager
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.module.main.MainActivity
import com.aroncent.utils.setUserInfoToSp
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.onesignal.OneSignal
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*
import kotlinx.android.synthetic.main.act_register_4.et_password
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit


class LoginActivity : BaseActivity() {


    private val EMAIL = "email"


    override fun layoutId(): Int {
        return R.layout.act_login
    }

    override fun initData() {
        val callbackManager = CallbackManager.Factory.create()
        AppEventsLogger.activateApp(getApplication())
        // If you are using in a fragment, call loginButton.setFragment(this);
        // Callback registration
        fb_login.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val map = hashMapOf<String, String>()
                map["token"] = result.accessToken.token
                LogUtils.e(result.accessToken.token)
                RetrofitManager.service.faceBookLogin(map).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(object :
                        RxSubscriber<RequestUserInfoBean?>(this@LoginActivity, true) {
                        override fun _onError(message: String?) {
                            MMKV.defaultMMKV().encode("userId",result.accessToken.userId)
                            startActivity(Intent(this@LoginActivity,RegisterActivity1::class.java))
                        }

                        override fun onSubscribe(d: Disposable) {

                        }
                        @SuppressLint("SetTextI18n")
                        override fun _onNext(t: RequestUserInfoBean?) {
                            t?.let {
                                if (t.code == 200) {
                                    setUserInfoToSp(t.data.userInfo)
                                    ActivityUtils.finishAllActivities()
                                    startActivity(MainActivity::class.java)
                                } else {
                                    showToast(t.msg)
                                }
                            }
                        }
                    })
            }

            override fun onCancel() {
                Log.e("facebook", "cancel")
            }

            override fun onError(error: FacebookException) {
                Log.e("facebook", error.message!!)
            }


        })

    }

    override fun initView() {
    }

    override fun initListener() {
        tv_register.setOnClickListener {
            startActivity(RegisterActivity1::class.java)
        }
        tv_login.setOnClickListener { login() }
        tv_forgot.setOnClickListener { startActivity(ResetPasswordActivity::class.java) }
    }

    private fun login() {
        val map = hashMapOf<String, String>()
        map["email"] = et_account.text.toString()
        map["password"] = et_password.text.toString()
        RetrofitManager.service.login(map).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<RequestUserInfoBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: RequestUserInfoBean?) {
                    t?.let {
                        if (t.code == 200) {
                            setUserInfoToSp(t.data.userInfo)
                            ActivityUtils.finishAllActivities()
                            startActivity(MainActivity::class.java)
                        } else {
                            showToast(t.msg)
                        }
                    }
                }
            })
    }



    override fun start() {
    }
}