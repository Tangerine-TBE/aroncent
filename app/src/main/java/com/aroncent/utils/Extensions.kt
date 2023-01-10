package com.aroncent.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.aroncent.app.KVKey
import com.aroncent.module.login.UserinfoBean
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tencent.bugly.proguard.t
import com.tencent.bugly.proguard.x
import com.tencent.mmkv.MMKV
import pub.devrel.easypermissions.EasyPermissions
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException


fun Fragment.showToast(content: String) {
    ToastUtils.make()
        .setDurationIsLong(false)
        .show(content)
}

private var toast: Toast? = null
fun Context.showToast(content: String) {
    ToastUtils.make()
        .setDurationIsLong(false)
        .show(content)
}
fun Context.showToastLong(content: String) {
    ToastUtils.make()
        .setGravity(Gravity.CENTER, 0, 0)
        .setDurationIsLong(true)
        .show(content)
}

fun getUserToken():String{
    return MMKV.defaultMMKV().decodeString(KVKey.TOKEN,"")
}

fun setUserInfoToSp(data : UserinfoBean){
    MMKV.defaultMMKV().encode(KVKey.TOKEN, data.token)
    MMKV.defaultMMKV().encode(KVKey.avatar,data.avatar)
    MMKV.defaultMMKV().encode(KVKey.username,data.username)
    MMKV.defaultMMKV().encode(KVKey.nickname,data.nickname)
    MMKV.defaultMMKV().encode(KVKey.partnerStatus,data.partnerstatus)
    MMKV.defaultMMKV().encode(KVKey.user_id,data.user_id)
    if (data.partnerstatus == "3"){
        MMKV.defaultMMKV().encode(KVKey.isBind,true)
    }else{
        MMKV.defaultMMKV().encode(KVKey.isBind,false)
    }
}

fun stringToMorseCode(str:String) :String{
    var morseStr = ""
    str.forEach {
        val char = if (it.toString() == "0") "• " else "▬ "
        morseStr += char
    }
    return morseStr
}

fun ImageView.setGlideResource(context: Context, resource: Int) {
    Glide.with(context).load(resource).into(this)
}
fun ImageView.setGlideResource(context: Context, url: String) {
    Glide.with(context).load(url).into(this)
}


fun Context.startActivity(className:Class<*>){
    startActivity(Intent(this,className))
}

fun View.dip2px(dipValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

fun View.px2dip(pxValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}


fun isAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
fun isAndroid11() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun hasPermission(context: Context,permission: String): Boolean {
    return EasyPermissions.hasPermissions(context, permission)
}
/*
*数字不足位数左补0
*
* @param str
* @param strLength
*/
fun addZeroForNum(string: String, strLength: Int): String {
    var str = string
    var strLen = str.length
    if (strLen < strLength) {
        while (strLen < strLength) {
            val sb = StringBuffer()
            sb.append("0").append(str) //左补0
            // sb.append(str).append("0");//右补0
            str = sb.toString()
            strLen = str.length
        }
    }
    return str
}

/**
 * 16进制转 length位数二进制
 * length :多少位
 * */
fun toBinary(num: String,length:Int) : String{
    return addZeroForNum(num.toInt(16).toString(2),length)
}

/**
 * 二进制转16进制
 * */
fun binaryToHexString(num: String):String{
    return num.toInt(2).toString(16)
}

fun isTopActivity(activity: Activity): Boolean {
    val am = activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val name  = am.getRunningTasks(1)[0].topActivity!!.className
    return name == activity::class.java.name
}















