package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.midi.router.RouterResult;

public abstract class BaseFilter {

    private boolean paused;

    // TODO: Add onProcess for multiple midimessages. for instance, channel filter only needs to know what one message's channel is
    public BaseFilter(final JsonObject settings) {
        onSettings(settings);
    }

    /* package */
    abstract RouterResult onProcess(final MidiMessage message);

    public abstract void onSettings(final JsonObject settings);
    public abstract JsonObject getSettings();

    public void pause() {
        if (!paused) {
            paused = true;
        }
    }

    public void unpause() {
        if (paused) {
            paused = false;
        }
    }

    public void toggle() {
        paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public RouterResult process(final MidiMessage message) {
        return null;
    }

}
