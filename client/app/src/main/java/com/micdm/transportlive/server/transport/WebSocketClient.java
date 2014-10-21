package com.micdm.transportlive.server.transport;

import android.os.Handler;

import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

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

    private static final Handler handler = new Handler();

    private final OnOpenListener onOpenListener;
    private final OnMessageListener onMessageListener;
    private final OnCloseListener onCloseListener;

    public WebSocketClient(URI serverUri, Draft draft, Map<String,String> headers, int timeout, OnOpenListener onOpenListener,
                           OnMessageListener onMessageListener, OnCloseListener onCloseListener) {
        super(serverUri, draft, headers, timeout);
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
