package com.micdm.transportlive.server.messages.outcoming;

import com.micdm.transportlive.server.messages.Message;

public class GreetingMessage implements Message {

    private final String version;

    public GreetingMessage(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
