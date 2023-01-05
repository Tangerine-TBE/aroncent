package com.aroncent.module.light_color

import android.util.Log
import com.aroncent.R
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.act_light_color.*

class LightColorActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_light_color
    }

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initListener() {
        color_picker_view.addOnColorSelectedListener {
            Log.e("onColorSelected: ","0x" + Integer.toHexString(it))
        }
        color_picker_view.addOnColorChangedListener {
            Log.e("onColorChanged: ","0x" + Integer.toHexString(it))
        }
    }

    override fun start() {
    }
}