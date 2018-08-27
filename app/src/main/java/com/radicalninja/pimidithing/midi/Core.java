package com.radicalninja.pimidithing.midi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;
import android.os.Bundle;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.radicalninja.pimidithing.MassStorageController;

import java.util.HashMap;
import java.util.Map;

public class Core implements MassStorageController.UsbMassStorageListener {

    public static class DeviceIndex {
        private final Map<String, PortRecord> records = new HashMap<>();
        private final Map<PortRecord, MidiReceiver> inputs = new HashMap<>();
        private final Map<PortRecord, MidiSender> outputs = new HashMap<>();

        public DeviceIndex() {
            // todo
        }

        public PortRecord add(final String name, final int port) {
            final PortRecord record = new PortRecord(name, port);
            records.put(record.nickname, record);
            return record;
        }

        public PortRecord add(final String name, final int port, final String nickname) {
            final PortRecord record = new PortRecord(name, port, nickname);
            records.put(nickname, record);
            return record;
        }

        public void putInput(final String nickname, final MidiReceiver input) {
            // todo: if input is null, remove from records
        }

        public void putInput(final PortRecord portRecord, final MidiReceiver input) {
            // todo
        }

        public void putOutput() {
            // todo
        }

        public void putOutput(final PortRecord portRecord, final MidiReceiver output) {
            // todo
        }
    }

    public static class PortRecord {
        public String name;
        public int port;
        public String nickname;

        public PortRecord(String name, int port) {
            this.name = name;
            this.port = port;
            // TODO: Generate nickname here
        }

        public PortRecord(String name, int port, String nickname) {
            this.name = name;
            this.port = port;
            this.nickname = nickname;
        }
    }

    private class DeviceCallback extends MidiManager.DeviceCallback {
        @Override
        public void onDeviceAdded(MidiDeviceInfo device) {
            super.onDeviceAdded(device);

        }

        @Override
        public void onDeviceRemoved(MidiDeviceInfo device) {
            super.onDeviceRemoved(device);
        }

        @Override
        public void onDeviceStatusChanged(MidiDeviceStatus status) {
            super.onDeviceStatusChanged(status);
            // TODO: Handle status change - Determine how different this is from the add/remove methods.
        }
    }

    private static final String CONFIG_FILENAME = "config.json";

    private final DeviceCallback deviceCallback = new DeviceCallback();
    private final DeviceIndex index = new DeviceIndex();
    private final MidiManager manager;

    private boolean started = false;
    private Configuration config;

    public Core(final Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            throw new IllegalStateException("MIDI feature is missing from this device!");
        }
        manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
    }

    public void init() {
        if (!started) {
            // TODO: log warning / throw error?
            return;
        }
        config = new Configuration();
        // TODO: Check if config file exists in internal storage
        // TODO: IF NOT - Open from raw / assets, save to internal storage.
        // TODO: IF YES - Configuration.fromFile()
    }

    public MidiDeviceInfo[] getDevices() {
        final MidiDeviceInfo[] infos = manager.getDevices();
        for (final MidiDeviceInfo info : infos) {
            final Bundle props = info.getProperties();
            Log.d("MIDI", String.format("Manufacturer: %s", props.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER)));
            Log.d("MIDI", String.format("Name: %s", props.getString(MidiDeviceInfo.PROPERTY_NAME)));
            Log.d("MIDI", String.format("Product: %s", props.getString(MidiDeviceInfo.PROPERTY_PRODUCT)));
            Log.d("MIDI", String.format("Serial Number: %s", props.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER)));
//            Log.d("MIDI", String.format("USB Device: %s", props.getString(MidiDeviceInfo.PROPERTY_USB_DEVICE)));
            final MidiDeviceInfo.PortInfo[] portInfos = info.getPorts();
            for (final MidiDeviceInfo.PortInfo portInfo : portInfos) {
                Log.d("MIDI", "Starting a PortInfo");
                Log.d("MIDI", String.format("Port Number: %d", portInfo.getPortNumber()));
                Log.d("MIDI", String.format("Port Type: %d", portInfo.getType()));
            }
        }
        return infos;
    }

    @Override
    public void onStorageAttached(UsbMassStorageDevice device) {
        // todo
    }

    @Override
    public void onStorageDetached(UsbMassStorageDevice device) {
        // todo
    }
}
