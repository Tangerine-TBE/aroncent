package com.aroncent.module.login;

public class RegisterBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean {
        public UserinfoBean userinfo;

        public static class UserinfoBean {
            public int id;
            public String username;
            public String nickname;
            public String mobile;
            public String avatar;
            public int score;
            public String token;
            public int user_id;
            public int createtime;
            public int expiretime;
            public int expires_in;
        }
    }
}
