package com.radicalninja.pimidithing.midi.router;

import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.App;
import com.radicalninja.pimidithing.ParkableWorkerThread;
import com.radicalninja.pimidithing.midi.MidiCore;
import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiOutputController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* package */class Configurator {

    private static final String TAG = Configurator.class.getCanonicalName();

    private final MidiCore midiCore = App.getInstance().getMidiCore();
    private final MidiRouter midiRouter;
    private final ParkableWorkerThread workerThread;

    public Configurator(MidiRouter midiRouter) {
        this.midiRouter = midiRouter;
        this.workerThread = new ParkableWorkerThread(configRunner);
        // TODO: Begin configuration process on `thread` and use a CountDownLatch or Condition to `await` until callbacks are called.
    }

    private final ParkableWorkerThread.ParkableRunnable<RouterConfig> configRunner =
            new ParkableWorkerThread.ParkableRunnable<RouterConfig>() {

        List<MidiCore.PortRecord> collectRecords(final List<RouterConfig.Device> devices) {
            final List<MidiCore.PortRecord> result = new ArrayList<>(devices.size());
            for (final RouterConfig.Device device : devices) {
                final MidiCore.PortRecord portRecord = midiCore.getPortRecord(device.getName());
            }
            return result;
        }

        @Override
        public void run(ParkableWorkerThread.ParkingValet parkingValet) {
            final RouterConfig config = getData();
            if (null == config) {
                // TODO: ENDED EARLY!!! Handle this situation.
                return;
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
                    final List<MidiInputController> inputControllers =
                            openInputs(inputRecords, parkingValet, getRetrievingValet(), callbackHandler);
                    // - Outputs
                    final List<MidiCore.PortRecord> outputRecords = collectRecords(mappingConfig.getOutputs());
                    final List<MidiOutputController> outputControllers =
                            openOutputs(outputRecords, parkingValet, getRetrievingValet(), callbackHandler);
                    // - Filters
                    final Map<String, JsonObject> filterConfigs = mappingConfig.getFilters();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Clock
            // Sysex
            // Options
        }
    };

    /* package */void start(final RouterConfig config) {
        // Set data to the thread worker.
        configRunner.setData(config);
        // Start the thread.
        workerThread.start();
    }

    /* package */void createMapping() {
        // TODO: Construct mapping here, use openInputs()/openOutputs below
    }


    /* package */
    List<MidiInputController> openInputs(final List<MidiCore.PortRecord> portRecords,
                                         final ParkableWorkerThread.ParkingValet parkingValet,
                                         final ParkableWorkerThread.RetrievingValet retrievingValet,
                                         final Handler callbackHandler)
            throws InterruptedException {

        final MidiCore.OnControllersOpenedListener<MidiInputController> callback =
                new MidiCore.OnControllersOpenedListener<MidiInputController>() {

                    @Override
                    public void onControllersOpened(List<MidiInputController> inputs) {
                        retrievingValet.unpark(inputs);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, String.format(Locale.US,
                                "Error encountered during InputController opening.\n%s", errorMessage));
                        retrievingValet.unpark();
                    }
                };
        midiCore.openInputs(portRecords, callback, callbackHandler);
        return parkingValet.park();
    }

    /* package */
    List<MidiOutputController> openOutputs(final List<MidiCore.PortRecord> portRecords,
                                         final ParkableWorkerThread.ParkingValet parkingValet,
                                         final ParkableWorkerThread.RetrievingValet retrievingValet,
                                         final Handler callbackHandler)
            throws InterruptedException {

        final MidiCore.OnControllersOpenedListener<MidiOutputController> callback =
                new MidiCore.OnControllersOpenedListener<MidiOutputController>() {

                    @Override
                    public void onControllersOpened(List<MidiOutputController> outputs) {
                        retrievingValet.unpark(outputs);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, String.format(Locale.US,
                                "Error encountered during OutputController opening.\n%s", errorMessage));
                        retrievingValet.unpark();
                    }
                };
        midiCore.openOutputs(portRecords, callback, callbackHandler);
        return parkingValet.park();
    }
}
