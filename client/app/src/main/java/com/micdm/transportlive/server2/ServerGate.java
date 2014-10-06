package com.micdm.transportlive.server2;

import android.content.Context;
import android.os.Build;

import com.micdm.transportlive.R;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.server2.converters.IncomingMessageConverter;
import com.micdm.transportlive.server2.converters.OutcomingMessageConverter;
import com.micdm.transportlive.server2.messages.Message;
import com.micdm.transportlive.server2.messages.outcoming.GreetingMessage;
import com.micdm.transportlive.server2.transport.ClientManager;

public class ServerGate {

    public static interface OnConnectListener {
        public void onStartConnect();
        public void OnCompleteConnect();
    }

    public static interface OnMessageListener {
        public void onMessage(Message message);
    }

    private final IncomingMessageConverter incomingMessageConverter = new IncomingMessageConverter();
    private final OutcomingMessageConverter outcomingMessageConverter = new OutcomingMessageConverter();

    private final Context context;

    private OnConnectListener onConnectListener;
    private OnMessageListener onMessageListener;

    private final ClientManager clientManager = new ClientManager(new ClientManager.OnConnectListener() {
        @Override
        public void onStartConnect() {
            onConnectListener.onStartConnect();
        }
        @Override
        public void onCompleteConnect() {
            sendGreeting();
            onConnectListener.OnCompleteConnect();
        }
    }, new ClientManager.OnMessageListener() {
        @Override
        public void onMessage(String message) {
            handleMessage(message);
        }
    });

    public ServerGate(Context context) {
        this.context = context;
    }

    public void connect(OnConnectListener onConnectListener, OnMessageListener onMessageListener) {
        this.onConnectListener = onConnectListener;
        this.onMessageListener = onMessageListener;
        clientManager.connect();
    }

    public void disconnect() {
        clientManager.disconnect();
    }

    private void sendGreeting() {
        String version = context.getString(R.string.__user_agent, Utils.getAppVersion(context), Build.VERSION.RELEASE);
        Message message = new GreetingMessage(version);
        clientManager.send(outcomingMessageConverter.convert(message));
    }

    private void handleMessage(String text) {
        Message message = incomingMessageConverter.convert(text);
        onMessageListener.onMessage(message);
    }
}
