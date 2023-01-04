package com.aroncent.module.mine

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.base.BaseFragment
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.frag_mine.*
import kotlinx.android.synthetic.main.item_menu.view.*

class MineFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_mine
    }

    override fun initView() {
        Glide.with(this)
            .load(R.drawable.tab_history)
            .circleCrop()
            .into(iv_head)

        rv_mine.layoutManager = LinearLayoutManager(requireContext())
        rv_mine.adapter = ProfileAdapter(
            R.layout.item_menu, arrayListOf(
                MenuBean(1, "History"),
                MenuBean(2, "Shake Settings"),
                MenuBean(3, "Light Color Settings"),
                MenuBean(4, "My Morsecode Templates Settings"),
                MenuBean(5, "Bind My Facebook Account"),
                MenuBean(5, "Do not disturb")
            )
        )
    }

    inner class ProfileAdapter(layoutResId: Int, data: MutableList<MenuBean>) :
        BaseQuickAdapter<MenuBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: MenuBean) {
            val itemView = helper.itemView
            itemView.menu_title.text = item.title

            if (item.isShowLine) {
                itemView.v_menu_line.visibility = View.VISIBLE
            } else {
                itemView.v_menu_line.visibility = View.GONE
            }
            ClickUtils.applySingleDebouncing(itemView, 500) {
                when (item.type) {
//                    1 -> isLoginAndStartActivity(MyInfoActivity::class.java)
//                    2 -> isLoginAndStartActivity(SettingActivity::class.java)
//                    3 -> isLoginAndStartActivity(MyDeviceListActivity::class.java)
//                    4 -> startActivity(
//                        Intent(requireContext(), WebViewActivity::class.java)
//                            .putExtra("title", getString(R.string.menu_help))
//                            .putExtra("url", AppConfig.help_html_url)
//                    )
//                    5 -> isLoginAndStartActivity(AboutActivity::class.java)
                }
            }
        }
    }

    override fun lazyLoad() {
    }

    override fun initListener() {
    }
}