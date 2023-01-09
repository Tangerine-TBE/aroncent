package com.aroncent.module.history;

import com.aroncent.app.KVKey;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.tencent.mmkv.MMKV;

import java.util.List;

public class HistoryListBean {

    public int code;
    public String msg;
    public String time;
    public DataBean data;

    public static class DataBean  {
        public List<ListBean> list;


        public static class ListBean implements MultiItemEntity{
            public int id;
            public int phraseid;
            public int isread; //0未读，1已读
            public String createtime;
            public String updatetime;
            public int userid;
            public int partnerid;
            public String content;
            public String morsecode;

            @Override
            public int getItemType() {
                return userid == MMKV.defaultMMKV().decodeInt(KVKey.user_id,0) ? 1 : 0;
            }
        }
    }
}
