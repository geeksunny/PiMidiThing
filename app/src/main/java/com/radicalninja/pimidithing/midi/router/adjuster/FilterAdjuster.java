package com.radicalninja.pimidithing.midi.router.adjuster;

import com.radicalninja.pimidithing.midi.MidiMessage;

public class FilterAdjuster {

    public interface ValueHandler {
        // TODO: Object<string, number>
    }

    public interface TriggerMap {
        // TODO: Object<string, number|boolean>
    }

    private final String name;
    private final String description;
    private final ValueHandler valueHandler;
    private final boolean potPickup;
    private final TriggerMap triggerMap;
    private final MidiMessage.MessageType messageType;
    private final String[] userMapping;
    private final String valueKey;

    private int value;

    // TODO: More constructors with default values?

    public FilterAdjuster(String name, String description, ValueHandler valueHandler,
                          boolean potPickup, TriggerMap triggerMap, MidiMessage.MessageType messageType,
                          String[] userMapping, String valueKey) {
        this.name = name;
        this.description = description;
        this.valueHandler = valueHandler;
        this.potPickup = potPickup;
        this.triggerMap = triggerMap;
        this.messageType = messageType;
        this.userMapping = userMapping;
        this.valueKey = valueKey;
    }
}
