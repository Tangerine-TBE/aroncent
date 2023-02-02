package com.aroncent.module.main;

public class PartnerInfoBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String avatar;
        public String nickname;
        public String url;
    }
}
