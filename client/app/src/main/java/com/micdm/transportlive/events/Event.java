package com.micdm.transportlive.events;

public abstract class Event {

    private final EventType _type;

    public Event(EventType type) {
        _type = type;
    }

    public EventType getType() {
        return _type;
    }
}
