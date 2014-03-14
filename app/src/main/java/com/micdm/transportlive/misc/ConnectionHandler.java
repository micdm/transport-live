package com.micdm.transportlive.misc;

public interface ConnectionHandler {

    public static interface OnNoConnectionListener {
        public void onNoConnection();
    }

    public void reconnect();
    public void setOnNoConnectionListener(OnNoConnectionListener listener);
}
