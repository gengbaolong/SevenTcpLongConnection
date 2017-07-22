package com.seven.gengbaolong.seventcplongconnection.bean;


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端实体
 * Created by gengbaolong on 2017/7/15.
 */

public class Server {

    public static String tag = Server.class.getSimpleName();

    private int port;
    private volatile boolean running=false;
    private long receiveTimeDelay=3000;
    private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();
    private Thread connWatching;



    /**
     * 要处理客户端发来的对象，并返回一个对象，可实现该接口。
     */
    public interface ObjectAction{
        Object doAction(Object rev);
    }

    public static final class DefaultObjectAction implements ObjectAction{
        public Object doAction(Object rev) {
            System.out.println("handle and back：" + rev);
            return rev;
        }
    }

    public Server(int port) {
        this.port = port;
    }

    public void start(){
        if(running)
            return;
        running=true;
        connWatching = new Thread(new ConnWatchingRunnable());
        connWatching.start();
    }

    @SuppressWarnings("deprecation")
    public void stop(){
        if(running) {
            running = false;
        }
        if(connWatching!=null) {
            connWatching.stop();
        }
    }

    //客户端可能发过来多个不同的对象，服务端接收到不同的对象，需要做相应的回调处理。
    public void addActionMap(Class<Object> cls,ObjectAction action){
        actionMapping.put(cls, action);
    }


    //处理连接的线程
    class ConnWatchingRunnable implements Runnable{
        public void run(){
            System.out.println("server ConnWatchingRunnable run() "+running);
            try {
                ServerSocket ss = new ServerSocket(port);
                System.out.println("server ConnWatchingRunnable run() has received client socket");
                while(running){
                    Socket s = ss.accept();
                    System.out.println("server ConnWatchingRunnable run() is handling client socket");
                    new Thread(new SocketAction(s)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Server.this.stop();
            }

        }
    }

    //维持连接的线程
    class SocketAction implements Runnable{
        Socket socket;
        boolean run=true;
        long lastReceiveTime = System.currentTimeMillis();
        public SocketAction(Socket s) {
            this.socket = s;
        }
        public void run() {
            System.out.println("server SocketAction run() "+running);
            while(running && run){
                if(System.currentTimeMillis()-lastReceiveTime>receiveTimeDelay){
                    System.out.println("server SocketAction run() overThis()");
                    overThis();
                }else{
                    System.out.println("server SocketAction run() else");
                    try {
                        InputStream in = socket.getInputStream();
                        if(in.available()>0){
                            ObjectInputStream ois = new ObjectInputStream(in);
                            Object obj = ois.readObject();
                            lastReceiveTime = System.currentTimeMillis();
                            System.out.println("receive：\t"+obj);
                            ObjectAction objectAction = actionMapping.get(obj.getClass());
                            objectAction = (objectAction==null? new DefaultObjectAction():objectAction);
                            //处理接收到的客户端数据
                            Object out = objectAction.doAction(obj);
                            if(out!=null){
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                oos.writeObject(out);
                                oos.flush();
                            }
                        }else{
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        overThis();
                    }
                }
            }
        }

        private void overThis() {
            if(run) {
                run = false;
            }
            if(null != socket){
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("server sclose："+socket.getRemoteSocketAddress());
        }
    }

}
