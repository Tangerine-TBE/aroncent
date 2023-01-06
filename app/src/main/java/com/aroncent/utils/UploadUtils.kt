package com.aroncent.utils

import com.aroncent.base.BaseBean
import com.aroncent.base.RxSubscriber
import com.ltwoo.estep.api.RetrofitManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object UploadUtils {
    //单文件上传
    fun uploadFile(file: File, callback: RxSubscriber<BaseBean>) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        builder.addPart(MultipartBody.Part.createFormData("Filename", file.name, requestBody))
        builder.addFormDataPart("token", getUserToken())

        RetrofitManager.service.upload(builder.build().parts())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(callback)
    }

}