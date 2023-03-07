package com.aroncent.module.login;

import java.util.List;

public class CountryListBean {

    public int code;
    public String msg;
    public String time;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public String country;
        public boolean isCheck = false;

        public DataBean(int id, String country) {
            this.id = id;
            this.country = country;
        }
    }
}
