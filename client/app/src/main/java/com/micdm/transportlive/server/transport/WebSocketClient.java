package com.micdm.transportlive.server.transport;

import android.os.Handler;

import org.java_websocket.drafts.Draft_17;
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

    private static final URI SERVER_URI = URI.create("ws://192.168.1.5:8001/api/v2");
    private static final int TIMEOUT = 10;

    private static final Handler handler = new Handler();

    private final OnOpenListener onOpenListener;
    private final OnMessageListener onMessageListener;
    private final OnCloseListener onCloseListener;

    public WebSocketClient(OnOpenListener onOpenListener, OnMessageListener onMessageListener, OnCloseListener onCloseListener) {
        super(SERVER_URI, new Draft_17(), null, TIMEOUT);
        this.onOpenListener = onOpenListener;
        this.onMessageListener = onMessageListener;
        this.onCloseListener = onCloseListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onOpenListener.onOpen();
            }
        });
    }

    @Override
    public void onMessage(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onMessageListener.onMessage(message);
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCloseListener.onClose();
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        close();
    }
}
