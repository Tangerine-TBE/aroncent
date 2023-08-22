package com.aroncent.event;

public class ReadMsgEvent {
    public String msgId;
    public String content;

    public ReadMsgEvent(String msgId, String content) {
        this.msgId = msgId;
        this.content = content;
    }
}
