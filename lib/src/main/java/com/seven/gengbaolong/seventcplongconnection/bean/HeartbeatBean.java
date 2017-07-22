package com.seven.gengbaolong.seventcplongconnection.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 客户端发给服务端的心跳包
 * Created by gengbaolong on 2017/7/15.
 */

public class HeartbeatBean implements Serializable{

    private static final long serialVersionUID = -1L;

    @Override
    public String toString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\t心跳包";
    }
}
