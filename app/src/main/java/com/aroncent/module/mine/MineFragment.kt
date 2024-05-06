package com.aroncent.module.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings.Global
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.aroncent.R
import com.aroncent.api.BindResponse
import com.aroncent.api.RetrofitManager
import com.aroncent.app.KVKey
import com.aroncent.base.BaseBean
import com.aroncent.base.BaseFragment
import com.aroncent.base.Disclaimers
import com.aroncent.base.RxSubscriber
import com.aroncent.base.UploadBean
import com.aroncent.ble.BleTool
import com.aroncent.ble.DeviceConfig
import com.aroncent.event.ReConnectEvent
import com.aroncent.module.info.EditInfoActivity
import com.aroncent.module.light_color.LightColorActivity
import com.aroncent.module.login.LoginActivity
import com.aroncent.module.login.RegisterActivity1
import com.aroncent.module.login.RequestUserInfoBean
import com.aroncent.module.main.MainActivity
import com.aroncent.module.main.UpdateHeadPicEvent
import com.aroncent.module.phrase.AddPhraseActivity
import com.aroncent.module.shake_flash_settings.ShakeFlashSettingActivity
import com.aroncent.utils.UploadUtils
import com.aroncent.utils.addZeroForNum
import com.aroncent.utils.binaryToHexString
import com.aroncent.utils.setUserInfoToSp
import com.aroncent.utils.showToast
import com.aroncent.utils.startActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.FileUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.clj.fastble.BleManager
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.language.LanguageConfig
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_mine.*
import kotlinx.android.synthetic.main.item_menu.view.*
import kotlinx.android.synthetic.main.item_not_disturb.not_disturb
import kotlinx.android.synthetic.main.item_press_send_morse.morse_model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.InetAddress
import java.util.Arrays


class MineFragment : BaseFragment() {
    private lateinit var callbackManager : CallbackManager

    override fun getLayoutId(): Int {
        return R.layout.frag_mine
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: UpdateHeadPicEvent) {
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.avatar, "")).circleCrop()
            .error(R.drawable.head_default_pic).into(iv_head)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {

        } else {
        }
        super.onHiddenChanged(hidden)
    }

    override fun initView() {
        callbackManager = (requireActivity() as MainActivity).callbackManager
        EventBus.getDefault().register(this)
        not_disturb.setCheckedNoEvent(MMKV.defaultMMKV().getBoolean(KVKey.not_disturb, false))
        morse_model.setCheckedNoEvent(MMKV.defaultMMKV().getBoolean(KVKey.morse_model, false))
        tv_name.text = "Hello," + MMKV.defaultMMKV().decodeString(KVKey.username, "")
        Glide.with(this).load(MMKV.defaultMMKV().decodeString(KVKey.avatar, "")).circleCrop()
            .error(R.drawable.head_default_pic).into(iv_head)
        rv_mine.layoutManager = LinearLayoutManager(requireContext())
        rv_mine.adapter = ProfileAdapter(
            R.layout.item_menu, arrayListOf(
                MenuBean(1, "Shake Settings"),
                MenuBean(2, "Light Color Settings"),
                MenuBean(3, "My Morsecode Templates Settings"),
                MenuBean(4, "Bind My Facebook Account"),
                MenuBean(6, "Upload My Video"),
                MenuBean(7, "Unbind your partner"),
                MenuBean(8, "Unpair your device")
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
                    1 -> startActivity(
                        Intent(
                            requireContext(), ShakeFlashSettingActivity::class.java
                        )
                    )

                    2 -> startActivity(Intent(requireContext(), LightColorActivity::class.java))
                    3 -> startActivity(Intent(requireContext(), AddPhraseActivity::class.java))
                    4 -> {
                        val accessToken: AccessToken? =
                            AccessToken.getCurrentAccessToken()
                        Profile.getCurrentProfile()
                        val isLoggedIn = accessToken != null && !accessToken.isExpired
                        if (isLoggedIn) {
                            showToast("already bind facebook !")
                            return@applySingleDebouncing
                        }
                        LoginManager.getInstance().registerCallback(callbackManager, object :
                            FacebookCallback<LoginResult> {
                            override fun onSuccess(result: LoginResult) {
                                val map = hashMapOf<String, String>()
                                map["Facebook"] = result.accessToken.userId
                                RetrofitManager.service.bindfacebook(map)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(object :
                                        RxSubscriber<BindResponse>(context, true) {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun _onNext(t: BindResponse) {
                                            if (t.code == 200L) {
                                                showToast("success")
                                            } else {
                                                showToast(t.msg)
                                            }
                                        }

                                        override fun _onError(message: String?) {

                                        }
                                    })
                            }

                            override fun onCancel() {
                                Log.e("facebook", "cancel")
                            }

                            override fun onError(error: FacebookException) {
                                Log.e("facebook", error.message!!)
                            }
                        })
                        //检查是否可以到达www.google.com
                        GlobalScope.launch {
                            withContext(Dispatchers.IO) {
                                WaitDialog.show("checking")
                                val address = InetAddress.getByName("www.google.com")
                                val reachable = address.isReachable(3000) // 10 seconds timeout
                                if (reachable) {
                                    LoginManager.getInstance().logInWithReadPermissions(
                                        requireActivity(),
                                        Arrays.asList("public_profile")
                                    )
                                } else {
                                    showToast("Network is not reachable")
                                }
                                WaitDialog.dismiss()
                            }
                        }
                    }

                    6 -> {
                        PictureSelector.create(requireContext())
                            .openCamera(SelectMimeType.ofVideo())
                            .setLanguage(LanguageConfig.ENGLISH).setRecordVideoMaxSecond(15)
                            .setRecordVideoMinSecond(5)
                            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                                override fun onResult(result: ArrayList<LocalMedia?>?) {
                                    UploadUtils.uploadFile(FileUtils.getFileByPath(result!![0]!!.realPath),
                                        object : RxSubscriber<UploadBean>(requireContext(), true) {
                                            override fun onSubscribe(d: Disposable) {

                                            }

                                            override fun _onNext(t: UploadBean?) {
                                                if (t!!.code == 200) {
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

                    7 -> {
                        RetrofitManager.service.getDisclaimers(hashMapOf())
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object :
                                RxSubscriber<Disclaimers?>(requireContext(), true) {
                                override fun _onError(message: String?) {

                                }

                                override fun onSubscribe(d: Disposable) {

                                }

                                @SuppressLint("SetTextI18n")
                                override fun _onNext(t: Disclaimers?) {
                                    t?.let {
                                        if (t.code == 200) {
                                            CustomDialog.build()
                                                .setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
                                                .setCustomView(object :
                                                    OnBindView<CustomDialog>(R.layout.dialog_tips2) {
                                                    override fun onBind(
                                                        dialog: CustomDialog?,
                                                        v: View?
                                                    ) {
                                                        v!!.let {
                                                            val tip =
                                                                v.findViewById<TextView>(R.id.tv_tip)
                                                            tip.text = Html.fromHtml(t.data.content)
                                                            val confirm =
                                                                v.findViewById<TextView>(R.id.tv_confirm)
                                                            val cancel =
                                                                v.findViewById<TextView>(R.id.tv_cancel)
                                                            val cb_check =
                                                                v.findViewById<CheckBox>(R.id.cb_check)
                                                            cancel.setOnClickListener {
                                                                dialog!!.dismiss()
                                                            }
                                                            confirm.setOnClickListener {
                                                                if (cb_check.isChecked) {
                                                                    dialog!!.dismiss()
                                                                    unbind()
                                                                } else {
                                                                    showToast("please read and confirm the content")
                                                                }

                                                            }
                                                        }
                                                    }
                                                }).show()
                                        } else {
                                            showToast(t.msg)
                                        }
                                    }
                                }
                            })

                    }

                    8 -> {
                        CustomDialog.build()
                            .setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
                            .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_tips) {
                                override fun onBind(dialog: CustomDialog?, v: View?) {
                                    v!!.let {
                                        val tip = v.findViewById<TextView>(R.id.tv_tip)
                                        tip.text =
                                            "Before connecting your phone to a new device, you need to unpair it from any previously connected devices. Are you sure you want to unpair it?"
                                        val confirm = v.findViewById<TextView>(R.id.tv_confirm)
                                        val cancel = v.findViewById<TextView>(R.id.tv_cancel)
                                        cancel.setOnClickListener {
                                            dialog!!.dismiss()
                                        }
                                        confirm.setOnClickListener {
                                            dialog!!.dismiss()
                                            unSetEquipment()
                                        }
                                    }
                                }
                            }).show()
                    }
                }
            }
        }
    }

    private fun unSetEquipment() {
        RetrofitManager.service.unSetEquipment(hashMapOf()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 200) {
                            showToast("Success")
                            /**解绑设备的同时需要清除本地缓存*/
                            MMKV.defaultMMKV().putString(KVKey.equipment, "")
                            BleManager.getInstance().disconnectAllDevice()
                            EventBus.getDefault().post(ReConnectEvent())
                        }
                    }
                }
            })


    }

    private fun setMyVideo(url: String) {
        RetrofitManager.service.setMyVideo(hashMapOf("videopath" to url))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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

    private fun unbind() {
        RetrofitManager.service.deletepanter(hashMapOf()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                override fun _onError(message: String?) {

                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SetTextI18n")
                override fun _onNext(t: BaseBean?) {
                    t?.let {
                        if (t.code == 200) {
                            showToast("Success")
                            ActivityUtils.finishAllActivities()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                        }
                    }
                }
            })
    }

    private fun editAvatar(avatar: String) {
        RetrofitManager.service.editavatar(hashMapOf("avatar" to avatar))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
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
                            EventBus.getDefault().post(UpdateHeadPicEvent())
                        }
                    }
                }
            })
    }

    override fun lazyLoad() {
    }


    override fun initListener() {
        not_disturb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (BleManager.getInstance().isConnected(BleTool.mBleDevice)) {
                MMKV.defaultMMKV().putBoolean(KVKey.not_disturb, isChecked)
                setShakeToDevice()
            } else {
                showToast("Please connect Bluetooth device")
            }
        }
        morse_model.setOnCheckedChangeListener { buttonView, isChecked ->
            val map = hashMapOf<String, String>()
            if (isChecked) {
                map["isonpress"] = 1.toString()
            } else {
                map["isonpress"] = 0.toString()
            }
            RetrofitManager.service.setIsonPressSendMorse(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : RxSubscriber<BaseBean?>(requireContext(), true) {
                    override fun _onError(message: String?) {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun _onNext(t: BaseBean?) {
                        if (t!!.code == 200) {
                            MMKV.defaultMMKV().putBoolean(KVKey.morse_model, isChecked)
                            showToast("Success")
                        }
                    }
                })
        }
        tv_logout.setOnClickListener {
            CustomDialog.build().setMaskColor(requireContext().getColor(R.color.dialogMaskColor))
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
                                val accessToken: AccessToken? =
                                    AccessToken.getCurrentAccessToken()
                                val isLoggedIn = accessToken != null && !accessToken.isExpired
                                if (isLoggedIn) {
                                    LoginManager.getInstance().logOut()
                                }
                                ActivityUtils.finishAllActivities()
                                startActivity(Intent(requireContext(), LoginActivity::class.java))
                            }
                        }
                    }
                }).show()

        }

        iv_head.setOnClickListener {

            startActivity(Intent(activity, EditInfoActivity::class.java))
            return@setOnClickListener

        }
    }

    private fun setShakeToDevice() {
        //给设备设置默认参数，灯光颜色，长短震，长短闪
        val long_shake = DeviceConfig.long_shake
        val short_shake = DeviceConfig.short_shake
        val long_flash = DeviceConfig.long_flash
        val short_flash = DeviceConfig.short_flash
        val shaking_levels = DeviceConfig.shaking_levels
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
            "02"
                    + lightColor
                    + binaryToHexString(binary_short_flash + binary_long_flash)
                    + vibration_intensity
                    + binaryToHexString(binary_short_shake + binary_long_shake)
        )

        BleTool.sendInstruct(
            "A5AAAC" + xorStr + "02"
                    + lightColor
                    + binaryToHexString(binary_short_flash + binary_long_flash)
                    + vibration_intensity
                    + binaryToHexString(binary_short_shake + binary_long_shake)
                    + "C5CCCA"
        )
    }

}