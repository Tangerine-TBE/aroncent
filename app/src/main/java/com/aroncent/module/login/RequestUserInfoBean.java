package com.aroncent.module.login;

public class RequestUserInfoBean {
    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public UserinfoBean userinfo;
    }
}
