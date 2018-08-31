package com.radicalninja.pimidithing.midi;

import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiInputController extends MidiDeviceController {

    public interface MessageListener {
        boolean onMessage(final MidiMessage message);
    }

    private static final String TAG = MidiInputController.class.getCanonicalName();

    private final MidiInputReceiver receiver = new MidiInputReceiver();
    private final MidiOutputPort sourcePort;

    public MidiInputController(final MidiOutputPort midiOutputPort) {
        this.sourcePort = midiOutputPort;
        this.sourcePort.connect(receiver);
    }

    @Override
    public void onClose() throws IOException {
        synchronized (receiver) {
            receiver.listeners.clear();
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

        final private List<MessageListener> listeners = new ArrayList<>();

        @Override
        public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
            final MidiMessage message = new MidiMessage(data, offset, count, timestamp);
            synchronized (this) {
                for (final MessageListener listener : listeners) {
                    final boolean result = listener.onMessage(message);
                    if (result) {
                        Log.d(TAG, "Message chain handled early.");
                        break;
                    }
                }
            }
        }

    }

}
