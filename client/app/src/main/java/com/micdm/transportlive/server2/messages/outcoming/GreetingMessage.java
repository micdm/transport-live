package com.micdm.transportlive.server2.messages.outcoming;

import com.micdm.transportlive.server2.messages.Message;

public class GreetingMessage implements Message {

    private final String version;

    public GreetingMessage(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
