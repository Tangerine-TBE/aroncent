package com.aroncent.api

import com.aroncent.base.BaseBean
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {


    /**设置密码**/
    @FormUrlEncoded
    @POST("user/operation/v1/setPassword")
    fun setPassword(@FieldMap map: HashMap<String, String>) : Observable<BaseBean>


}
