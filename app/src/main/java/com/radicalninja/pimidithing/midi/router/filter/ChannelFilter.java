package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;

public class ChannelFilter extends BaseFilter {

    public ChannelFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        // TODO
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
