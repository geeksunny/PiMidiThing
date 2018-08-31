package com.radicalninja.pimidithing.midi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MidiMessage {

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

        public static MessageType fromValue(final int value) {
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

    // TODO: Use with noteString property. ex: "NOTE_STRINGS[this.note / 12] this.octave";
    private static final String[] NOTE_STRINGS =
            { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    private final byte[] bytes;
    private final int offset;
    private final int count;
    private final long timestamp;

    private final Map<String, Integer> properties = new HashMap<>();

    private MessageType type;

    public MidiMessage(final int type, final Map<String, Integer> properties) {
        this(MessageType.fromValue(type), properties);
    }

    public MidiMessage(final MessageType type, final Map<String, Integer> properties) {
        if (null == type) {
            throw new NullPointerException("Invalid MessageType provided.");
        }
        this.offset = 0;
        this.count = 0;
        this.timestamp = 0;
        // TODO: Build contents of bytes based on type and property!
        bytes = new byte[3];
    }

    public MidiMessage(final byte[] bytes, final int offset, final int count, final long timestamp) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.offset = offset;
        this.count = count;
        this.timestamp = timestamp;
        parseBytes();
    }

    protected void parseBytes() {
        // TODO: Set type, populate properties
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getChannel() {
        // TODO: Parse from byte[0]
        return 0;
    }

    public MessageType getType() {
        // TODO!
        return null;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
