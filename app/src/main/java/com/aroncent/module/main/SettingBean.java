package com.aroncent.module.main;

public class SettingBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public int id;
        public int userid;
        public String long_shake;
        public String short_shake;
        public String long_light;
        public String short_light;
        public String lightcolor;
        public String equipment;
    }
}
