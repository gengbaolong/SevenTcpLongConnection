package com.seven.gengbaolong.seventcplongconnection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.seven.gengbaolong.seventcplongconnection.bean.Client;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    public String tag = MainActivity.class.getSimpleName();
    private Button client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = (Button) findViewById(R.id.client);
        client.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.client://客户端
                client();
                break;
            default:
                break;
        }
    }


    private void client() {
        String serverIp = "192.168.1.10";
        int port = 22222;
        final Client client = new Client(serverIp,port);
        new Thread(){
            @Override
            public void run() {
                Log.e(tag, "客户端启动");
                client.start();
            }
        }.start();
    }
}
