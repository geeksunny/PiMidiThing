package com.radicalninja.pimidithing.midi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.os.Bundle;
import android.text.TextUtils;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.radicalninja.pimidithing.App;
import com.radicalninja.pimidithing.R;
import com.radicalninja.pimidithing.midi.router.RouterConfig;
import com.radicalninja.pimidithing.usb.MassStorageController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MidiCore implements MassStorageController.UsbMassStorageListener {

    public static class DeviceIndex {
        private final Map<String, PortRecord> records = new HashMap<>();
        // Reversing the names of output and input ports to align with RtMIDI's implementation.
        private final Map<PortRecord, MidiInputController> inputs = new HashMap<>();
        private final Map<PortRecord, MidiOutputController> outputs = new HashMap<>();

        public DeviceIndex() { }

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

        public PortRecord findRecord(final RouterConfig.Device device) {
            for (final PortRecord record : records.values()) {
                if (record.name.equals(device.getName()) && record.port == device.getPort()) {
                    return record;
                }
            }
            return null;
        }

        public PortRecord findRecord(final String productName) {
            for (final PortRecord record : records.values()) {
                if (record.name.equals(productName)) {
                    return record;
                }
            }
            return null;
        }

        public PortRecord getRecord(final String nickname) {
            return records.get(nickname);
        }

        public MidiInputController getInput(final PortRecord portRecord) {
            return inputs.get(portRecord);
        }

        public MidiOutputController getOutput(final PortRecord portRecord) {
            return outputs.get(portRecord);
        }

        public boolean hasRecord(final String name) {
            return records.containsKey(name);
        }

        public void putInput(final PortRecord portRecord, final MidiInputController input) {
            inputs.put(portRecord, input);
        }

        public void putOutput(final PortRecord portRecord, final MidiOutputController output) {
            outputs.put(portRecord, output);
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
            this.nickname = String.format(Locale.US, NICKNAME_TEMPLATE, name, port);
        }

        public PortRecord(String name, int port, String nickname) {
            this.name = name;
            this.port = port;
            this.nickname = TextUtils.isEmpty(nickname)
                    ? String.format(Locale.US, NICKNAME_TEMPLATE, name, port)
                    : nickname;
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

    public interface OnControllerOpenedListener<T extends MidiDeviceController> {
        void onControllerOpened(final T input);
        void onError(final String errorMessage);
    }

    public interface OnControllersOpenedListener<T extends MidiDeviceController> {
        void onControllersOpened(final List<T> input);
        void onError(final String errorMessage);
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
        // Populate PortRecords
        final Map<String, RouterConfig.Device> devices = config.getDevices();
        for (final Map.Entry<String, RouterConfig.Device> deviceEntry : devices.entrySet()) {
            final RouterConfig.Device device = deviceEntry.getValue();
            index.add(device.getName(), device.getPort(), deviceEntry.getKey());
        }
        // Create the router
        router = new MidiRouter(config);
        // TODO: Check if config file exists in internal storage
        // TODO: IF NOT - Open from raw / assets, save to internal storage.
        // TODO: IF YES - RouterConfig.fromFile()
    }

    /**
     * Retrieve the MidiDeviceInfo object of a given product name.
     * @param deviceName - The "Product Name" of which to retrieve.
     * @return The MidiDeviceInfo object describing the device you requested,
     *      or null if it does not exist.
     */
    protected MidiDeviceInfo fetchDeviceInfo(final String deviceName) {

        final MidiDeviceInfo[] infos = manager.getDevices();
        for (final MidiDeviceInfo info : infos) {
            final Bundle props = info.getProperties();
            final String name = props.getString(MidiDeviceInfo.PROPERTY_PRODUCT);
            if (deviceName.equals(name)) {
                return info;
            }
        }
        return null;
    }

    /**
     * TODO
     * @param portRecord
     * @return
     */
    protected MidiDeviceInfo fetchDeviceInfo(final PortRecord portRecord) {
        return fetchDeviceInfo(portRecord.getName());
    }

    /**
     * Retrieve a map of MidiDeviceInfo objects of a given list of product names.
     * @param deviceNames - A list of device "Product Names" of which to retrieve..
     * @return A Map of the product names matched up to their corresponding MidiDeviceInfo objects.
     *      If no devices match, an empty map will be returned.
     */
    protected Map<String, MidiDeviceInfo> fetchDeviceInfosByNames(
            final List<String> deviceNames) {

        final Map<String, MidiDeviceInfo> result = new HashMap<>();
        final MidiDeviceInfo[] infos = manager.getDevices();
        for (final String deviceName : deviceNames) {
            if (result.containsKey(deviceName)) {
                // Already retrieved, skipping.
                continue;
            }
            for (final MidiDeviceInfo info : infos) {
                final Bundle props = info.getProperties();
                final String name = props.getString(MidiDeviceInfo.PROPERTY_PRODUCT);
                if (deviceName.equals(name)) {
                    result.put(deviceName, info);
                }
            }
        }
        return result;
    }

    /**
     * Retrieve a map of MidiDeviceInfo objects of a given list of device records.
     * @param portRecords - A list of device records with the value of name matching the
     *                      MidiDeviceInfo's "Product Name".
     * @return A map of the supplied PortRecords matched up to their corresponding
     *      MidiDeviceInfo objects. If no devices match, an empty map will be returned.
     */
    protected Map<PortRecord, MidiDeviceInfo> fetchDeviceInfos(
            final List<PortRecord> portRecords) {

        final Map<PortRecord, MidiDeviceInfo> result = new HashMap<>();
        final MidiDeviceInfo[] infos = manager.getDevices();
        for (final PortRecord portRecord : portRecords) {
            if (result.containsKey(portRecord)) {
                // Already retrieved, skipping.
                continue;
            }
            for (final MidiDeviceInfo info : infos) {
                final Bundle props = info.getProperties();
                final String name = props.getString(MidiDeviceInfo.PROPERTY_PRODUCT);
                if (portRecord.getName().equals(name)) {
                    result.put(portRecord, info);
                }
            }
        }
        return result;
    }

    /**
     * TODO
     * @param portRecord
     * @param listener
     * @return
     */
    public void openInput(final PortRecord portRecord,
                          final OnControllerOpenedListener<MidiInputController> listener) {

        final MidiInputController controller = index.getInput(portRecord);
        if (null != controller) {
            listener.onControllerOpened(controller);
            return;
        }
        final MidiDeviceInfo portInfo = fetchDeviceInfo(portRecord.getName());
        manager.openDevice(portInfo, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (null == device) {
                    listener.onError("MidiDevice failed to open and was null.");
                } else {
                    final MidiOutputPort outputPort = device.openOutputPort(portRecord.getPort());
                    final MidiInputController controller =
                            new MidiInputController(outputPort, portRecord);
                    index.putInput(portRecord, controller);
                    listener.onControllerOpened(controller);
                }
            }
        }, null);
    }

    /**
     * TODO
     * @param portRecords
     */
    public void openInputs(final List<PortRecord> portRecords,
                           final OnControllersOpenedListener<MidiInputController> listener) {

        // TODO: Some of this could be templated and consolidated into a shared `openDevices()` method...
        final List<MidiInputController> results = new ArrayList<>(portRecords.size());
        final Map<PortRecord, MidiDeviceInfo> infoMap = fetchDeviceInfos(portRecords);
        final Iterator<Map.Entry<PortRecord, MidiDeviceInfo>> iterator =
                infoMap.entrySet().iterator();

        // TODO: This needs to be tested and verified with a large number of open requests at once.
        final class RecursiveDeviceOpenedListener implements MidiManager.OnDeviceOpenedListener {

            private Map.Entry<PortRecord, MidiDeviceInfo> currentEntry;

            private void iterate() {
                try {
                    if (iterator.hasNext()) {
                        currentEntry = iterator.next();
                        iterator.remove();
                        manager.openDevice(currentEntry.getValue(), this, null);
                    } else {
                        listener.onControllersOpened(results);
                    }
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    listener.onError("An exception caused the open operation to abort.");
                }
            }

            @Override
            public void onDeviceOpened(MidiDevice device) {
                try {
                    if (null == device) {
                        // TODO: Handle error. Log something, probably?
                    } else {
                        final PortRecord portRecord = currentEntry.getKey();
                        final MidiOutputPort outputPort = device.openOutputPort(portRecord.port);
                        final MidiInputController controller =
                                new MidiInputController(outputPort, portRecord);
                        index.putInput(portRecord, controller);
                        results.add(controller);
                    }
                    iterate();
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    listener.onError("An exception caused the open operation to abort.");
                }
            }
        }

        final RecursiveDeviceOpenedListener deviceOpenedListener =
                new RecursiveDeviceOpenedListener();
        deviceOpenedListener.iterate();
    }

    /**
     * TODO
     * @param portRecord
     */
    public void openOutput(final PortRecord portRecord,
                           final OnControllerOpenedListener<MidiOutputController> listener) {

        final MidiOutputController controller = index.getOutput(portRecord);
        if (null != controller) {
            listener.onControllerOpened(controller);
            return;
        }
        final MidiDeviceInfo portInfo = fetchDeviceInfo(portRecord.getName());
        manager.openDevice(portInfo, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (null == device) {
                    listener.onError("MidiDevice failed to open and was null.");
                } else {
                    final MidiInputPort inputPort = device.openInputPort(portRecord.getPort());
                    final MidiOutputController controller =
                            new MidiOutputController(inputPort, portRecord);
                    index.putOutput(portRecord, controller);
                    listener.onControllerOpened(controller);
                }
            }
        }, null);
    }

    /**
     * TODO
     * @param portRecords
     */
    public void openOutputs(final List<PortRecord> portRecords,
                            final OnControllersOpenedListener<MidiOutputController> listener) {

        final List<MidiOutputController> results = new ArrayList<>(portRecords.size());
        final Map<PortRecord, MidiDeviceInfo> infoMap = fetchDeviceInfos(portRecords);
        final Iterator<Map.Entry<PortRecord, MidiDeviceInfo>> iterator =
                infoMap.entrySet().iterator();

        // TODO: This needs to be tested and verified with a large number of open requests at once.
        final class RecursiveDeviceOpenedListener implements MidiManager.OnDeviceOpenedListener {

            private Map.Entry<PortRecord, MidiDeviceInfo> currentEntry;

            private void iterate() {
                try {
                    if (iterator.hasNext()) {
                        currentEntry = iterator.next();
                        iterator.remove();
                        manager.openDevice(currentEntry.getValue(), this, null);
                    } else {
                        listener.onControllersOpened(results);
                    }
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    listener.onError("An exception caused the open operation to abort.");
                }
            }

            @Override
            public void onDeviceOpened(MidiDevice device) {
                try {
                    if (null == device) {
                        // TODO: Handle error. Log something, probably?
                    } else {
                        final PortRecord portRecord = currentEntry.getKey();
                        final MidiInputPort inputPort = device.openInputPort(portRecord.port);
                        final MidiOutputController controller =
                                new MidiOutputController(inputPort, portRecord);
                        index.putOutput(portRecord, controller);
                        results.add(controller);
                    }
                    iterate();
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    listener.onError("An exception caused the open operation to abort.");
                }
            }
        }

        final RecursiveDeviceOpenedListener deviceOpenedListener =
                new RecursiveDeviceOpenedListener();
        deviceOpenedListener.iterate();
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
