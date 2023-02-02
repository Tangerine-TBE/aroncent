package com.aroncent.module.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.BaseFragment
import com.aroncent.base.RxSubscriber
import com.aroncent.base.UploadBean
import com.aroncent.module.light_color.LightColorActivity
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.main.MainActivity
import com.aroncent.module.phrase.AddPhraseActivity
import com.aroncent.module.shake_flash_settings.ShakeFlashSettingActivity
import com.aroncent.utils.UploadUtils
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.aroncent.api.RetrofitManager
import com.aroncent.app.MyApplication
import com.aroncent.module.main.UpdateHeadPicEvent
import com.aroncent.utils.GlideEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_mine.*
import kotlinx.android.synthetic.main.item_menu.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


class MineFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.frag_mine
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(iv_head)
    }
    override fun initView() {
        EventBus.getDefault().register(this)
        tv_name.text = "Hello,"+MMKV.defaultMMKV().decodeString(KVKey.username,"")
        Glide.with(this)
            .load(MMKV.defaultMMKV().decodeString(KVKey.avatar,""))
            .circleCrop()
            .error(R.drawable.head_default_pic)
            .into(iv_head)
        rv_mine.layoutManager = LinearLayoutManager(requireContext())
        rv_mine.adapter = ProfileAdapter(
            R.layout.item_menu, arrayListOf(
                MenuBean(1, "Shake Settings"),
                MenuBean(2, "Light Color Settings"),
                MenuBean(3, "My Morsecode Templates Settings"),
                MenuBean(4, "Bind My Facebook Account"),
                MenuBean(5, "Do not disturb"),
                MenuBean(6, "Upload My Video"),
                MenuBean(7, "Unbind your partner"),
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
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
                    1 -> startActivity(Intent(requireContext(), ShakeFlashSettingActivity::class.java))
                    2 -> startActivity(Intent(requireContext(), LightColorActivity::class.java))
                    3 -> startActivity(Intent(requireContext(), AddPhraseActivity::class.java))
                    4->showToast("Coming Soon")
                    5->showToast("Coming Soon")
                    6->{
                        PictureSelector.create(requireContext())
                            .openCamera(SelectMimeType.ofVideo())
                            .setLanguage(LanguageConfig.ENGLISH)
                            .setRecordVideoMaxSecond(15)
                            .setRecordVideoMinSecond(5)
                            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                                override fun onResult(result: ArrayList<LocalMedia?>?) {
                                    UploadUtils.uploadFile(File(result!![0]!!.realPath),object : RxSubscriber<UploadBean>(requireContext(),true){
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun _onNext(t: UploadBean?) {
                                            if (t!!.code==200){
                                                showToast(t.msg)
                                                setMyVideo(t.data.fullurl)
                                            }
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
                    7->{
                        CustomDialog
                            .build()
                            .setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
                            .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_tips) {
                                override fun onBind(dialog: CustomDialog?, v: View?) {
                                    v!!.let {
                                        val tip = v.findViewById<TextView>(R.id.tv_tip)
                                        tip.text = "Unbind your partner?"
                                        val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                                        val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                                        cancel.setOnClickListener {
                                            dialog!!.dismiss()
                                        }
                                        confirm.setOnClickListener {
                                            dialog!!.dismiss()
                                            unbind()
                                        }
                                    }
                                }
                            })
                            .show()
                    }
                }
            }
        }
    }


    private fun setMyVideo(url:String){
        RetrofitManager.service.setMyVideo(hashMapOf("videopath" to url))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {

                    }
                }
            })
    }
    private fun unbind(){
        RetrofitManager.service.deletepanter(hashMapOf())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code==200){
                            showToast("Success")
                            ActivityUtils.finishAllActivities()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                        }
                    }
                }
            })
    }

    private fun editAvatar(avatar:String){
        RetrofitManager.service.editavatar(hashMapOf("avatar" to avatar))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code==200){
                            showToast("Success")
                            MMKV.defaultMMKV().encode(KVKey.avatar,avatar)
                            EventBus.getDefault().post(UpdateHeadPicEvent())
                        }
                    }
                }
            })
    }

    override fun lazyLoad() {
    }



    override fun initListener() {
        tv_logout.setOnClickListener {
                CustomDialog
                    .build()
                    .setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
                    .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_tips) {
                        override fun onBind(dialog: CustomDialog?, v: View?) {
                            v!!.let {
                                val tip = v.findViewById<TextView>(R.id.tv_tip)
                                tip.text = "Are you sure to exit?"
                                val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                                val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                                cancel.setOnClickListener {
                                    dialog!!.dismiss()
                                }
                                confirm.setOnClickListener {
                                    dialog!!.dismiss()
                                    MMKV.defaultMMKV().clearAll()
                                    ActivityUtils.finishAllActivities()
                                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                                }
                            }
                        }
                    })
                    .show()

        }

        iv_head.setOnClickListener {
            PictureSelector.create(requireContext())
                .openGallery(SelectMimeType.ofImage())
                .setMaxSelectNum(1)
                .setImageEngine(GlideEngine.createGlideEngine())
                .setLanguage(LanguageConfig.ENGLISH)
                .setCompressEngine(CompressFileEngine { _, source, call ->
                    Luban.with(MyApplication.context).load(source)
                        .ignoreBy(100)//当原始图像文件大小小于一个值时，请勿压缩
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call?.onCallback(source, compressFile!!.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                call?.onCallback(source, "")
                            }
                        }).launch()
                })
                .forResult(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>?) {
                        UploadUtils.uploadFile(File(result!![0]!!.compressPath),object : RxSubscriber<UploadBean>(requireContext(),true){
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun _onNext(t: UploadBean?) {
                                if (t!!.code==200){
                                    showToast(t.msg)
                                    editAvatar(t.data.fullurl)
                                }
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