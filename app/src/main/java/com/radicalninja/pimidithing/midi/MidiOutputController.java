package com.radicalninja.pimidithing.midi;

import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

public class MidiOutputController extends MidiDeviceController<MidiOutputController, MidiInputPort> {

    private MidiInputPort sourcePort;

    public MidiOutputController(final MidiCore.PortRecord portRecord) {

        super(portRecord);
    }

    @Override
    protected MidiInputPort getSourcePort() {
        return sourcePort;
    }

    @Nullable
    @Override
    protected MidiInputPort openSourcePort(@NonNull MidiDevice midiDevice) {
        return midiDevice.openInputPort(getPortRecord().port);
    }

    @Override
    protected void setSourcePort(@NonNull MidiInputPort sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Override
    public void closeSourcePort() throws IOException {
        sourcePort.flush();
        sourcePort.close();
    }

    public void send(final MidiMessage message) throws IOException {
        if (isOpen()) { // TODO: replace with (null != sourcePort) ?? Can be nullified in onClose()
            sourcePort.send(
                    message.getBytes(), message.getOffset(), message.getCount(), message.getTimestamp());
        }
    }

}
