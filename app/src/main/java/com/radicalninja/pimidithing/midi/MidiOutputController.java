package com.radicalninja.pimidithing.midi;

import android.media.midi.MidiInputPort;

import java.io.IOException;

public class MidiOutputController extends MidiDeviceController {

    private final MidiInputPort midiPort;

    public MidiOutputController(final MidiInputPort midiInputPort) {
        this.midiPort = midiInputPort;
    }

    @Override
    public void onClose() throws IOException {
        midiPort.flush();
        midiPort.close();
    }

    public void send(final MidiMessage message) throws IOException {
        midiPort.send(message.getBytes(), message.getOffset(), message.getCount(), message.getTimestamp());
    }

}
