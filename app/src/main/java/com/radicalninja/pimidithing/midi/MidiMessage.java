package com.radicalninja.pimidithing.midi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.radicalninja.pimidithing.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MidiMessage {

    public enum MessageType {

        NOTEOFF(Set.BASIC, (byte) 0x08),
        NOTEON(Set.BASIC, (byte) 0x09),
        POLY_AFTERTOUCH(Set.BASIC, (byte) 0x0A),
        CC(Set.BASIC, (byte) 0x0B),
        PROGRAM(Set.BASIC, (byte) 0x0C),
        CHANNEL_AFTERTOUCH(Set.BASIC, (byte) 0x0D),
        PITCH(Set.BASIC, (byte) 0x0E),

        SYSEX(Set.EXTENDED, (byte) 0xF0),
        MTC(Set.EXTENDED, (byte) 0xF1),
        POSITION(Set.EXTENDED, (byte) 0xF2),
        SELECT(Set.EXTENDED, (byte) 0xF3),
        TUNE(Set.EXTENDED, (byte) 0xF6),
        SYSEX_END(Set.EXTENDED, (byte) 0xF7),
        CLOCK(Set.EXTENDED, (byte) 0xF8),
        START(Set.EXTENDED, (byte) 0xFA),
        CONTINUE(Set.EXTENDED, (byte) 0xFB),
        STOP(Set.EXTENDED, (byte) 0xFC),
        RESET(Set.EXTENDED, (byte) 0xFF);

        @Nullable
        public static MessageType fromValue(final byte value) {
            final boolean isBasic = (value < (byte) 0xF0);
            final byte typeByte = isBasic ? (byte) ((value >>> (byte) 4) & (byte) 0x0f) : value;
//            Log.d("MessageType",String.format("value: %08x | typeByte: %08x | is basic: %s", value, typeByte, isBasic));
            final MessageType[] types = isBasic ? MessageType.basicTypes : MessageType.extendedTypes;
            for (final MessageType type : types) {
                if (type.value == typeByte) {
                    return type;
                }
            }
            return null;
        }

        @Nullable
        public static MessageType fromString(@NonNull final String name) {
            try {
                return MessageType.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
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
        public final byte value;

        MessageType(final Set set, final byte value) {
            this.set = set;
            this.value = value;
        }

    }

    public interface PropertyHandler {
        /**
         * Get the value of this property.
         * @return An integer represting the current value of this property.
         */
        int get();

        /**
         * Set the value of this property.
         * @param value - The new value to set in this property.
         */
        void set(final int value);
    }

    // TODO: Use with noteString property. ex: "NOTE_STRINGS[this.note / 12] this.octave";
    private static final String[] NOTE_STRINGS =
            { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    public static final byte BYTE_SYSEX_START = (byte) 0xF0;
    public static final byte BYTE_SYSEX_END = (byte) 0xF7;

    public static final String PROPERTY_NAME_NOTE = "note";
    public static final String PROPERTY_NAME_VELOCITY = "velocity";
    public static final String PROPERTY_NAME_OCTAVE = "octave";
    public static final String PROPERTY_NAME_NOTE_STRING = "note_string";
    public static final String PROPERTY_NAME_PRESSURE = "pressure";
    public static final String PROPERTY_NAME_CONTROLLER = "controller";
    public static final String PROPERTY_NAME_VALUE = "value";
    public static final String PROPERTY_NAME_NUMBER = "number";
    public static final String PROPERTY_NAME_MTC_TYPE = "mtc_type";
    public static final String PROPERTY_NAME_SONG = "song";

    public static MidiMessage fromSysexFile(final File sysexFile) throws IOException {
        final byte[] fileBytes = Files.readAllBytes(sysexFile.toPath());
        // Verify content is sysex data. Trim excess bytes.
        final int start = ByteUtils.firstOccuranceOfByte(fileBytes, BYTE_SYSEX_START);
        final int end = ByteUtils.lastOccuranceOfByte(fileBytes, BYTE_SYSEX_END);
        final MidiMessage message;
        if (start == -1 || end == -1 || start >= end) {
            throw new MalformedSysexBytesException(sysexFile.getPath(), fileBytes.length);
        } else if (start == 0 && end == fileBytes.length - 1) {
            message = new MidiMessage(fileBytes, 0, 0, 0);
        } else {
            final byte[] subset = Arrays.copyOfRange(fileBytes, start, end);
            message = new MidiMessage(subset, 0, 0, 0);
        }
        return message;
    }

    private final byte[] bytes;
    private final int offset;
    private final int count;
    private final long timestamp;

    private final Map<String, PropertyHandler> properties = new HashMap<>();

    private int channel;
    private MessageType type;

    public MidiMessage(final byte type, final Map<String, Integer> properties) {
        this(MessageType.fromValue(type), properties);
    }

    public MidiMessage(final MessageType type, final Map<String, Integer> properties) {
        if (null == type) {
            throw new NullPointerException("Invalid MessageType provided.");
        }
        this.type = type;
        this.offset = 0;
        this.count = 0;
        this.timestamp = 0;

        bytes = new byte[3];
        setupPropertyHandlers();
        // TODO: Build contents of bytes based on type and property!
        // TODO: populate value of channel... should this be in properties or a separate param?
    }

    public MidiMessage(final byte[] bytes, final int offset, final int count, final long timestamp) {
        this.type = MessageType.fromValue(bytes[0]);
        if (null == this.type) {
            throw new NullPointerException("Invalid MessageType encountered.");
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.offset = offset;
        this.count = count;
        this.timestamp = timestamp;
        parseBytes();
    }

    public MidiMessage(MidiMessage other) {
        this.bytes = other.bytes;
        this.offset = other.offset;
        this.count = other.count;
        this.timestamp = other.timestamp;
        this.channel = other.channel;
        this.type = other.type;
    }

    protected PropertyHandler createBasicPropertyHandler(final int byteIndex) {
        return new PropertyHandler() {
            @Override
            public int get() {
                return bytes[byteIndex];
            }

            @Override
            public void set(int value) {
                bytes[byteIndex] = (byte) value;
            }
        };
    }

    protected void parseBytes() {
        channel = (type.set == MessageType.Set.BASIC) ? bytes[0] & 0xF : -1;
        setupPropertyHandlers();
    }

    /**
     * Called when message type has changed.
     */
    protected void setupPropertyHandlers() {
        // Clear any existing property handlers for re-initialization
        properties.clear();
        // Setting properties and handlers
        switch (type) {
            // Basic types
            case NOTEOFF:
            case NOTEON:
                properties.put(PROPERTY_NAME_NOTE, createBasicPropertyHandler(1));
                properties.put(PROPERTY_NAME_VELOCITY, createBasicPropertyHandler(2));
                properties.put(PROPERTY_NAME_OCTAVE, new PropertyHandler() {
                    @Override
                    public int get() {
                        return (bytes[1] / 12) - 1;
                    }

                    @Override
                    public void set(int value) {
                        // TODO: Modify the contents of property:note using value
                    }
                });
                // TODO: How should string values be handled with properties interface?
//                properties.put(PROPERTY_NAME_NOTE_STRING, /*TODO*/null);
                break;
            case POLY_AFTERTOUCH:
                properties.put(PROPERTY_NAME_NOTE, createBasicPropertyHandler(1));
                properties.put(PROPERTY_NAME_PRESSURE, createBasicPropertyHandler(2));
                properties.put(PROPERTY_NAME_OCTAVE, new PropertyHandler() {
                    @Override
                    public int get() {
                        return (bytes[1] / 12) - 1;
                    }

                    @Override
                    public void set(int value) {
                        // TODO: Modify the contents of property:note using value
                    }
                });
                // TODO: How should string values be handled with properties interface?
//                properties.put(PROPERTY_NAME_NOTE_STRING, /*TODO*/null);
                break;
            case CC:
                properties.put(PROPERTY_NAME_CONTROLLER, createBasicPropertyHandler(1));
                properties.put(PROPERTY_NAME_VALUE, createBasicPropertyHandler(2));
                break;
            case PROGRAM:
                properties.put(PROPERTY_NAME_NUMBER, createBasicPropertyHandler(1));
                break;
            case CHANNEL_AFTERTOUCH:
                properties.put(PROPERTY_NAME_PRESSURE, createBasicPropertyHandler(1));
                break;
            case PITCH:
            // Extended types
            case POSITION:
                properties.put(PROPERTY_NAME_VALUE, new PropertyHandler() {
                    @Override
                    public int get() {
                        return bytes[1] + (bytes[2] * 128);
                    }

                    @Override
                    public void set(int value) {
                        bytes[1] = (byte) (value & 0x7F);           // lsb
                        bytes[2] = (byte) ((value & 0x3F80) >> 7);  // msb
                    }
                });
                break;
            case MTC:
                properties.put(PROPERTY_NAME_MTC_TYPE, new PropertyHandler() {
                    @Override
                    public int get() {
                        return (bytes[1] >> 4) & 0x07;
                    }

                    @Override
                    public void set(int value) {
                        // TODO: Verify this!
                        bytes[1] = (byte) ((value << 4) & 0x07);
                    }
                });
                properties.put(PROPERTY_NAME_VALUE, new PropertyHandler() {
                    @Override
                    public int get() {
                        return bytes[1] & 0x0F;
                    }

                    @Override
                    public void set(int value) {
                        final PropertyHandler mtcType = properties.get(PROPERTY_NAME_MTC_TYPE);
                        bytes[1] = (byte) ((mtcType.get() << 4) + value);
                    }
                });
                break;
            case SELECT:
                properties.put(PROPERTY_NAME_SONG, createBasicPropertyHandler(1));
                break;
        }
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public int getChannel() {
        return (channel == -1) ? channel : channel + 1;
    }

    /**
     * Set the channel of this message.
     * @param channel - An integer value between 1 and 16.
     */
    public void setChannel(@IntRange(from=1,to=16) final int channel) {
        if ((type.set == MessageType.Set.BASIC) && !(channel == (this.channel + 1))) {
            this.channel = channel - 1;
            this.bytes[0] = (byte) ((type.value << 4) + channel);
        } else if (type.set == MessageType.Set.EXTENDED) {
            this.channel = -1;
        }
    }

    public MessageType getType() {
        return type;
    }

    public void setMessageType(final MessageType type) {
        if (type != this.type) {
            if (type.set == MessageType.Set.BASIC) {
                this.bytes[0] = (byte) ((type.value >> 4) + channel);
            } else {
                this.bytes[0] = (byte) type.value;
                this.channel = -1;
            }
            setupPropertyHandlers();
        }
    }

    public Map<String, Integer> getProperties() {
        final Map<String, Integer> result = new HashMap<>(properties.size());
        for (final Map.Entry<String, PropertyHandler> property : properties.entrySet()) {
            result.put(property.getKey(), property.getValue().get());
        }
        return result;
    }

    public int getProperty(final String propertyName) throws PropertyNotDefinedException {
        if (!properties.containsKey(propertyName)) {
            throw new PropertyNotDefinedException(propertyName);
        }
        return properties.get(propertyName).get();
    }

    public boolean hasProperty(final String propertyName) {
        return properties.containsKey(propertyName);
    }

    public boolean setProperty(final String propertyName, final int value) throws PropertyNotDefinedException {
        // TODO: Should get/set on this.bytes be synchronized?
        final PropertyHandler handler = properties.get(propertyName);
        if (null == handler) {
            throw new PropertyNotDefinedException(propertyName);
        }
        if (handler.get() == value) {
            return false;
        }
        handler.set(value);
        return true;
    }

    public Set<String> getPropertyNames() {
        return properties.keySet();
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

    public static class MalformedSysexBytesException extends IOException {
        public MalformedSysexBytesException(final String path, final int length) {
            super(String.format(Locale.US,
                    "Data in file (%s, %d bytes) is not a valid sysex message.", path, length));
        }
    }

    public static class PropertyNotDefinedException extends IndexOutOfBoundsException {
        public PropertyNotDefinedException(final String propertyName) {
            super(String.format(Locale.US,
                    "The requested property (%s) is not defined.", propertyName));
        }
    }

}
