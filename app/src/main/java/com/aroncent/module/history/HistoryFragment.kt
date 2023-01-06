package com.aroncent.module.history

import android.view.View
import android.widget.TextView
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.aroncent.ble.BleTool
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.android.synthetic.main.frag_history.*

class HistoryFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_history
    }

    override fun initView() {
    }

    override fun lazyLoad() {
    }

    override fun initListener() {
        button.setOnClickListener {
//            val xorStr = BleTool.getXOR("01"+"0101FFFFFF0A")
//            BleTool.sendInstruct("A5AAAC"+xorStr+"01"+"0101FFFFFF0A"+"C5CCCA")

        }
    }
}