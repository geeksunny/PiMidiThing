package com.radicalninja.pimidithing.midi;

import java.io.Closeable;
import java.io.IOException;

public abstract class MidiDeviceController implements Closeable {

    private final MidiCore.PortRecord portRecord;

    private boolean isClosed = false;

    public MidiDeviceController(final MidiCore.PortRecord portRecord) {
        this.portRecord = portRecord;
    }

    public abstract void onClose() throws IOException;

    @Override
    public void close() throws IOException {
        this.onClose();
        this.isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public MidiCore.PortRecord getPortRecord() {
        return portRecord;
    }

}
