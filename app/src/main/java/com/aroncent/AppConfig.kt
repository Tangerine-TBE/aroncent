package com.aroncent

import com.aroncent.app.KVKey
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.PathUtils
import com.tencent.mmkv.MMKV


/**
 * URL常量
 */
object AppConfig {

    enum class EnvironmentEnum {
        RELEASE,
        UAT
    }

    @JvmStatic
    val lang : String
    get() {
        return MMKV.mmkvWithID("language").getString(KVKey.SP_LANGUAGE, LanguageUtils.getSystemLanguage().language)!!
    }

    @JvmStatic
    val Environment: EnvironmentEnum
        get() {
            return when (BuildConfig.env) {
                "aroncent" -> EnvironmentEnum.RELEASE
                "uat" -> EnvironmentEnum.UAT
                else -> EnvironmentEnum.UAT
            }
        }

    @JvmStatic
    val BASE_URL: String
        get() {
            return when (Environment) {
                EnvironmentEnum.RELEASE -> "http://oss.ltwoo-app.top/"
                EnvironmentEnum.UAT -> "http://oss.ltwoo-app.top/"
            }
        }


    @JvmStatic
    val app_version: String
    get() {
        return "v"+AppUtils.getAppVersionName()
    }


    @JvmStatic
    val updateFilePath: String //更新固件文件的存储路径
        get() {
            return PathUtils.getInternalAppCachePath() + "/" + "XUpdate.bin"
        }
}
