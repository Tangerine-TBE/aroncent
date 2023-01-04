package com.aroncent.module.main

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.aroncent.R
import com.aroncent.module.history.HistoryFragment
import com.aroncent.module.home.HomeFragment
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.mine.MineFragment
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private var mTab1: Fragment? = null
    private var mTab2: Fragment? = null
    private var mTab3: Fragment? = null
    override fun layoutId(): Int {
        return  R.layout.activity_main
    }

    override fun initData() {
    }
    fun resetBg() {
        Glide.with(this)
            .load(R.drawable.tab_home)
            .into(iv_main_tab_1)
        Glide.with(this)
            .load(R.drawable.tab_history)
            .into(iv_main_tab_2)
        Glide.with(this)
            .load(R.drawable.tab_my)
            .into(iv_main_tab_3)
        tv_tab_1.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
        tv_tab_2.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
        tv_tab_3.setTextColor(ContextCompat.getColor(this,R.color.tabTextNormal))
    }
    private fun setSelect(i: Int) {
        resetBg()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        when (i) {
            0 -> {
                mTab1 = supportFragmentManager.findFragmentByTag(HomeFragment::class.java.name)
                if (mTab1 == null) {
                    mTab1 = HomeFragment()
                    transaction.add(R.id.view_stub_main, mTab1!!, HomeFragment::class.java.name)
                } else {
                    transaction.show(mTab1!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_home_h)
                    .into(iv_main_tab_1)
                tv_tab_1.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
            1 -> {
                mTab2 = supportFragmentManager.findFragmentByTag(HistoryFragment::class.java.name)
                if (mTab2 == null) {
                    mTab2 = HistoryFragment()
                    transaction.add(R.id.view_stub_main, mTab2!!, HistoryFragment::class.java.name)

                } else {
                    transaction.show(mTab2!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_history_h)
                    .into(iv_main_tab_2)
                tv_tab_2.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
            2 -> {
                mTab3 = supportFragmentManager.findFragmentByTag(MineFragment::class.java.name)
                if (mTab3 == null) {
                    mTab3 = MineFragment()
                    transaction.add(R.id.view_stub_main, mTab3!!, MineFragment::class.java.name)
                } else {
                    transaction.show(mTab3!!)
                }
                Glide.with(this)
                    .load(R.drawable.tab_my_h)
                    .into(iv_main_tab_3)
                tv_tab_3.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        if (mTab1 != null) {
            transaction.hide(mTab1!!)
        }
        if (mTab2 != null) {
            transaction.hide(mTab2!!)
        }
        if (mTab3 != null) {
            transaction.hide(mTab3!!)
        }
    }

    override fun initView() {
        if (MMKV.defaultMMKV().getString("token","")==""){
            finish()
            startActivity(LoginActivity::class.java)
        } else {
            setSelect(0)
        }
    }

    override fun initListener() {
        //设置图标点击缩放
        ClickUtils.applyPressedViewScale(arrayOf(LL_tab_1,LL_tab_2,LL_tab_3),
            floatArrayOf(-0.2f,-0.2f,-0.2f))

        ClickUtils.applySingleDebouncing(LL_tab_1, 300) {
            setSelect(0)
        }
        ClickUtils.applySingleDebouncing(LL_tab_2, 300) {
            setSelect(1)
        }
        ClickUtils.applySingleDebouncing(LL_tab_3, 300) {
            setSelect(2)
        }
    }

    override fun start() {
    }

}