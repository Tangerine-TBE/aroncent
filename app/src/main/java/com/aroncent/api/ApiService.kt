package com.aroncent.api

import com.aroncent.base.BaseBean
import com.aroncent.module.login.CountryListBean
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
    fun register(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**登录**/
    @FormUrlEncoded
    @POST("api/user/login")
    fun login(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

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
    @GET("addons/litestore/api.usersettings/getsettings")
    fun getsettings(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**添加摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/addssphrase")
    fun addssphrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**修改摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/editphrase")
    fun editphrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**删除摩斯短语**/
    @FormUrlEncoded
    @POST("addons/litestore/api.userphrase/delete")
    fun deletephrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取摩斯短语模板列表记录**/
    @GET("addons/litestore/api.morsecode/lists")
    fun getMorseCodeList(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**使用常用短语方式发送**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/send")
    fun sendPhrase(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**使用手环按键摩斯方式发送**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/codesend")
    fun sendPhraseCode(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**修改发送记录标题**/
    @FormUrlEncoded
    @POST("addons/litestore/api.Sendphrase/updatehistory")
    fun updateHistory(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**上传文件**/
    @FormUrlEncoded
    @POST("api/Common/upload")
    fun upload(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**修改个人头像**/
    @FormUrlEncoded
    @POST("api/User/editavatar")
    fun editavatar(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>

    /**获取注册时的国家地区列表**/
    @FormUrlEncoded
    @POST("api/Country/getList")
    fun getCountryList(@FieldMap map: HashMap<String, String>) : Observable<CountryListBean>
}
