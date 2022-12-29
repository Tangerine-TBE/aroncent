package com.ltwoo.estep.api.exception

import android.content.Context
import com.aroncent.api.exception.ErrorStatus
import com.google.gson.JsonParseException
import com.ltwoo.estep.R
import com.orhanobut.logger.Logger

import org.json.JSONException

import java.net.ConnectException

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * Created by xuhao on 2017/12/5.
 * desc: 异常处理类
 */

class ExceptionHandle {

    companion object {

        fun handleException(context: Context,e: Throwable): String {
            var errorCode = ErrorStatus.UNKNOWN_ERROR
            val errorMsg: String
            e.printStackTrace()
            if (e is SocketTimeoutException) {//网络超时
                Logger.e("TAG", "网络连接异常: " + e.message)
                errorMsg = context.getString(R.string.net_error_text1)
                errorCode = ErrorStatus.NETWORK_ERROR
            } else if (e is ConnectException) { //均视为网络错误
                Logger.e("TAG", "网络连接异常: " + e.message)
                errorMsg = context.getString(R.string.net_error_text2)
                errorCode = ErrorStatus.NETWORK_ERROR
            } else if (e is JsonParseException
                || e is JSONException
                || e is ParseException
            ) {   //均视为解析错误
                Logger.e("TAG", "数据解析异常: " + e.message)
                errorMsg = context.getString(R.string.net_error_text3)
                errorCode = ErrorStatus.SERVER_ERROR
            } else if (e is ApiException) {//服务器返回的错误信息
                errorMsg = e.message.toString()
                errorCode = ErrorStatus.SERVER_ERROR
            } else if (e is UnknownHostException) { //未知主机异常
                Logger.e("TAG", "网络连接异常: " + e.message)
                errorMsg = context.getString(R.string.net_error_text1)
                errorCode = ErrorStatus.NETWORK_ERROR
            } else if (e is IllegalArgumentException) {
                errorMsg = context.getString(R.string.net_error_text4)
                errorCode = ErrorStatus.SERVER_ERROR
            } else {//未知错误
                try {
                    Logger.e("TAG", "错误: " + e.message)
                } catch (e1: Exception) {
                    Logger.e("TAG", "未知错误Debug调试 ")
                }

                errorMsg = context.getString(R.string.net_error_text)
                errorCode = ErrorStatus.UNKNOWN_ERROR
            }
            return errorMsg
        }

    }


}
