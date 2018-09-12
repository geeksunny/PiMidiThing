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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.radicalninja.pimidithing.midi.router.MidiRouter;
import com.radicalninja.pimidithing.midi.router.RouterConfig;
import com.radicalninja.pimidithing.usb.MassStorageController;

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

    // TODO: Consolidate the listener interfaces/classes below if possible
    public interface OnControllerOpenedListener<T extends MidiDeviceController> {
        void onControllerOpened(final T input);
        void onError(final String errorMessage);
    }

    public interface OnControllersOpenedListener<T extends MidiDeviceController> {
        void onControllersOpened(final List<T> inputs);
        void onError(final String errorMessage);
    }

    public interface OnDevicesOpenedListener {
        void onDeviceOpened(final MidiDevice device, final PortRecord portRecord);
        void onFinished();
        void onError(final String errorMessage);
    }

    private static final String CONFIG_FILENAME = "config.json";
    private static final String TAG = MidiCore.class.getCanonicalName();

    private final DeviceCallback deviceCallback = new DeviceCallback();
    private final DeviceIndex index = new DeviceIndex();
    private final MidiManager manager;

    private boolean started = false;
    private MidiRouter router;

    public MidiCore(@NonNull final Context context, @NonNull final RouterConfig config) {

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            throw new IllegalStateException("MIDI feature is missing from this device!");
        }
        manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        init(config);
    }

    protected void init(@NonNull final RouterConfig config) {

        if (started) {
            // TODO: log warning / throw error?
            return;
        }
        // Populate PortRecords
        final Map<String, RouterConfig.Device> devices = config.getDevices();
        for (final Map.Entry<String, RouterConfig.Device> deviceEntry : devices.entrySet()) {
            final RouterConfig.Device device = deviceEntry.getValue();
            index.add(device.getName(), device.getPort(), deviceEntry.getKey());
        }
        // Create the router
        router = new MidiRouter(config);
    }

    public void initRouter(@Nullable final MidiRouter.OnRouterReadyListener listener) {
        initRouter(listener, null);
    }

    public void initRouter(@Nullable final MidiRouter.OnRouterReadyListener listener,
                           @Nullable final Handler callbackHandler) {

        if (null != listener) {
            final Handler handler = (null == callbackHandler) ? new Handler() : callbackHandler;
            router.init(listener, handler);
        } else {
            router.init();
        }
    }

    public PortRecord getPortRecord(final String nickname) {
        return index.getRecord(nickname);
    }

    public List<PortRecord> getPortRecords(final List<String> nicknames) {
        final List<PortRecord> result = new ArrayList<>(nicknames.size());
        for (final String nickname : nicknames) {
            final PortRecord record = index.getRecord(nickname);
            if (null != record) {
                result.add(record);
            }
        }
        return result;
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
     * Open a single MIDI Input Controller.
     * @param portRecord - A PortRecord describing the desired input.
     * @param listener - Callback to be executed upon completion of the opening process.
     */
    public void openInput(final PortRecord portRecord,
                          final OnControllerOpenedListener<MidiInputController> listener,
                          final Handler openHandler) {

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
        }, openHandler);
    }

    /**
     * Shared method for opening multiple MIDI devices at once.
     * @param portRecords - A list of the PortRecords you wish to open.
     * @param onDevicesOpenedListener
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    protected void openDevices(final List<PortRecord> portRecords,
                            final OnDevicesOpenedListener onDevicesOpenedListener,
                            final Handler openHandler) {

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
                        manager.openDevice(currentEntry.getValue(), this, openHandler);
                    } else {
                        onDevicesOpenedListener.onFinished();
                    }
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    onDevicesOpenedListener.onError("An exception caused the open operation to abort.");
                }
            }

            @Override
            public void onDeviceOpened(MidiDevice device) {
                try {
                    onDevicesOpenedListener.onDeviceOpened(device, currentEntry.getKey());
                    iterate();
                } catch (Exception e) {
                    // TODO: Work exception into onError call...
                    onDevicesOpenedListener.onError("An exception caused the open operation to abort.");
                }
            }
        }

        final RecursiveDeviceOpenedListener deviceOpenedListener =
                new RecursiveDeviceOpenedListener();
        deviceOpenedListener.iterate();
    }

    /**
     * Open one or more more than one MIDI Input Controller.
     * @param portRecords - PortRecords describing the inputs to open.
     * @param listener - Callback to be executed upon completion of the opening process.
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    public void openInputs(final List<PortRecord> portRecords,
                           final OnControllersOpenedListener<MidiInputController> listener,
                           final Handler openHandler) {

        final List<MidiInputController> results = new ArrayList<>(portRecords.size());
        // Checking existing controllers for matches
        final Iterator<PortRecord> i = portRecords.iterator();
        while (i.hasNext()) {
            final PortRecord record = i.next();
            final MidiInputController controller = index.getInput(record);
            if (null != controller) {
                results.add(controller);
                i.remove();
            }
        }
        // If all matched, execute callback
        if (portRecords.isEmpty()) {
            listener.onControllersOpened(results);
            return;
        }
        // Open remaining PortRecords
        final OnDevicesOpenedListener onDevicesOpenedListener = new OnDevicesOpenedListener() {
            @Override
            public void onFinished() {
                listener.onControllersOpened(results);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }

            @Override
            public void onDeviceOpened(MidiDevice device, PortRecord portRecord) {
                if (null == device) {
                    Log.d(TAG, String.format(Locale.US,
                            "Encountered an error when opening input for PortRecord:%s",
                            portRecord.getNickname()));
                } else {
                    final MidiOutputPort outputPort = device.openOutputPort(portRecord.port);
                    final MidiInputController controller =
                            new MidiInputController(outputPort, portRecord);
                    index.putInput(portRecord, controller);
                    results.add(controller);
                }
            }
        };
        openDevices(portRecords, onDevicesOpenedListener, openHandler);
    }

    /**
     * Open a single MIDI Output Controller.
     * @param portRecord - A PortRecord describing the desired output.
     * @param listener - Callback to be executed upon completion of the opening process.
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    public void openOutput(final PortRecord portRecord,
                           final OnControllerOpenedListener<MidiOutputController> listener,
                           final Handler openHandler) {

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
        }, openHandler);
    }

    /**
     * Open one or more more than one MIDI Output Controller.
     * @param portRecords - PortRecords describing the outputs to open.
     * @param listener - Callback to be executed upon completion of the opening process.
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    public void openOutputs(final List<PortRecord> portRecords,
                            final OnControllersOpenedListener<MidiOutputController> listener,
                            final Handler openHandler) {

        final List<MidiOutputController> results = new ArrayList<>(portRecords.size());
        // Checking existing controllers for matches
        final Iterator<PortRecord> i = portRecords.iterator();
        while (i.hasNext()) {
            final PortRecord record = i.next();
            final MidiOutputController controller = index.getOutput(record);
            if (null != controller) {
                results.add(controller);
                i.remove();
            }
        }
        // If all matched, execute callback
        if (portRecords.isEmpty()) {
            listener.onControllersOpened(results);
            return;
        }
        // Open remaining PortRecords
        final OnDevicesOpenedListener onDevicesOpenedListener = new OnDevicesOpenedListener() {
            @Override
            public void onFinished() {
                listener.onControllersOpened(results);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }

            @Override
            public void onDeviceOpened(MidiDevice device, PortRecord portRecord) {
                if (null == device) {
                    Log.d(TAG, String.format(Locale.US,
                            "Encountered an error when opening output for PortRecord:%s",
                            portRecord.getNickname()));
                } else {
                    final MidiInputPort inputPort = device.openInputPort(portRecord.port);
                    final MidiOutputController controller =
                            new MidiOutputController(inputPort, portRecord);
                    index.putOutput(portRecord, controller);
                    results.add(controller);
                }
            }
        };
        openDevices(portRecords, onDevicesOpenedListener, openHandler);
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
