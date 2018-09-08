package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;

public abstract class BaseFilter {

    private boolean paused;

    public BaseFilter(final JsonObject settings) {
        onSettings(settings);
    }

    /* package */
    abstract Result onProcess(final MidiMessage message);

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

    public Result process(final MidiMessage message) {
        return null;
    }

    public static class Result {
        // TODO: Build class around requirements of process(msg)
    }

}
