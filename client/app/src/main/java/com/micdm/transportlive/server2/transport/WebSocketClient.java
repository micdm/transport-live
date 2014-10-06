package com.micdm.transportlive.server2.transport;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    public static interface OnOpenListener {
        public void onOpen();
    }

    public static interface OnMessageListener {
        public void onMessage(String message);
    }

    public static interface OnCloseListener {
        public void onClose();
    }

    private final OnOpenListener onOpenListener;
    private final OnMessageListener onMessageListener;
    private final OnCloseListener onCloseListener;

    public WebSocketClient(URI serverURI, OnOpenListener onOpenListener, OnMessageListener onMessageListener, OnCloseListener onCloseListener) {
        super(serverURI);
        this.onOpenListener = onOpenListener;
        this.onMessageListener = onMessageListener;
        this.onCloseListener = onCloseListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        onOpenListener.onOpen();
    }

    @Override
    public void onMessage(String message) {
        onMessageListener.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        onCloseListener.onClose();
    }

    @Override
    public void onError(Exception ex) {
        close();
    }
}
