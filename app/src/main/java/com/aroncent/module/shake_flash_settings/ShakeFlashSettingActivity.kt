package com.aroncent.module.shake_flash_settings

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.tencent.mmkv.MMKV
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_shake_setting.*
import kotlinx.android.synthetic.main.top_bar.iv_back
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ShakeFlashSettingActivity : BaseActivity() {
    var long_shake = "0.3"
    var short_shake = "0.1"
    var long_flash = "0.3"
    var short_flash = "0.1"
    var shaking_levels = "1"
    override fun layoutId(): Int {
        return R.layout.act_shake_setting
    }

    override fun initData() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: BleAnswerEvent) {
        if (event.title == "notify") {
            Log.e(this.javaClass.name, event.content)
            val str = event.content
            when {
                str.contains("0241434B") -> {
                    setShake()
                }

                str.contains("024E41434B") -> {
                    showToast(getString(R.string.save_fail))
                }
            }
        }
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        long_shake = DeviceConfig.long_shake
        short_shake = DeviceConfig.short_shake
        long_flash = DeviceConfig.long_flash
        short_flash = DeviceConfig.short_flash
        shaking_levels = DeviceConfig.shaking_levels

        seekBar_long.setProgress(long_shake.toFloat())
        seekBar_long.setIndicatorTextFormat("\${PROGRESS} S")
        seekBar_short.setProgress(short_shake.toFloat())
        seekBar_short.setIndicatorTextFormat("\${PROGRESS} S")

        seekBar_long_flash.setProgress(long_flash.toFloat())
        seekBar_long_flash.setIndicatorTextFormat("\${PROGRESS} S")

        seekBar_short_flash.setProgress(short_flash.toFloat())
        seekBar_short_flash.setIndicatorTextFormat("\${PROGRESS} S")

        seekBar_shaking_levels.setProgress(shaking_levels.toFloat())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun initListener() {
        iv_back.setOnClickListener { finish() }
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

        seekBar_shaking_levels.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(p: SeekParams) {}
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                Log.e("shaking_levels", seekBar.progress.toString())
                shaking_levels = seekBar.progress.toString()
                if (shaking_levels == "0") {
                    showToast(getString(R.string.no_vibration_notification))
                }
            }
        }

        tv_save.setOnClickListener {
            setShakeToDevice()
        }

        iv_reset.setOnClickListener {
            CustomDialog.build().setMaskColor(getColor(R.color.dialogMaskColor))
                .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_tips) {
                    override fun onBind(dialog: CustomDialog?, v: View?) {
                        v!!.let {
                            val tip = v.findViewById<TextView>(R.id.tv_tip)
                            tip.text = "Restore default settings?"
                            val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                            val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                            cancel.setOnClickListener {
                                dialog!!.dismiss()
                            }
                            confirm.setOnClickListener {
                                dialog!!.dismiss()
                                short_flash = "0.1"
                                long_flash = "0.3"
                                short_shake = "0.1"
                                long_shake = "0.3"
                                shaking_levels = "1"
                                MMKV.defaultMMKV().encode(KVKey.light_color, "FFFFFF")
                                setShakeToDevice()
                            }
                        }
                    }
                }).show()

        }
    }

    private fun setShakeToDevice() {
        //给设备设置默认参数，灯光颜色，长短震，长短闪
        if (short_flash >= long_flash) {
            showToast("the short flash must be smaller than the long flash")
            return
        } else if (short_shake >= long_shake) {
            showToast("the short shake must be smaller than the long shake")
            return
        }
        val lightColor = if (DeviceConfig.lightColor == "") "FFFFFF" else DeviceConfig.lightColor
        val binary_short_flash = addZeroForNum((short_flash.toFloat() * 10).toInt().toString(2), 4)
        val binary_long_flash = addZeroForNum((long_flash.toFloat() * 10).toInt().toString(2), 4)
        val binary_short_shake = addZeroForNum((short_shake.toFloat() * 10).toInt().toString(2), 4)
        val binary_long_shake = addZeroForNum((long_shake.toFloat() * 10).toInt().toString(2), 4)
        val vibration_intensity = addZeroForNum(shaking_levels, 2) //震动强度 0-3

        Log.e("short_flash", binary_short_flash)
        Log.e("long_flash", binary_long_flash)
        Log.e("short_shake", binary_short_shake)
        Log.e("long_shake", binary_long_shake)
        Log.e("vibration_intensity", vibration_intensity)

        Log.e("flash", binaryToHexString(binary_short_flash + binary_long_flash))
        Log.e("shake", binaryToHexString(binary_short_shake + binary_long_shake))

        val xorStr = BleTool.getXOR(
            "02" + lightColor + binaryToHexString(binary_short_flash + binary_long_flash) + vibration_intensity + binaryToHexString(
                binary_short_shake + binary_long_shake
            )
        )

        BleTool.sendInstruct(
            "A5AAAC" + xorStr + "02" + lightColor + binaryToHexString(binary_short_flash + binary_long_flash) + vibration_intensity + binaryToHexString(
                binary_short_shake + binary_long_shake
            ) + "C5CCCA"
        )
    }

    private fun setShake() {
        val map = hashMapOf<String, String>()
        map["long_shake"] = long_shake
        map["short_shake"] = short_shake
        map["long_light"] = long_flash
        map["short_light"] = short_flash
        map["shake_level"] = shaking_levels
        RetrofitManager.service.setshake(map).subscribeOn(Schedulers.io())
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
                            MMKV.defaultMMKV().encode(KVKey.long_shake, long_shake)
                            MMKV.defaultMMKV().encode(KVKey.short_shake, short_shake)
                            MMKV.defaultMMKV().encode(KVKey.long_flash, long_flash)
                            MMKV.defaultMMKV().encode(KVKey.short_flash, short_flash)
                            MMKV.defaultMMKV().encode(KVKey.shaking_levels, shaking_levels)
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