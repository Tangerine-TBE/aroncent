package com.aroncent.module.shake_flash_settings

import android.annotation.SuppressLint
import android.util.Log
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.ble.BleTool
import com.aroncent.ble.BleTool.ledLight
import com.aroncent.utils.addZeroForNum
import com.aroncent.utils.binaryToHexString
import com.aroncent.utils.showToast
import com.ltwoo.estep.api.RetrofitManager
import com.tencent.mmkv.MMKV
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_shake_setting.*


class ShakeFlashSettingActivity : BaseActivity() {
    var long_shake = "0.0"
    var short_shake = "0.0"
    var long_flash = "0.0"
    var short_flash = "0.0"
    override fun layoutId(): Int {
        return R.layout.act_shake_setting
    }

    override fun initData() {

    }

    override fun initView() {
        long_shake = MMKV.defaultMMKV().decodeString(KVKey.long_shake, "0.0")
        short_shake = MMKV.defaultMMKV().decodeString(KVKey.short_shake, "0.0")
        long_flash = MMKV.defaultMMKV().decodeString(KVKey.long_flash, "0.0")
        short_flash = MMKV.defaultMMKV().decodeString(KVKey.short_flash, "0.0")
        seekBar_long.setProgress(long_shake.toFloat())
        seekBar_short.setProgress(short_shake.toFloat())
        seekBar_long_flash.setProgress(long_flash.toFloat())
        seekBar_short_flash.setProgress(short_flash.toFloat())
    }

    override fun initListener() {
        seekBar_long.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p: SeekParams) {}
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                Log.e("long_shake", seekBar.progressFloat.toString())
                long_shake = seekBar.progressFloat.toString()
            }
        }
        seekBar_short.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p: SeekParams) {}
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                Log.e("short_shake", seekBar.progressFloat.toString())
                short_shake = seekBar.progressFloat.toString()
            }
        }
        seekBar_long_flash.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p: SeekParams) {}
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                Log.e("long_flash", seekBar.progressFloat.toString())
                long_flash = seekBar.progressFloat.toString()
            }
        }
        seekBar_short_flash.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p: SeekParams) {}
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                Log.e("short_flash", seekBar.progressFloat.toString())
                short_flash = seekBar.progressFloat.toString()
            }
        }

        tv_save.setOnClickListener {
            //给设备设置默认参数，长短震，长短闪，灯光颜色
            val lightColor = MMKV.defaultMMKV().decodeString(KVKey.light_color,"FFFFFF")


            val binary_short_flash = addZeroForNum((short_flash.toFloat()*10).toInt().toString(2),4)
            val binary_long_flash = addZeroForNum((long_flash.toFloat()*10).toInt().toString(2),4)
            val binary_short_shake = addZeroForNum((short_shake.toFloat()*10).toInt().toString(2),4)
            val binary_long_shake = addZeroForNum((long_shake.toFloat()*10).toInt().toString(2),4)

            Log.e("short_flash",binary_short_flash)
            Log.e("long_flash",binary_long_flash)
            Log.e("short_shake",binary_short_shake)
            Log.e("long_shake",binary_long_shake)

            Log.e("flash",binaryToHexString(binary_short_flash+binary_long_flash))
            Log.e("shake",binaryToHexString(binary_short_shake+binary_long_shake))

            val vibration_intensity = "00" //震动强度 0-3
            val xorStr = BleTool.getXOR("02"
                    +lightColor
                    +binaryToHexString(binary_short_flash+binary_long_flash)
                    +vibration_intensity
                    +binaryToHexString(binary_short_shake+binary_long_shake)
            )

//            BleTool.sendInstruct("A5AAAC"+xorStr+"02"+"0101FFFFFF0A"+"C5CCCA")
//            setShake()
        }
    }

    private fun setShake() {
        val map = hashMapOf<String, String>()
        map["long_shake"] = long_shake
        map["short_shake"] = short_shake
        map["long_light"] = long_flash
        map["short_light"] = short_flash
        RetrofitManager.service.setshake(map)
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
                            MMKV.defaultMMKV().encode(KVKey.long_shake,long_shake)
                            MMKV.defaultMMKV().encode(KVKey.short_shake,short_shake)
                            MMKV.defaultMMKV().encode(KVKey.long_flash,long_flash)
                            MMKV.defaultMMKV().encode(KVKey.short_flash,short_flash)
                            showToast("Success")
                            finish()
                        } else {
                            showToast("Failed")
                        }
                    }
                }
            })
    }

    override fun start() {
    }
}