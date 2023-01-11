package com.aroncent.base;

public class UploadBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public String url;
        public String fullurl;
    }
}
