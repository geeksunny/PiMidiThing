package com.radicalninja.pimidithing.midi.router.filter;

import android.support.annotation.IntRange;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.MathUtils;

public class TransposeFilter extends BaseFilter {

    private static final String KEY_STEP = "step";

    private static final int NOTE_MIN = 0;
    private static final int NOTE_MAX = 127;
    private static final int NOTE_STEP = 12;

    private int step;

    public TransposeFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        final JsonElement stepJson = settings.get(KEY_STEP);
        if (null != stepJson && stepJson.isJsonPrimitive()) {
            setStep(stepJson.getAsInt());
        }
    }

    @Override
    public JsonObject getSettings() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_STEP, step);
        return json;
    }

    @Override
    Result onProcess(MidiMessage message) {
        if (message.hasProperty(MidiMessage.PROPERTY_NAME_NOTE)) {
            final int note = message.getProperty(MidiMessage.PROPERTY_NAME_NOTE);
            final int scaled = note + (step * NOTE_STEP);
            final int _note = MathUtils.clipToRange(scaled, NOTE_MIN, NOTE_MAX);
            if (note != _note) {
                message.setProperty(MidiMessage.PROPERTY_NAME_NOTE, _note);
            }
        }
        return new Result(message);
    }

    void setStep(@IntRange(from=-10, to=10) final int step) {
        if (step > 10 || step < -10) {
            this.step = 0;
        } else {
            this.step = step;
        }
    }

}
