package com.micdm.transportlive.server.transport;

import android.os.Handler;

import org.java_websocket.WebSocket;

public class ClientManager {

    public static interface OnConnectListener {
        public void onStartConnect(int tryNumber);
        public void onCompleteConnect();
    }

    public static interface OnMessageListener {
        public void onMessage(String message);
    }

    private static final int RETRY_INTERVAL = 5;

    private static final Handler handler = new Handler();

    private final WebSocketClient.OnOpenListener onClientOpenListener = new WebSocketClient.OnOpenListener() {
        @Override
        public void onOpen() {
            tryNumber = 0;
            onConnectListener.onCompleteConnect();
        }
    };
    private final WebSocketClient.OnMessageListener onClientMessageListener = new WebSocketClient.OnMessageListener() {
        @Override
        public void onMessage(String message) {
            onMessageListener.onMessage(message);
        }
    };
    private final WebSocketClient.OnCloseListener onClientCloseListener = new WebSocketClient.OnCloseListener() {
        @Override
        public void onClose() {
            reconnectCallback = new Runnable() {
                @Override
                public void run() {
                    reconnectCallback = null;
                    if (needKeepConnect) {
                        connect();
                    }
                }
            };
            handler.postDelayed(reconnectCallback, RETRY_INTERVAL * 1000);
        }
    };

    private boolean needKeepConnect;
    private int tryNumber;
    private Runnable reconnectCallback;

    private final OnConnectListener onConnectListener;
    private final OnMessageListener onMessageListener;

    private WebSocketClient client;

    public ClientManager(OnConnectListener onConnectListener, OnMessageListener onMessageListener) {
        this.onConnectListener = onConnectListener;
        this.onMessageListener = onMessageListener;
    }

    public void connect() {
        needKeepConnect = true;
        if (reconnectCallback != null) {
            handler.removeCallbacks(reconnectCallback);
            reconnectCallback = null;
        }
        tryNumber += 1;
        onConnectListener.onStartConnect(tryNumber);
        client = new WebSocketClient(onClientOpenListener, onClientMessageListener, onClientCloseListener);
        client.connect();
    }

    public void send(String message) {
        if (client != null && client.getReadyState() == WebSocket.READYSTATE.OPEN) {
            client.send(message);
        }
    }

    public void disconnect() {
        needKeepConnect = false;
        if (reconnectCallback != null) {
            handler.removeCallbacks(reconnectCallback);
            reconnectCallback = null;
        }
        client.close();
        client = null;
    }
}
