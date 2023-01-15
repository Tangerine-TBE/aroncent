package com.aroncent.jpush;

public class PushMsgBean {

    public KeyBean key;

    public static class KeyBean {
        public String infotype;
        public String morsecode;
        public String infoid;

        @Override
        public String toString() {
            return "KeyBean{" +
                    "infotype='" + infotype + '\'' +
                    ", morsecode='" + morsecode + '\'' +
                    ", infoid='" + infoid + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PushMsgBean{" +
                "key=" + key +
                '}';
    }
}
