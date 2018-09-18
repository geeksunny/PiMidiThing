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
        void onControllerOpened(@NonNull final T controller,
                                final boolean success,
                                @Nullable String errorMessage);
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

    protected abstract void closeSourcePort() throws IOException;

    protected abstract SP getSourcePort();

    protected abstract void setSourcePort(@NonNull final SP sourcePort);

    public void open(@NonNull final OnControllerOpenedListener<T> listener,
                     @Nullable final Handler openHandler) {

        final T controller = (T) this;
        if (isOpen) {
            Log.w(TAG, "Device is already opened.");
            listener.onControllerOpened(controller, true, null);
            return;
        }
        final MidiManager.OnDeviceOpenedListener callback = new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (null == device) {
                    Log.d(TAG, "Controller.openDevice | name: "+portRecord.name+" | FAILED, MIDI DEVICE NULL");
                    listener.onControllerOpened(controller, false,
                            "Encountered an error opening the device.");
                } else {
                    Log.d(TAG, "Controller.openDevice | name: "+portRecord.name+" | SUCCESS, OPENING PORT");
                    final SP sourcePort = MidiDeviceController.this.openSourcePort(device);
                    if (null == sourcePort) {
                        Log.d(TAG, "Controller.openDevice | name: "+portRecord.name+" | FAILED, PORT NULL");
                        listener.onControllerOpened(controller, false,
                                "Failed to open the device's source port.");
                    } else {
                        Log.d(TAG, "Controller.openDevice | name: "+portRecord.name+" | SUCCESS, PORT OPENED");
                        MidiDeviceController.this.setSourcePort(sourcePort);
                        isOpen = true;
                        listener.onControllerOpened(controller, true, null);
                    }
                }
            }
        };
        App.getInstance().getMidiCore().openDevice(portRecord, callback, openHandler);
    }

    @Override
    public void close() throws IOException {
        if (this.isOpen) {
            this.closeSourcePort();
            this.isOpen = false;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public MidiCore.PortRecord getPortRecord() {
        return portRecord;
    }

}
