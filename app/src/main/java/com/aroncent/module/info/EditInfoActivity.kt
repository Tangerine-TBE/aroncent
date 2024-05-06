package com.aroncent.module.info

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.content.ContentProviderCompat.requireContext
import com.aroncent.R
import com.aroncent.api.RetrofitManager
import com.aroncent.app.KVKey
import com.aroncent.app.MyApplication
import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.aroncent.base.UploadBean
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.main.UpdateHeadPicEvent
import com.aroncent.utils.GlideEngine
import com.aroncent.utils.UploadUtils
import com.bumptech.glide.Glide
import com.aroncent.utils.showToast
import com.blankj.utilcode.util.ActivityUtils
import com.facebook.AccessToken
import com.facebook.login.LoginManager

import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.tencent.mmkv.MMKV
import com.xlitebt.base.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_info_activity.btn_save
import kotlinx.android.synthetic.main.act_info_activity.edit_email
import kotlinx.android.synthetic.main.act_info_activity.edit_name
import kotlinx.android.synthetic.main.act_info_activity.edit_new2_password
import kotlinx.android.synthetic.main.act_info_activity.edit_new_password
import kotlinx.android.synthetic.main.frag_mine.iv_head
import kotlinx.android.synthetic.main.frag_mine.tv_name
import org.greenrobot.eventbus.EventBus
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

class EditInfoActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.act_info_activity
    }

    override fun initData() {
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.avatar, "")).circleCrop()
            .error(R.drawable.head_default_pic).into(iv_head)
        tv_name.text = MMKV.defaultMMKV().decodeString(KVKey.username, "")
        edit_name.setText(MMKV.defaultMMKV().decodeString(KVKey.nickname, ""))
        edit_email.setText(MMKV.defaultMMKV().decodeString(KVKey.email, ""))
    }

    override fun initView() {

    }

    private fun editAvatar(avatar: String) {
        RetrofitManager.service.editavatar(hashMapOf("avatar" to avatar))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
                            MMKV.defaultMMKV().encode(KVKey.avatar, avatar)
                            Glide.with(this@EditInfoActivity)
                                .load(MMKV.defaultMMKV().decodeString(KVKey.avatar, ""))
                                .circleCrop()
                                .error(R.drawable.head_default_pic).into(iv_head)
                            EventBus.getDefault().post(UpdateHeadPicEvent())
                        }
                    }
                }
            })
    }

    override fun initListener() {
        iv_head.setOnClickListener {
            PictureSelector.create(this).openGallery(SelectMimeType.ofImage())
                .setMaxSelectNum(1).setImageEngine(GlideEngine.createGlideEngine())
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
                }).forResult(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>?) {
                        UploadUtils.uploadFile(
                            File(result!![0]!!.compressPath),
                            object : RxSubscriber<UploadBean>(this@EditInfoActivity, true) {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun _onNext(t: UploadBean?) {
                                    if (t!!.code == 200) {
                                        editAvatar(t.data.fullurl)
                                        showToast(t.msg)
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


        btn_save.setOnClickListener {
            val map = hashMapOf<String, String>()
            map["avatar"] = MMKV.defaultMMKV().decodeString(KVKey.avatar, "")
            map["nickname"] = edit_name.text.toString()
            map["email"] = edit_email.text.toString()
            if (edit_new_password.text.toString() == edit_new2_password.text.toString()) {
                map["newpassword"] = edit_new_password.text.toString()
            } else {
                showToast("different password1 between password2 !")
                return@setOnClickListener
            }
            RetrofitManager.service.editProfile(map).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(
                    object : RxSubscriber<BaseBean?>(this, true) {
                        override fun _onError(message: String?) {
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        @SuppressLint("SetTextI18n")
                        override fun _onNext(t: BaseBean?) {
                            if (t!!.code == 200) {
                                MMKV.defaultMMKV().encode(KVKey.email, edit_email.text.toString())
                                MMKV.defaultMMKV().encode(KVKey.nickname, edit_name.text.toString())
                                EventBus.getDefault().post(UpdateHeadPicEvent())
                                MMKV.defaultMMKV().clearAll()
                                val accessToken: AccessToken? =
                                    AccessToken.getCurrentAccessToken()
                                val isLoggedIn = accessToken != null && !accessToken.isExpired
                                if(isLoggedIn){
                                    LoginManager.getInstance().logOut()
                                }
                                ActivityUtils.finishAllActivities()
                                startActivity(Intent(this@EditInfoActivity, LoginActivity::class.java))
                                showToast("Success")
                            }
                        }
                    }

                )


        }
    }

    override fun start() {
    }
}