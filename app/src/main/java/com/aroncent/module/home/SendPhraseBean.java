package com.aroncent.module.home;

public class SendPhraseBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String content;
        public String shake;
        public String color;
        public int userid;
        public int partnerid;
        public int createtime;
        public int updatetime;
    }
}
