package com.aroncent.api

import com.aroncent.base.BaseBean
import com.aroncent.base.UploadBean
import com.aroncent.module.history.HistoryListBean
import com.aroncent.module.home.MorseCodeListBean
import com.aroncent.module.home.SendPhraseBean
import com.aroncent.module.home.UserPhraseListBean
import com.aroncent.module.login.CountryListBean
import com.aroncent.module.login.RequestUserInfoBean
import com.aroncent.module.main.SettingBean
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    /**发送邮箱验证码**/
    @FormUrlEncoded
    @POST("api/ems/send")
    fun sendEms(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**注册**/
    @FormUrlEncoded
    @POST("api/user/register")
    fun register(@FieldMap map: HashMap<String, String>) : Observable<RequestUserInfoBean>

    /**登录**/
    @FormUrlEncoded
    @POST("api/user/login")
    fun login(@FieldMap map: HashMap<String, String>) : Observable<RequestUserInfoBean>

    /**修改极光或onesignal注册ID**/
    @FormUrlEncoded
    @POST("api/user/updaterid")
    fun updaterid(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**重置密码**/
    @FormUrlEncoded
    @POST("api/user/resetpwd")
    fun resetpwd(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**绑定FACEBOOK帐号**/
    @FormUrlEncoded
    @POST("api/user/bindfacebook")
    fun bindfacebook(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**添加情侣帐号**/
    @FormUrlEncoded
    @POST("addons/litestore/api.partner/add")
    fun addPartner(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**设置个人手环震动**/
    @FormUrlEncoded
    @POST("addons/litestore/api.usersettings/setshake")
    fun setshake(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**设置个人手环灯光颜色**/
    @FormUrlEncoded
    @POST("addons/litestore/api.usersettings/setlightcolor")
    fun setlightcolor(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取个人硬件设置的默认信息**/
    @FormUrlEncoded
    @POST("addons/litestore/api.usersettings/getsettings")
    fun getsettings(@FieldMap map: HashMap<String, String>) : Observable<SettingBean>

    /**添加摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/addphrase")
    fun addPhrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**修改摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/editphrase")
    fun editphrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**删除摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/delete")
    fun deletephrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取摩斯短语模板列表记录**/
    @FormUrlEncoded
    @POST("addons/litestore/api.morsecode/lists")
    fun getMorseCodeList(@FieldMap map: HashMap<String, String>) : Observable<MorseCodeListBean>

    /**使用常用短语方式发送**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/send")
    fun sendPhrase(@FieldMap map: HashMap<String, String>) : Observable<SendPhraseBean>

    /**使用手环按键摩斯方式发送**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/codesend")
    fun sendMorseCode(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**修改发送记录标题**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/updatehistory")
    fun updateHistory(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**上传文件**/
    @Multipart
    @POST("api/Common/upload")
    fun upload(@Part list : List<MultipartBody.Part>) : Observable<UploadBean>

    /**修改个人头像**/
    @FormUrlEncoded
    @POST("api/User/editavatar")
    fun editavatar(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取注册时的国家地区列表**/
    @FormUrlEncoded
    @POST("api/Country/getList")
    fun getCountryList(@FieldMap map: HashMap<String, String>) : Observable<CountryListBean>

    /**检查是否有需要审核好友请求**/
    @FormUrlEncoded
    @POST("addons/litestore/api.partner/getrequest")
    fun getPartnerRequest(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**审核好友请求**/
    @FormUrlEncoded
    @POST("addons/litestore/api.partner/confirmemail")
    fun confirmEmail(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**审核好友请求**/
    @FormUrlEncoded
    @POST("api/user/getuserinfoapp")
    fun getUserInfo(@FieldMap map: HashMap<String, String>) : Observable<RequestUserInfoBean>

    /**获取用户聊天记录**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/gethistory")
    fun getHistory(@FieldMap map: HashMap<String, String>) : Observable<HistoryListBean>

    /**标记摩斯消息为已读**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/read")
    fun readMsg(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**设置设备蓝牙名称**/
    @FormUrlEncoded
    @POST("addons/litestore/api.usersettings/setequipment")
    fun setEquipment(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**上传及设置个人介绍视频**/
    @FormUrlEncoded
    @POST("api/user/setmyvideo")
    fun setMyVideo(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取用户个人视频介绍**/
    @FormUrlEncoded
    @POST("api/user/getvideo")
    fun getVideo(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取个人及伙伴定义的摩斯列表**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Userphrase/lists")
    fun getUserPhraseList(@FieldMap map: HashMap<String, String>) : Observable<UserPhraseListBean>

    /**发起解绑情侣帐号请求(对方判断解绑请求状态为partnerstatus=5**/
    @FormUrlEncoded
    @POST("addons/litestore/api.partner/deletepanter")
    fun deletepanter(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**解绑对方发起帐号审核**/
    @FormUrlEncoded
    @POST("addons/litestore/api.partner/deleteconfirm")
    fun deletePartnerConfirm(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>
}
