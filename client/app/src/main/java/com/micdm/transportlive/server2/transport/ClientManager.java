package com.micdm.transportlive.server2.transport;

import android.os.Handler;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class ClientManager {

    public static interface OnConnectListener {
        public void onStartConnect();
        public void onCompleteConnect();
    }

    public static interface OnMessageListener {
        public void onMessage(String message);
    }

    private static final URI SERVER_URI = URI.create("ws://192.168.1.4/api/v2");

    private boolean needKeepConnect;

    private final OnConnectListener onConnectListener;
    private final OnMessageListener onMessageListener;

    private WebSocketClient client;

    public ClientManager(OnConnectListener onConnectListener, OnMessageListener onMessageListener) {
        this.onConnectListener = onConnectListener;
        this.onMessageListener = onMessageListener;
    }

//    public void connect() {
//        needKeepConnect = true;
//        onConnectListener.onStartConnect();
//        client = new WebSocketClient(SERVER_URI, new WebSocketClient.OnOpenListener() {
//            @Override
//            public void onOpen() {
//                onConnectListener.onCompleteConnect();
//            }
//        }, new WebSocketClient.OnMessageListener() {
//            @Override
//            public void onMessage(String message) {
//                onMessageListener.onMessage(message);
//            }
//        }, new WebSocketClient.OnCloseListener() {
//            @Override
//            public void onClose() {
//                if (needKeepConnect) {
//                    connect();
//                }
//            }
//        });
//        client.connect();
//    }
//
//    public void send(String message) {
//        if (client.getReadyState() != WebSocket.READYSTATE.OPEN) {
//            throw new RuntimeException("client isn't ready");
//        }
//        client.send(message);
//    }
//
//    public void disconnect() {
//        needKeepConnect = false;
//        client.close();
//        client = null;
//    }

    public void connect() {
        onConnectListener.onStartConnect();
        Timer timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onConnectListener.onCompleteConnect();
                    }
                });
            }
        }, 3000);
    }

    public void send(String message) {

    }

    public void disconnect() {

    }
}
