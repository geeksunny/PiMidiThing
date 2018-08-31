package com.radicalninja.pimidithing.midi;

import java.io.Closeable;
import java.io.IOException;

public abstract class MidiDeviceController implements Closeable {

    private boolean isClosed = false;

    public abstract void onClose() throws IOException;

    @Override
    public void close() throws IOException {
        this.onClose();
        this.isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

}
