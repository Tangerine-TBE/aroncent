package com.aroncent.module.light_color

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.ble.BleAnswerEvent
import com.aroncent.ble.BleTool
import com.aroncent.ble.DeviceConfig
import com.aroncent.utils.addZeroForNum
import com.aroncent.utils.binaryToHexString
import com.aroncent.utils.showToast
import com.aroncent.api.RetrofitManager
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_light_color.*
import kotlinx.android.synthetic.main.top_bar.iv_back
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LightColorActivity : BaseActivity() {
    var lightColor = ""
    var long_shake = "0.0"
    var short_shake = "0.0"
    var long_flash = "0.0"
    var short_flash = "0.0"
    var shaking_levels = "0"

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: BleAnswerEvent) {
        if (event.title == "notify") {
            Log.e(this.javaClass.name, event.content)
            val str = event.content
            when{
                str.contains("0241434B")->{
                    setLightColor()
                }
            }
        }
    }
    override fun layoutId(): Int {
        return R.layout.act_light_color
    }

    override fun initData() {
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        lightColor = if (DeviceConfig.lightColor =="") "FFFFFF" else DeviceConfig.lightColor
        color_picker_view.setInitialColor(Color.parseColor("#FF$lightColor"))
        long_shake = DeviceConfig.long_shake
        short_shake = DeviceConfig.short_shake
        long_flash = DeviceConfig.long_flash
        short_flash = DeviceConfig.short_flash
        shaking_levels = DeviceConfig.shaking_levels
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun initListener() {
        iv_back.setOnClickListener { finish() }
        color_picker_view.subscribe { color, fromUser, shouldPropagate ->
            if (fromUser){
                Log.e("subscribe: ",Integer.toHexString(color).uppercase())
            }
            //只需要保存后面6位颜色，透明度不需要保存
            lightColor = Integer.toHexString(color).uppercase().substring(2)
        }

        tv_save.setOnClickListener {

            //给设备设置默认参数，灯光颜色，长短震，长短闪
            val binary_short_flash = addZeroForNum((short_flash.toFloat()*10).toInt().toString(2),4)
            val binary_long_flash = addZeroForNum((long_flash.toFloat()*10).toInt().toString(2),4)
            val binary_short_shake = addZeroForNum((short_shake.toFloat()*10).toInt().toString(2),4)
            val binary_long_shake = addZeroForNum((long_shake.toFloat()*10).toInt().toString(2),4)
            val vibration_intensity = addZeroForNum(shaking_levels,2) //震动强度 0-3

            Log.e("short_flash",binary_short_flash)
            Log.e("long_flash",binary_long_flash)
            Log.e("short_shake",binary_short_shake)
            Log.e("long_shake",binary_long_shake)
            Log.e("vibration_intensity",vibration_intensity)

            Log.e("flash", binaryToHexString(binary_short_flash+binary_long_flash))
            Log.e("shake", binaryToHexString(binary_short_shake+binary_long_shake))

            val xorStr = BleTool.getXOR("02"
                    + lightColor
                    + binaryToHexString(binary_short_flash+binary_long_flash)
                    + vibration_intensity
                    + binaryToHexString(binary_short_shake+binary_long_shake)
            )

            BleTool.sendInstruct("A5AAAC"+xorStr+"02"
                    + lightColor
                    + binaryToHexString(binary_short_flash+binary_long_flash)
                    + vibration_intensity
                    + binaryToHexString(binary_short_shake+binary_long_shake)
                    + "C5CCCA")

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