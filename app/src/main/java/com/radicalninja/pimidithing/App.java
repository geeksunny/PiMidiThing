package com.radicalninja.pimidithing;

import android.app.Application;
import android.media.midi.MidiDeviceInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radicalninja.pimidithing.midi.Configuration;
import com.radicalninja.pimidithing.midi.Core;

public class App extends Application {

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    private Core core;
    private Gson gson;
    private MassStorageController massStorageController;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initGson();
        initUsbHotplug();
        initMidiCore();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initGson() {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Configuration.class, new Configuration.Adapter());
        gson = builder.create();
    }

    private void initUsbHotplug() {
        massStorageController = new MassStorageController(this);
    }

    private void initMidiCore() {
        core = new Core(this);
        final MidiDeviceInfo[] devices = core.getDevices();
    }

    public Core getCore() {
        return core;
    }

    public Gson getGson() {
        return gson;
    }

    public MassStorageController getMassStorageController() {
        return massStorageController;
    }
}
