package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.MathUtils;

public class VelocityFilter extends BaseFilter {

    public static final int MIN_VELOCITY = 0;
    public static final int MAX_VELOCITY = 127;

    private static final String KEY_MIN = "min";
    private static final String KEY_MAX = "max";
    private static final String KEY_MODE = "mode";

    public enum Mode {

        CLIP, DROP, SCALED;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

    }

    interface VelocityListener {
        /**
         *
         * @param velocity The given message's current value for the Velocity property.
         * @return the new value for the Velocity property, or -1 if the message should be dropped.
         */
        int transformVelocity(final int velocity);
    }

    private Mode mode;
    private VelocityListener listener;

    private int min = MIN_VELOCITY;
    private int max = MAX_VELOCITY;

    private boolean readyToUpdateScale = false;
    private float scale;

    public VelocityFilter(JsonObject settings) {
        super(settings);
        setMode(Mode.CLIP);
    }

    @Override
    public void onSettings(JsonObject settings) {
        final JsonElement jsonMin = settings.get(KEY_MIN);
        final int _min = (null != jsonMin && jsonMin.isJsonPrimitive())
                ? jsonMin.getAsInt() : MIN_VELOCITY;
        setMin(_min);

        final JsonElement jsonMax = settings.get(KEY_MAX);
        final int _max = (null != jsonMax && jsonMax.isJsonPrimitive())
                ? jsonMax.getAsInt() : MAX_VELOCITY;
        setMax(_max);

        final JsonElement jsonMode = settings.get(KEY_MODE);
        if (null != jsonMode && jsonMode.isJsonPrimitive()) {
            Mode _mode;
            try {
                _mode = Mode.valueOf(jsonMode.getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                _mode = Mode.CLIP;
            }
            setMode(_mode);
        }
    }

    @Override
    public JsonObject getSettings() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_MIN, min);
        json.addProperty(KEY_MAX, max);
        json.addProperty(KEY_MODE, mode.toString());
        return json;
    }

    @Override
    Result onProcess(final MidiMessage message) {
        if (!message.hasProperty(MidiMessage.PROPERTY_NAME_VELOCITY)) {
            return new Result(message);
        }
        final int velocity = message.getProperty(MidiMessage.PROPERTY_NAME_VELOCITY);
        final int processed = listener.transformVelocity(velocity);
        if (processed == -1) {
            return Result.failed();
        }
        message.setProperty(MidiMessage.PROPERTY_NAME_VELOCITY, processed);
        return new Result(message);
    }

    public void setMin(int min) {
        if (this.min == min) {
            return;
        }
        this.min = MathUtils.clipToRange(min, MIN_VELOCITY, MAX_VELOCITY);
        if (this.mode == Mode.SCALED && readyToUpdateScale) {
            updateScale();
        }
    }

    public void setMax(int max) {
        if (this.max == max) {
            return;
        }
        this.max = MathUtils.clipToRange(max, this.min, MAX_VELOCITY);
        if (this.mode == Mode.SCALED && readyToUpdateScale) {
            updateScale();
        }
    }

    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }
        switch (mode) {
            case SCALED:
                readyToUpdateScale = true;
                listener = new VelocityListener() {
                    @Override
                    public int transformVelocity(int velocity) {
                        return Math.round(velocity * scale) + min;
                    }
                };
                break;
            case DROP:
                listener = new VelocityListener() {
                    @Override
                    public int transformVelocity(int velocity) {
                        return MathUtils.withinRange(velocity, min, max) ? velocity : -1;
                    }
                };
                break;
            default:
                mode = Mode.CLIP;
            case CLIP:
                listener = new VelocityListener() {
                    @Override
                    public int transformVelocity(int velocity) {
                        return MathUtils.clipToRange(velocity, min, max);
                    }
                };
        }
        this.mode = mode;
    }

    protected void updateScale() {
        scale = (max - min + 1) / 128;
    }

}
