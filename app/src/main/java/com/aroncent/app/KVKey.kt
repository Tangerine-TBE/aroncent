package com.aroncent.app

/**
 * @Author:         Alex
 * @CreateTime:     2020/5/12
 * @Explain :
 */

object KVKey {
    const val SP_LANGUAGE = "SP_LANGUAGE"
    const val SP_COUNTRY = "SP_COUNTRY"

    //硬件设备配置
    const val light_color = "light_color"
    const val long_shake = "long_shake"
    const val short_shake = "short_shake"
    const val long_flash = "long_flash"
    const val short_flash = "short_flash"
    const val shaking_levels = "shaking_levels"
    const val loop_number = "loop_number" //循环次数
    const val equipment = "equipment"
    const val not_disturb = "not_disturb"
    const val morse_model = "morse_model"

    //用户信息
    const val TOKEN = "token"
    const val PASS_WORLD =""
    const val avatar = "avatar"
    const val username = "username"
    const val nickname = "nickname"
    const val user_id = "user_id"
    const val email = "email"

    //用户伴侣信息
    const val partner_avatar = "partner_avatar"
    const val partner_nickname = "partner_nickname"

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
    const val isBind = "isBind" //是否绑定了情侣
}