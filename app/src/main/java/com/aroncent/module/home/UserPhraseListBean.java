package com.aroncent.module.home;

import java.util.List;

public class UserPhraseListBean {

    public int code;
    public String msg;
    public String time;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public int userid;
        public int partnerid;
        public String content;
        public String color;
        public String shake;
        public String createtime;
        public String updatetime;
        public String morseword;
    }
}
