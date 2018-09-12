package com.radicalninja.pimidithing;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.SenseHat;
import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radicalninja.pimidithing.midi.MidiCore;
import com.radicalninja.pimidithing.midi.router.MidiRouter;
import com.radicalninja.pimidithing.midi.router.RouterConfig;
import com.radicalninja.pimidithing.usb.MassStorageController;

import java.io.IOException;

public class App extends Application implements MidiRouter.OnRouterReadyListener {

    private static final String TAG = App.class.getCanonicalName();

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    private MidiCore midiCore;
    private Gson gson;
    private MassStorageController massStorageController;

    private LedMatrix ledMatrix;
    private SenseHat senseHat;
    private SensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initGson();
        initUsbHotplug();
        initMidiCore();
        initSenseHat();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initGson() {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(RouterConfig.class, new RouterConfig.Adapter());
        gson = builder.create();
    }

    private void initUsbHotplug() {
        massStorageController = new MassStorageController(this);
    }

    private void initMidiCore() {
        midiCore = new MidiCore(this, this);
    }

    @Override
    public void onRouterReady() {
        // TODO: Alert user that router is ready!
        Log.i(TAG, "MidiRouter is configured and ready!");
    }

    private void initSenseHat() {
        try {
            // TODO: Joystick monitoring
            // TODO: alert messages
            // TODO: Handle this feature as optional. Exception will be thrown when sensehat is not found.
            sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            senseHat = SenseHat.init(sensorManager);
            ledMatrix = senseHat.getLedMatrix();
            ledMatrix.draw(Color.TRANSPARENT);
        } catch (IOException e) {
            Log.e(TAG, "Error while initializing SenseHAT!", e);
        }
    }

    public MidiCore getMidiCore() {
        return midiCore;
    }

    public Gson getGson() {
        return gson;
    }

    public MassStorageController getMassStorageController() {
        return massStorageController;
    }

}
