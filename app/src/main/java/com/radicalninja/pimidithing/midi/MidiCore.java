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

        DeviceIndex() { }

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
            // TODO: open device
        }

        @Override
        public void onDeviceRemoved(MidiDeviceInfo device) {
            // TODO: Close / remove device controller
        }

        @Override
        public void onDeviceStatusChanged(MidiDeviceStatus status) {
            // TODO: Handle status change - Determine how different this is from the add/remove methods.
        }
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
            Log.d(TAG, "MidiCore was already started, skipping init call.");
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

        if (router.started()) {
            if (null != listener) {
                listener.onRouterError("MidiRouter is already started!", null);
            }
        }
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
    @Nullable
    /* package */
    MidiDeviceInfo fetchDeviceInfo(final String deviceName) {
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
     * Retrieve the MidiDeviceInfo object of a given product name.
     * @param portRecord - A PortRecord object representing the "Product Name" of the MIDI device.
     * @return The MidiDeviceInfo object describing the device you requested,
     *      of null if it does not exist.
     */
    @Nullable
    /* package */
    MidiDeviceInfo fetchDeviceInfo(final PortRecord portRecord) {
        return fetchDeviceInfo(portRecord.getName());
    }

    /**
     * Open a MidiDevice
     * @param portRecord
     * @param callback
     * @param openHandler
     */
    /* package */
    void openDevice(@NonNull final PortRecord portRecord,
                    @NonNull final MidiManager.OnDeviceOpenedListener callback,
                    @Nullable final Handler openHandler) {

        final MidiDeviceInfo midiDeviceInfo = fetchDeviceInfo(portRecord.name);
        manager.openDevice(midiDeviceInfo, callback, openHandler);
    }

    /**
     * Open a single MIDI Input Controller.
     * @param portRecord - A PortRecord describing the desired input.
     * @param listener - Callback to be executed upon completion of the opening process.
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    public void openInput(final PortRecord portRecord,
                          final MidiDeviceController.OnControllerOpenedListener<MidiInputController> listener,
                          final Handler openHandler) {

        final MidiInputController controller = index.getInput(portRecord);
        if (null != controller) {
            if (controller.isOpen()) {
                listener.onControllerOpened(controller);
            } else {
                controller.open(listener, openHandler);
            }
        } else {
            final MidiInputController c = new MidiInputController(portRecord);
            index.putInput(portRecord, c);
            c.open(listener, openHandler);
        }
    }

    /**
     * Open a single MIDI Output Controller.
     * @param portRecord - A PortRecord describing the desired output.
     * @param listener - Callback to be executed upon completion of the opening process.
     * @param openHandler - Optional handler to be used by the MIDI manager.
     */
    public void openOutput(final PortRecord portRecord,
                           final MidiDeviceController.OnControllerOpenedListener<MidiOutputController> listener,
                           final Handler openHandler) {

        // TODO: change out to retrieve indexed controllers OR create one. Attempt to open unopened controllers???????
        final MidiOutputController controller = index.getOutput(portRecord);
        if (null != controller) {
            if (controller.isOpen()) {
                listener.onControllerOpened(controller);
            } else {
                controller.open(listener, openHandler);
            }
        } else {
            final MidiOutputController c = new MidiOutputController(portRecord);
            index.putOutput(portRecord, c);
            c.open(listener, openHandler);
        }
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
