package com.aroncent.module.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.module.light_color.LightColorActivity
import com.aroncent.module.login.LoginActivity
import com.aroncent.utils.UploadUtils
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.VideoQuality
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.frag_mine.*
import kotlinx.android.synthetic.main.item_menu.view.*
import java.io.File


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
                MenuBean(1, "Shake Settings"),
                MenuBean(2, "Light Color Settings"),
                MenuBean(3, "My Morsecode Templates Settings"),
                MenuBean(4, "Bind My Facebook Account"),
                MenuBean(5, "Do not disturb"),
                MenuBean(6, "Upload My Video")
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
                    2 -> startActivity(Intent(requireContext(), LightColorActivity::class.java))
                    6->{
                        PictureSelector.create(requireContext())
                            .openCamera(SelectMimeType.ofVideo())
                            .setLanguage(LanguageConfig.ENGLISH)
                            .setRecordVideoMaxSecond(15)
                            .setRecordVideoMinSecond(5)
                            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                                override fun onResult(result: ArrayList<LocalMedia?>?) {
                                    UploadUtils.uploadFile(File(result!![0]!!.realPath),object : RxSubscriber<BaseBean>(requireContext(),true){
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun _onNext(t: BaseBean?) {
                                            showToast(t!!.msg)
                                        }

                                        override fun _onError(message: String?) {
                                            showToast(message!!)
                                        }
                                    })
                                }
                                override fun onCancel() {
                                    showToast("Cancel")
                                }
                            })
                    }
                }
            }
        }
    }



    override fun lazyLoad() {
    }

    override fun initListener() {
        tv_logout.setOnClickListener {
            MMKV.defaultMMKV().clearAll()
            ActivityUtils.finishAllActivities()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }
}