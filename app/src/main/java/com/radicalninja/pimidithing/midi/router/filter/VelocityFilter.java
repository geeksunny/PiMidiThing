package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;

public class VelocityFilter extends BaseFilter {

    private int min;
    private int max;
    private String mode;

    public VelocityFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        // TODO: min, max, mode
    }

    @Override
    public JsonObject getSettings() {
        // TODO
        return null;
    }

    @Override
    Result onProcess(MidiMessage message) {
        // TODO!!!
        return null;
    }

}
