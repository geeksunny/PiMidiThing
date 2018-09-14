package com.radicalninja.pimidithing.midi.router;

import com.radicalninja.pimidithing.midi.MidiMessage;

public class RouterResult {

    public static RouterResult failed() {
        return new RouterResult(false, true);
    }

    public static RouterResult consumed() {
        return new RouterResult(true, false);
    }

    private final boolean consumed, failed;
    private final MidiMessage[] messages;

    RouterResult(final boolean consumed, final boolean failed) {
        this.consumed = consumed;
        this.failed = failed;
        messages = null;
    }

    public RouterResult(MidiMessage message) {
        consumed = failed = false;
        this.messages = new MidiMessage[]{ message };
    }

    public RouterResult(MidiMessage[] messages) {
        consumed = failed = false;
        this.messages = messages;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean shouldBroadcast() {
        return !(consumed || failed);
    }

    public MidiMessage[] getMessages() {
        return messages;
    }

    public int messageCount() {
        return (null != messages) ? messages.length : 0;
    }

}
