package com.seven.gengbaolong.seventcplongconnection.bean;

import android.util.Log;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端实体
 * Created by gengbaolong on 2017/7/15.
 */

public class Client {

    public static String tag = Client.class.getSimpleName();

    private String serverIp;
    private int port;
    private Socket socket;
    private boolean running=false;
    private long lastSendTime;
    private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();

    /**
     * 处理服务端返回的对象，可实现该接口。
     */
    public static interface ObjectAction{
        void doAction(Object obj,Client client);
    }
    public static final class DefaultObjectAction implements ObjectAction{
        public void doAction(Object obj,Client client) {
            Log.e(tag, "处理：\t" + obj.toString());
        }
    }

    public Client(String serverIp, int port){
        this.serverIp=serverIp;
        this.port=port;
    }

    public void start(){
        try{
            Log.e(tag, "client start()");
            socket = new Socket(serverIp, port);
            lastSendTime = System.currentTimeMillis();
            running = true;
            //开启心跳监听线程
            new Thread(new HeartbeatRunnable()).start();
            //开启接收消息监听线程
            new Thread(new ReceiveMsgRunnable()).start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        if(running){
            running = false;
        }
    }

    /**
     * 添加接收对象的处理对象。
     * @param cls 待处理的对象，其所属的类。
     * @param action 处理过程对象。
     */
    public void addActionMap(Class<Object> cls,ObjectAction action){//服务端可能返回不同的对象,而对于返回对象的处理，要编写具体的ObjectAction实现类进行处理。
        actionMapping.put(cls, action);
    }

    //向服务端发送数据
    public void sendObject(Object obj){
        ObjectOutputStream oos = null;
        try{
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(obj);
            oos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public class HeartbeatRunnable implements Runnable{
        //延迟时间
        long checkDelay = 10;
        //间隔时间，也就是当2s之内没有数据交互，那么客户端就需要发送心跳包给服务端
        long intervalDelay = 2000;

        @Override
        public void run() {
            Log.e(tag, "client HeartbeatRunnable run() "+running);
            while (running){
                //如果当前时间距离最后发送数据的时间间隔大于定义好的时间间隔，就发送心跳
                if(System.currentTimeMillis() - lastSendTime > intervalDelay){
                    try{
                        Log.e(tag, "send heart-beat msg");
                        Client.this.sendObject(new HeartbeatBean());
                    }catch (Exception e){
                        e.printStackTrace();
                        Client.this.stop();
                    }
                    lastSendTime = System.currentTimeMillis();
                }else{

                    try {
                        Thread.sleep(checkDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Client.this.stop();
                    }

                }
            }
        }
    }

    public class ReceiveMsgRunnable implements Runnable{

        @Override
        public void run() {
            Log.e(tag, "client ReceiveMsgRunnable run() "+running);
            while (running){
                try{
                    if(null!=socket){
                        InputStream in = socket.getInputStream();
                        if(in.available() > 0 ){
                            ObjectInputStream ois = new ObjectInputStream(in);
                            Object obj = ois.readObject();
                            System.out.println("client receive：\t"+obj);
                            //客户端接收到对象后，将对象放入Map中
                            ObjectAction objectAction = actionMapping.get(obj.getClass());
                            objectAction = ( objectAction==null? new DefaultObjectAction() : objectAction);
                            objectAction.doAction(obj, Client.this);
                        }else{
                            Thread.sleep(10);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Client.this.stop();
                }
            }
        }
    }

}
