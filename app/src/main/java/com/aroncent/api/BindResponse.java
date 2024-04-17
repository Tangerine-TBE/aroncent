package com.aroncent.api;

public class BindResponse {
    private String msg;
    private long code;
    private Data data;
    private String time;

    public String getMsg() { return msg; }
    public void setMsg(String value) { this.msg = value; }

    public long getCode() { return code; }
    public void setCode(long value) { this.code = value; }

    public Data getData() { return data; }
    public void setData(Data value) { this.data = value; }

    public String getTime() { return time; }
    public void setTime(String value) { this.time = value; }

}
class Data {
    private String facebookaccount;

    public String getFacebookaccount() { return facebookaccount; }
    public void setFacebookaccount(String value) { this.facebookaccount = value; }
}