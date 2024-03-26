package com.aroncent.module.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.facebook.login.LoginResult
import com.onesignal.OneSignal
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class LoginActivity : BaseActivity() {


    private val EMAIL = "email"


    override fun layoutId(): Int {
        return R.layout.act_login
    }

    override fun initData() {
        val callbackManager = CallbackManager.Factory.create()
        fb_login.setPermissions(EMAIL)
        // If you are using in a fragment, call loginButton.setFragment(this);
        // Callback registration
        fb_login.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val map = hashMapOf<String, String>()
                map["token"] = result.accessToken.token
                RetrofitManager.service.faceBookLogin(map).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(object :
                        RxSubscriber<RequestUserInfoBean?>(this@LoginActivity, true) {
                        override fun _onError(message: String?) {
                        }

                        override fun onSubscribe(d: Disposable) {

                        }
                        @SuppressLint("SetTextI18n")
                        override fun _onNext(t: RequestUserInfoBean?) {
                            t?.let {
                                if (t.code == 200) {
                                    setUserInfoToSp(t.data.userInfo)
                                    updateRid()
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
                            updateRid()
                            ActivityUtils.finishAllActivities()
                            startActivity(MainActivity::class.java)
                        } else {
                            showToast(t.msg)
                        }
                    }
                }
            })
    }

    private fun updateRid() {
        val map = hashMapOf<String, String>()
        map["jg_pushid"] = JPushInterface.getRegistrationID(this)
        map["onesignal_pushid"] = OneSignal.getDeviceState()?.userId ?: ""
        RetrofitManager.service.updaterid(map).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, false) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {

                    }
                }
            })
    }

    override fun start() {
    }
}