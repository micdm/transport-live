package com.micdm.transportlive.server;

import android.content.Context;
import android.os.Build;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.server.converters.IncomingMessageConverter;
import com.micdm.transportlive.server.converters.OutcomingMessageConverter;
import com.micdm.transportlive.server.messages.Message;
import com.micdm.transportlive.server.messages.outcoming.GreetingMessage;
import com.micdm.transportlive.server.messages.outcoming.LoadNearestStationsMessage;
import com.micdm.transportlive.server.messages.outcoming.SelectRouteMessage;
import com.micdm.transportlive.server.messages.outcoming.SelectStationMessage;
import com.micdm.transportlive.server.messages.outcoming.UnselectRouteMessage;
import com.micdm.transportlive.server.messages.outcoming.UnselectStationMessage;
import com.micdm.transportlive.server.transport.ClientManager;

import java.math.BigDecimal;

public class ServerGate {

    public static interface OnConnectListener {
        public void onStartConnect(int tryNumber);
        public void OnCompleteConnect();
    }

    public static interface OnMessageListener {
        public void onMessage(Message message);
    }

    private final IncomingMessageConverter incomingMessageConverter = new IncomingMessageConverter();
    private final OutcomingMessageConverter outcomingMessageConverter = new OutcomingMessageConverter();

    private final Context context;

    private final ClientManager clientManager;
    private OnConnectListener onConnectListener;
    private OnMessageListener onMessageListener;

    public ServerGate(Context context) {
        this.context = context;
        clientManager = new ClientManager(context, new ClientManager.OnConnectListener() {
            @Override
            public void onStartConnect(int tryNumber) {
                onConnectListener.onStartConnect(tryNumber);
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
    }

    public void connect(OnConnectListener onConnectListener, OnMessageListener onMessageListener) {
        this.onConnectListener = onConnectListener;
        this.onMessageListener = onMessageListener;
        clientManager.connect();
    }

    private void sendGreeting() {
        String version = context.getString(R.string.__user_agent, Utils.getAppVersion(context), Build.VERSION.RELEASE);
        send(new GreetingMessage(version));
    }

    public void selectRoute(SelectedRoute route) {
        send(new SelectRouteMessage(route.getTransportId(), route.getRouteNumber()));
    }

    public void unselectRoute(SelectedRoute route) {
        send(new UnselectRouteMessage(route.getTransportId(), route.getRouteNumber()));
    }

    public void selectStation(SelectedStation station) {
        send(new SelectStationMessage(station.getTransportId(), station.getStationId()));
    }

    public void unselectStation(SelectedStation station) {
        send(new UnselectStationMessage(station.getTransportId(), station.getStationId()));
    }

    public void loadNearestStations(BigDecimal latitude, BigDecimal longitude) {
        send(new LoadNearestStationsMessage(latitude, longitude));
    }

    private void send(Message message) {
        clientManager.send(outcomingMessageConverter.convert(message));
    }

    private void handleMessage(String text) {
        Message message = incomingMessageConverter.convert(text);
        onMessageListener.onMessage(message);
    }

    public void disconnect() {
        clientManager.disconnect();
    }
}
