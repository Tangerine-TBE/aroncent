package com.aroncent.module.login

import android.annotation.SuppressLint
import com.aroncent.R
import com.aroncent.base.RxSubscriber
import com.aroncent.utils.showToast
import com.aroncent.api.RetrofitManager
import com.aroncent.base.BaseBean
import com.aroncent.utils.RxTimerUtil
import com.blankj.utilcode.util.ThreadUtils
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_reset_pwd.*
import kotlinx.android.synthetic.main.act_reset_pwd.et_re_pwd
import kotlinx.android.synthetic.main.act_reset_pwd.tv_ok

class ResetPasswordActivity : BaseActivity() {


    override fun layoutId(): Int {
        return R.layout.act_reset_pwd
    }

    override fun initData() {
    }

    override fun initView() {

    }

    override fun initListener() {
        tv_get_code.setOnClickListener {
            sendCode()
        }
        tv_ok.setOnClickListener {
            resetPwd()
        }
    }
    private fun resetPwd(){
        if (et_account.text.toString() == "" || et_code.text.toString()=="" ||et_password.text.toString()==""){
            showToast("Please fill in all information")
            return
        }
        if (et_password.text.toString() != et_re_pwd.text.toString()){
            showToast("Inconsistent passwords entered")
            return
        }
        val map = hashMapOf<String,String>()
        map["email"] = et_account.text.toString()
        map["newpassword"] = et_password.text.toString()
        map["event"] = "resetpwd"
        map["captcha"] = et_code.text.toString()
        RetrofitManager.service.resetpwd(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 200) {
                            showToast("Success")
                           finish()
                        }else{
                            showToast(t.msg)
                        }
                    }
                }
            })
    }

    private fun sendCode(){
        RetrofitManager.service.sendEms(hashMapOf("email" to et_account.text.toString(),"event" to "resetpwd"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(this, true) {
                override fun _onError(message: String?) {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 200) {
                            countTime()
                        }
                        showToast(t.msg)
                    }
                }
            })
    }

    private fun countTime(){
        var time = 60
        RxTimerUtil.interval(1000){
            time -= 1
            tv_get_code.isEnabled = false
            tv_get_code.text = time.toString()+"s"
            if(time==0){
                tv_get_code.isEnabled = true
                tv_get_code.text = "Get Code"
                RxTimerUtil.cancel()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
    override fun start() {
    }
}