package com.radicalninja.pimidithing.midi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.radicalninja.pimidithing.App;
import com.radicalninja.pimidithing.R;
import com.radicalninja.pimidithing.midi.router.RouterConfig;
import com.radicalninja.pimidithing.usb.MassStorageController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MidiCore implements MassStorageController.UsbMassStorageListener {

    public static class DeviceIndex {
        private final Map<String, PortRecord> records = new HashMap<>();
        // Reversing the names of output and input ports to align with RtMIDI's implementation.
        private final Map<PortRecord, MidiInputController> inputs = new HashMap<>();
        private final Map<PortRecord, MidiOutputController> outputs = new HashMap<>();

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
        static final String NICKNAME_TEMPLATE = "%s___%d";

        String name;
        int port;
        String nickname;

        public PortRecord(String name, int port) {
            this.name = name;
            this.port = port;
            this.nickname = String.format(NICKNAME_TEMPLATE, name, port);
        }

        public PortRecord(String name, int port, String nickname) {
            this.name = name;
            this.port = port;
            this.nickname = nickname;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(final String nickname) {
            this.nickname = nickname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PortRecord that = (PortRecord) o;
            return port == that.port &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(nickname, that.nickname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, port, nickname);
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
    private MidiRouter router;

    public MidiCore(final Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            throw new IllegalStateException("MIDI feature is missing from this device!");
        }
        manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        init();
    }

    protected void init() {
        if (!started) {
            // TODO: log warning / throw error?
            return;
        }
        final App app = App.getInstance();
        final InputStream defaultConfig = app.getResources().openRawResource(R.raw.config);
        final RouterConfig config =
                app.getGson().fromJson(new InputStreamReader(defaultConfig), RouterConfig.class);
        router = new MidiRouter(config);
        // TODO: Check if config file exists in internal storage
        // TODO: IF NOT - Open from raw / assets, save to internal storage.
        // TODO: IF YES - RouterConfig.fromFile()
    }

    protected Map<MidiDeviceInfo, Integer> openDevices(
            final int portType, final List<RouterConfig.Device> deviceRecords) {

        final Map<MidiDeviceInfo, Integer> result = new HashMap<>();
        return result;
    }

    public void openInput(
            final RouterConfig.Device deviceRecord, MidiManager.OnDeviceOpenedListener listener) {

        final MidiDeviceInfo[] infos = manager.getDevices();
        for (final MidiDeviceInfo info : infos) {
            final Bundle props = info.getProperties();
            final String name = props.getString(MidiDeviceInfo.PROPERTY_PRODUCT);
            if (!deviceRecord.getName().equals(name)) {
                continue;
            }
            final MidiDeviceInfo.PortInfo[] ports = info.getPorts();
            for (final MidiDeviceInfo.PortInfo portInfo : ports) {
                if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT
                        && portInfo.getPortNumber() == deviceRecord.getPort()) {

                    manager.openDevice(info, listener, null);
                    return;
                }
            }
        }
    }

    public List<MidiDevice> openInputs(final List<RouterConfig.Device> deviceRecords) {
        // TODO: Refactor in the future for input wrapper class
        final List<MidiDevice> devices = new ArrayList<>(deviceRecords.size());
        for (final RouterConfig.Device deviceRecord : deviceRecords) {
            // TODO!!!!!
        }
        return devices;
    }

    public MidiDevice openOutput(final RouterConfig.Device deviceRecord) {
        // TODO: Refactor in the future for output wrapper class
        MidiDevice device;
        // TODO!!!!!
        return null;
    }

    public List<MidiDevice> openOutputs(final List<RouterConfig.Device> deviceRecords) {
        // TODO: Refactor in the future for output wrapper class
        final List<MidiDevice> devices = new ArrayList<>(deviceRecords.size());
        for (final RouterConfig.Device deviceRecord : deviceRecords) {
            // TODO!!!!!
        }
        return devices;
    }

    @Override
    public void onStorageAttached(final UsbMassStorageDevice device) {
        // todo: init config sync
    }

    @Override
    public void onStorageDetached(UsbMassStorageDevice device) {
        // todo
    }
}
