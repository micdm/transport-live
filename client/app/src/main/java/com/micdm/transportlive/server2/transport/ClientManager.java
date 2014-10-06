package com.micdm.transportlive.server2.transport;

import org.java_websocket.WebSocket;

import java.net.URI;

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

    public void connect() {
        needKeepConnect = true;
        onConnectListener.onStartConnect();
        client = new WebSocketClient(SERVER_URI, new WebSocketClient.OnOpenListener() {
            @Override
            public void onOpen() {
                onConnectListener.onCompleteConnect();
            }
        }, new WebSocketClient.OnMessageListener() {
            @Override
            public void onMessage(String message) {
                onMessageListener.onMessage(message);
            }
        }, new WebSocketClient.OnCloseListener() {
            @Override
            public void onClose() {
                if (needKeepConnect) {
                    connect();
                }
            }
        });
        client.connect();
    }

    public void send(String message) {
        if (client.getReadyState() != WebSocket.READYSTATE.OPEN) {
            throw new RuntimeException("client isn't ready");
        }
        client.send(message);
    }

    public void disconnect() {
        needKeepConnect = false;
        client.close();
        client = null;
    }
}
