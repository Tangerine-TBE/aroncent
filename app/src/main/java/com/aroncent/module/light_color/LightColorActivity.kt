package com.aroncent.module.light_color

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.utils.showToast
import com.ltwoo.estep.api.RetrofitManager
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_light_color.*

class LightColorActivity : BaseActivity() {
    var lightColor = ""
    override fun layoutId(): Int {
        return R.layout.act_light_color
    }

    override fun initData() {
    }

    override fun initView() {
        lightColor = if (MMKV.defaultMMKV().decodeString(KVKey.light_color,"")==""){
            "FFFFFF"
        }else{
            MMKV.defaultMMKV().decodeString(KVKey.light_color,"")
        }
        color_picker_view.setInitialColor(Color.parseColor("#FF$lightColor"),true)
    }

    override fun initListener() {
        color_picker_view.addOnColorChangedListener {
            Log.e("onColorChanged: ","0x" + Integer.toHexString(it))
            //只需要保存后面6位颜色，透明度不需要保存
            lightColor = Integer.toHexString(it).uppercase().substring(2)
        }
        tv_save.setOnClickListener {
            setLightColor()
        }
    }

    private fun setLightColor(){
        val map = hashMapOf<String,String>()
        map["light_color"] = lightColor
        RetrofitManager.service.setlightcolor(map)
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
                            MMKV.defaultMMKV().encode(KVKey.light_color,lightColor)
                            finish()
                        }else{
                            showToast("Failed")
                        }
                    }
                }
            })
    }

    override fun start() {
    }
}