package com.radicalninja.pimidithing.midi;

import java.util.HashMap;
import java.util.Map;

public class Message {

    public enum MessageType {

        NOTEOFF(Set.BASIC, 0x08),
        NOTEON(Set.BASIC, 0x09),
        POLY_AFTERTOUCH(Set.BASIC, 0x0A),
        CC(Set.BASIC, 0x0B),
        PROGRAM(Set.BASIC, 0x0C),
        CHANNEL_AFTERTOUCH(Set.BASIC, 0x0D),
        PITCH(Set.BASIC, 0x0E),

        SYSEX(Set.EXTENDED, 0xF0),
        MTC(Set.EXTENDED, 0xF1),
        POSITION(Set.EXTENDED, 0xF2),
        SELECT(Set.EXTENDED, 0xF3),
        TUNE(Set.EXTENDED, 0xF6),
        SYSEX_END(Set.EXTENDED, 0xF7),
        CLOCK(Set.EXTENDED, 0xF8),
        START(Set.EXTENDED, 0xFA),
        CONTINUE(Set.EXTENDED, 0xFB),
        STOP(Set.EXTENDED, 0xFC),
        RESET(Set.EXTENDED, 0xFF);

        public static final MessageType fromValue(final int value) {
            // TODO: VERIFY THIS WORKS
            final MessageType[] types = (value < 0xF0)
                    ? MessageType.basicTypes : MessageType.extendedTypes;
            for (final MessageType type : types) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

        public static final MessageType[] basicTypes = {
                NOTEOFF, NOTEON, POLY_AFTERTOUCH, CC, PROGRAM, CHANNEL_AFTERTOUCH, PITCH
        };

        public static final MessageType[] extendedTypes = {
                SYSEX, MTC, POSITION, SELECT, TUNE, SYSEX_END, CLOCK, START, CONTINUE, STOP, RESET
        };

        public enum Set {
            BASIC, EXTENDED
        }

        public final Set set;
        public final int value;

        MessageType(final Set set, final int value) {
            this.set = set;
            this.value = value;
        }

    }

    // TODO: Should actually this be final?
    private final byte[] bytes;
    private final Map<String, Integer> properties = new HashMap<>();

    public Message(final byte[] bytes) {
        this.bytes = bytes;
        // todo: set up properties
    }
}
