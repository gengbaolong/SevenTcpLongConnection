package com.seven;

import com.seven.gengbaolong.seventcplongconnection.bean.Server;

public class MyClass {

    public static void main(String[] args){
        int port = 22222;
        Server server = new Server(port);
        server.start();
    }

}
