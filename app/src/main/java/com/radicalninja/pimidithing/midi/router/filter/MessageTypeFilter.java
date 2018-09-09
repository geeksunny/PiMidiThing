package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.NumberList;

public class MessageTypeFilter extends BaseFilter {

    private NumberList whitelist = new NumberList();
    private NumberList blacklist = new NumberList();

    public MessageTypeFilter(JsonObject settings) {
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
