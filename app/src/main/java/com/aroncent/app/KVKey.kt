package com.aroncent.app

/**
 * @Author:         Alex
 * @CreateTime:     2020/5/12
 * @Explain :
 */

object KVKey {
    const val SP_LANGUAGE = "SP_LANGUAGE"
    const val SP_COUNTRY = "SP_COUNTRY"

    const val light_color = "light_color"
    const val long_shake = "long_shake"
    const val short_shake = "short_shake"
    const val long_flash = "long_flash"
    const val short_flash = "short_flash"
    const val TOKEN = "token"
    const val avatar = "avatar"
    const val username = "username"
    const val nickname = "nickname"

    /**
     * 情侣的状态
     *0：默认状态或者为审核不通过
     *1：对方正在对申请要求进行审核
     *2：有好友申请请求
     *3：已绑定好友
     *4: 对方正在对申请的好友删除审核中
     *5：有好友要求删除要求需要确认
     * */
    const val partnerStatus = "partnerstatus"
    const val hasBle = "hasBle" //是否添加了蓝牙设备
    const val isBind = "isBind" //是否绑定了情侣
}