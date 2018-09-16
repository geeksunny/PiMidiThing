package com.radicalninja.pimidithing.midi;

import android.media.midi.MidiDevice;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.radicalninja.pimidithing.App;

import java.io.Closeable;
import java.io.IOException;

public abstract class MidiDeviceController
        <T extends MidiDeviceController, SP extends Closeable>
        implements Closeable {

    public interface OnControllerOpenedListener<T extends MidiDeviceController> {
        void onControllerOpened(@NonNull final T controller);
        void onError(@NonNull final String errorMessage);
    }

    private static final String TAG = MidiDeviceController.class.getCanonicalName();

    private final MidiCore.PortRecord portRecord;

    private boolean isOpen = false;

    public MidiDeviceController(final MidiCore.PortRecord portRecord) {
        this.portRecord = portRecord;
    }

    // TODO: Should an optional OnControllerClosedListener<T> be added?

    @Nullable
    protected abstract SP openSourcePort(@NonNull final MidiDevice midiDevice);

    protected abstract void setSourcePort(@NonNull final SP sourcePort);

    protected abstract SP getSourcePort();

    public abstract void onClose() throws IOException;

    public void open(@NonNull final OnControllerOpenedListener<T> listener,
                     @Nullable final Handler openHandler) {

        if (isOpen) {
            Log.w(TAG, "Device is already opened.");
            listener.onControllerOpened((T) this);
            return;
        }
        // TODO: Add some logic to check isOpen() before proceeding
        final MidiManager.OnDeviceOpenedListener callback = new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (null == device) {
                    listener.onError("Encountered an error opening the device");
                } else {
                    final SP sourcePort = MidiDeviceController.this.openSourcePort(device);
                    if (null == sourcePort) {
                        listener.onError("Failed to open the device's source port.");
                    } else {
                        MidiDeviceController.this.setSourcePort(sourcePort);
                        isOpen = true;
                        listener.onControllerOpened((T) MidiDeviceController.this);
                    }
                }
            }
        };
        App.getInstance().getMidiCore().openDevice(portRecord, callback, openHandler);
    }

    @Override
    public void close() throws IOException {
        if (this.isOpen) {
            this.onClose();
            // TODO: Should this be handled here or left up to implementation?
            //getSourcePort().close();
            this.isOpen = true;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public MidiCore.PortRecord getPortRecord() {
        return portRecord;
    }

}
