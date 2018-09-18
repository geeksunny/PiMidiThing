package com.radicalninja.pimidithing.midi;

import android.media.midi.MidiDevice;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiInputController
        extends MidiDeviceController<MidiInputController, MidiOutputPort> {

    public interface MessageListener {
        boolean onMessage(final MidiInputController input, final MidiMessage message);
    }

    private static final String TAG = MidiInputController.class.getCanonicalName();

    private final MidiInputReceiver receiver = new MidiInputReceiver(this);

    private MidiOutputPort sourcePort;

    public MidiInputController(final MidiCore.PortRecord portRecord) {

        super(portRecord);
    }

    @Override
    protected MidiOutputPort getSourcePort() {
        return sourcePort;
    }

    @Nullable
    @Override
    protected MidiOutputPort openSourcePort(@NonNull MidiDevice midiDevice) {
        return midiDevice.openOutputPort(getPortRecord().port);
    }

    @Override
    protected void setSourcePort(@NonNull MidiOutputPort sourcePort) {
        synchronized (receiver) {
            this.sourcePort = sourcePort;
            this.sourcePort.connect(receiver);
        }
    }

    @Override
    public void closeSourcePort() throws IOException {
        synchronized (receiver) {
            this.sourcePort.disconnect(receiver);
//            receiver.listeners.clear();
            receiver.flush();
            sourcePort.close();
        }
    }

    public boolean addMessageListener(final MessageListener listener) {
        synchronized (receiver) {
            if (receiver.listeners.contains(listener)) {
                Log.d(TAG, "Attempted to add a duplicate MessageListener. Skipping.");
                return false;
            }
            receiver.listeners.add(listener);
            return true;
        }
    }

    public boolean removeMessageListener(final MessageListener listener) {
        synchronized (receiver) {
            return receiver.listeners.remove(listener);
        }
    }

    public static class MidiInputReceiver extends MidiReceiver {

        private final MidiInputController inputController;
        private final List<MessageListener> listeners = new ArrayList<>();

        public MidiInputReceiver(final MidiInputController inputController) {
            this.inputController = inputController;
        }

        @Override
        public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
//            Log.d(TAG, String.format("offset: %d | count: %d", offset, count));
            final MidiMessage message = new MidiMessage(data, offset, count, timestamp);
            synchronized (this) {
                for (final MessageListener listener : listeners) {
                    final boolean result = listener.onMessage(inputController, message);
                    if (result) {
                        Log.d(TAG, "Message chain handled early.");
                        break;
                    }
                }
            }
        }

    }

}
