package com.radicalninja.pimidithing.midi.router;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.App;
import com.radicalninja.pimidithing.CountDownWorkerThread;
import com.radicalninja.pimidithing.midi.MidiCore;
import com.radicalninja.pimidithing.midi.MidiDeviceController;
import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiOutputController;
import com.radicalninja.pimidithing.midi.router.filter.BaseFilter;
import com.radicalninja.pimidithing.midi.router.filter.ChannelFilter;
import com.radicalninja.pimidithing.midi.router.filter.ChordFilter;
import com.radicalninja.pimidithing.midi.router.filter.MessageTypeFilter;
import com.radicalninja.pimidithing.midi.router.filter.TransposeFilter;
import com.radicalninja.pimidithing.midi.router.filter.VelocityFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/* package */
class Configurator {

    /* package */
    interface OnConfigFinishedListener {
        void onFinish();
    }

    private static final String TAG = Configurator.class.getCanonicalName();

    private static final String FILTER_CHANNEL = "channels";
    private static final String FILTER_CHORD = "chord";
    private static final String FILTER_MESSAGE = "messageType";
    private static final String FILTER_TRANSPOSE = "transpose";
    private static final String FILTER_VELOCITY = "velocity";

    private final MidiCore midiCore = App.getInstance().getMidiCore();
    private final MidiRouter midiRouter;
    private final OnConfigFinishedListener onConfigFinishedListener;
    private final CountDownWorkerThread workerThread;


    /* package */
    Configurator(@NonNull final MidiRouter midiRouter,
                 @Nullable final OnConfigFinishedListener listener) {

        this.midiRouter = midiRouter;
        this.onConfigFinishedListener = listener;
        this.workerThread = new CountDownWorkerThread(configRunner);
    }

    private final CountDownWorkerThread.CountDownRunnable<RouterConfig> configRunner =
            new CountDownWorkerThread.CountDownRunnable<RouterConfig>() {

        List<MidiCore.PortRecord> collectRecords(final List<String> deviceNicknames) {
            if (null == deviceNicknames) {
                return new ArrayList<>();
            }
            final List<MidiCore.PortRecord> result = new ArrayList<>(deviceNicknames.size());
            for (final String deviceNickname : deviceNicknames) {
                final MidiCore.PortRecord portRecord = midiCore.getPortRecord(deviceNickname);
                if (null != portRecord) {
                    result.add(portRecord);
                }
            }
            return result;
        }

        BaseFilter[] collectFilters(final Map<String, JsonObject> filterConfigs) {
            final BaseFilter[] result = new BaseFilter[filterConfigs.size()];
            int i = 0;
            for (final Map.Entry<String, JsonObject> filterConfig : filterConfigs.entrySet()) {
                final BaseFilter filter;
                switch (filterConfig.getKey()) {
                    case FILTER_CHANNEL:
                        filter = new ChannelFilter(filterConfig.getValue());
                        break;
                    case FILTER_CHORD:
                        filter = new ChordFilter(filterConfig.getValue());
                        break;
                    case FILTER_MESSAGE:
                        filter = new MessageTypeFilter(filterConfig.getValue());
                        break;
                    case FILTER_TRANSPOSE:
                        filter = new TransposeFilter(filterConfig.getValue());
                        break;
                    case FILTER_VELOCITY:
                        filter = new VelocityFilter(filterConfig.getValue());
                        break;
                    default:
                        continue;
                }
                result[i++] = filter;
            }
            return result;
        }

        @Override
        public void run(CountDownWorkerThread.Latcher latcher) {
            final RouterConfig config = getData();
            if (null == config) {
                throw new NullPointerException(
                        "The RouterConfig object provided was null. Configurator cannot run.");
            }
            try {
                final Handler callbackHandler = new Handler(getSubtaskLooper());
                // Iterate over mappings.
                final Map<String, RouterConfig.Mapping> mappings = config.getMappings();
                for (final Map.Entry<String, RouterConfig.Mapping> mappingEntry : mappings.entrySet()) {
                    final String mappingName = mappingEntry.getKey();
                    final RouterConfig.Mapping mappingConfig = mappingEntry.getValue();
                    // - Inputs
                    final List<MidiCore.PortRecord> inputRecords = collectRecords(mappingConfig.getInputs());
                    final Set<MidiInputController> inputControllers =
                            openInputs(inputRecords, latcher, getUnlatcher(), callbackHandler);
                    // - Outputs
                    final List<MidiCore.PortRecord> outputRecords = collectRecords(mappingConfig.getOutputs());
                    final Set<MidiOutputController> outputControllers =
                            openOutputs(outputRecords, latcher, getUnlatcher(), callbackHandler);
                    // - Filters
                    final Map<String, JsonObject> filterConfigs = mappingConfig.getFilters();
                    final BaseFilter[] filters = collectFilters(filterConfigs);
                    // - Mapping
                    final RouterMapping mapping =
                            new RouterMapping(mappingName, inputControllers, outputControllers);
                    mapping.addFilters(filters);
                    midiRouter.addMapping(mapping);
                }
                // Clock - TODO: Revisit when digital and analog clocks are implemented.
                // Sysex -TODO: Revisit when sysex is fully implemented.
                // Options -TODO: Revisit when options are implemented.
//                final RouterConfig.Options options = config.getOptions();
                // TODO: set setting for options.hotplug, options.syncConfigToUsb, options.verbose
                // Finished! Exec callback if set.
                if (null != onConfigFinishedListener) {
                    onConfigFinishedListener.onFinish();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /* package */
    void start(final RouterConfig config) {
        // Set data to the thread worker.
        configRunner.setData(config);
        // Start the thread.
        workerThread.start();
    }

    /* package */
    void createMapping() {
        // TODO: Construct mapping here, use openInputs()/openOutputs below
    }


    /* package */
    Set<MidiInputController> openInputs(final List<MidiCore.PortRecord> portRecords,
                                        final CountDownWorkerThread.Latcher latcher,
                                        final CountDownWorkerThread.Unlatcher unlatcher,
                                        final Handler callbackHandler)
            throws InterruptedException {

        final MidiDeviceController.OnControllerOpenedListener<MidiInputController> callback =
                new MidiDeviceController.OnControllerOpenedListener<MidiInputController>() {

                    @Override
                    public void onControllerOpened(@NonNull MidiInputController controller) {
                        unlatcher.unlatch(controller);
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        Log.e(TAG, String.format(Locale.US,
                                "Error encountered during InputController opening.\n%s", errorMessage));
                        unlatcher.unlatch();
                    }
                };
        for (final MidiCore.PortRecord portRecord : portRecords) {
            midiCore.openInput(portRecord, callback, callbackHandler);
        }
        return latcher.latch(portRecords.size(), true);
    }

    /* package */
    Set<MidiOutputController> openOutputs(final List<MidiCore.PortRecord> portRecords,
                                          final CountDownWorkerThread.Latcher latcher,
                                          final CountDownWorkerThread.Unlatcher unlatcher,
                                          final Handler callbackHandler)
            throws InterruptedException {

        final MidiDeviceController.OnControllerOpenedListener<MidiOutputController> callback =
                new MidiDeviceController.OnControllerOpenedListener<MidiOutputController>() {
                    @Override
                    public void onControllerOpened(@NonNull MidiOutputController controller) {
                        unlatcher.unlatch(controller);
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        Log.e(TAG, String.format(Locale.US,
                                "Error encountered during OutputController opening.\n%s", errorMessage));
                        unlatcher.unlatch();
                    }
                };
        for (final MidiCore.PortRecord portRecord : portRecords) {
            midiCore.openOutput(portRecord, callback, callbackHandler);
        }
        return latcher.latch(portRecords.size(), true);
    }

}
