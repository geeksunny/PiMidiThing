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

        static Result failed() {
            return new Result(false, true);
        }

        static Result consumed() {
            return new Result(true, false);
        }

        private final boolean consumed, failed;
        private final MidiMessage[] messages;

        Result(final boolean consumed, final boolean failed) {
            this.consumed = consumed;
            this.failed = failed;
            messages = null;
        }

        Result(MidiMessage message) {
            consumed = failed = false;
            this.messages = new MidiMessage[]{ message };
        }

        Result(MidiMessage[] messages) {
            consumed = failed = false;
            this.messages = messages;
        }

        public boolean isConsumed() {
            return consumed;
        }

        public boolean isFailed() {
            return failed;
        }

        public MidiMessage[] getMessages() {
            return messages;
        }

        public int messageCount() {
            return (null != messages) ? messages.length : 0;
        }

    }

}
