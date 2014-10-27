package com.micdm.transportlive.server.transport;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.micdm.transportlive.R;
import com.micdm.transportlive.misc.Utils;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_17;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ClientManager {

    public static interface OnConnectListener {
        public void onStartConnect(int tryNumber);
        public void onCompleteConnect();
    }

    public static interface OnMessageListener {
        public void onMessage(String message);
    }

    private static final URI SERVER_URI = URI.create("ws://transport-live.tom.ru/api/v2");
    private static final int TIMEOUT = 10;
    private static final int RETRY_INTERVAL = 3;

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
            if (!needKeepConnect) {
                return;
            }
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

    private final Context context;
    private final OnConnectListener onConnectListener;
    private final OnMessageListener onMessageListener;

    private WebSocketClient client;

    public ClientManager(Context context, OnConnectListener onConnectListener, OnMessageListener onMessageListener) {
        this.context = context;
        this.onConnectListener = onConnectListener;
        this.onMessageListener = onMessageListener;
    }

    public void connect() {
        needKeepConnect = true;
        tryNumber += 1;
        onConnectListener.onStartConnect(tryNumber);
        client = new WebSocketClient(SERVER_URI, new Draft_17(), getRequestHeaders(), TIMEOUT, onClientOpenListener, onClientMessageListener, onClientCloseListener);
        client.connect();
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", context.getString(R.string.__user_agent, Utils.getAppVersion(context), Build.VERSION.RELEASE));
        return headers;
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
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
