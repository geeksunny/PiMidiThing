package com.radicalninja.pimidithing;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radicalninja.pimidithing.midi.MidiCore;
import com.radicalninja.pimidithing.midi.router.MidiRouter;
import com.radicalninja.pimidithing.midi.router.RouterConfig;
import com.radicalninja.pimidithing.ui.SenseHatController;
import com.radicalninja.pimidithing.usb.MassStorageController;

import java.io.InputStream;
import java.io.InputStreamReader;

public class App extends Application implements MidiRouter.OnRouterReadyListener {

    private static final String TAG = App.class.getCanonicalName();

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    private MidiCore midiCore;
    private Gson gson;
    private MassStorageController massStorageController;
    private SenseHatController senseHatController;

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
        Log.d(TAG, "Beginning MidiCore init");
        final InputStream defaultConfig = getResources().openRawResource(R.raw.config);
        final RouterConfig config =
                gson.fromJson(new InputStreamReader(defaultConfig), RouterConfig.class);

        midiCore = new MidiCore(this, config);
        midiCore.initRouter(this);
    }

    @Override
    public void onRouterReady() {
        // TODO: Alert user that router is ready!
        Log.i(TAG, "MidiRouter is configured and ready!");
    }

    @Override
    public void onRouterError(String message, @Nullable Throwable error) {
        Log.e(TAG, message, error);
    }

    private void initSenseHat() {
        senseHatController = SenseHatController.init(this);
        senseHatController.blankDisplay();
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
