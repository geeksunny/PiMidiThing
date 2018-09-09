package com.radicalninja.pimidithing.midi.router.filter;

import android.util.SparseIntArray;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.NumberList;

public class ChannelFilter extends BaseFilter {

    private SparseIntArray map = new SparseIntArray();
    private NumberList whitelist = new NumberList();
    private NumberList blacklist = new NumberList();

    public ChannelFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        // TODO map, whitelist, blacklist
    }

    @Override
    public JsonObject getSettings() {
        // TODO map, whitelist, blacklist
        return null;
    }

    @Override
    Result onProcess(MidiMessage message) {
        // TODO!!!
        return null;
    }

}
