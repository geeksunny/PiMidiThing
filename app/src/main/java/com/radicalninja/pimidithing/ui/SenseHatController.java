package com.radicalninja.pimidithing.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.SenseHat;
import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class SenseHatController {

    public static SenseHatController init(final Context context) {
        if (null != instance) {
            Log.w(TAG, "SenseHatController instance was already initialized.");
            return instance;
        }
        SenseHatController controller;
        try {
            final SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            final SenseHat senseHat = SenseHat.init(sensorManager);
            controller = new SenseHatController(senseHat, context.getResources());
        } catch (IOException e) {
            Log.w(TAG, "Encountered an error opening SenseHAT. Interactions are disabled.", e);
            controller = new SenseHatController(null, null);
        }
        instance = controller;
        return controller;
    }

    public static SenseHatController getInstance() {
        if (null == instance) {
            throw new IllegalStateException("SenseHatController has not been initialized.");
        }
        return instance;
    }

    private static final String TAG = SenseHatController.class.getCanonicalName();

    private static SenseHatController instance;

    private final boolean enabled;
    private final SenseHat senseHat;
    private final LedMatrix ledMatrix;
    private final Resources resources;

    // All draws (except initial blanking) take place on the display thread.
    private final LedDisplayThread displayThread;

    private Typeface typeface;

    private SenseHatController(@Nullable final SenseHat senseHat,
                               @Nullable final Resources resources) {

        enabled = null != senseHat;
        this.senseHat = senseHat;
        this.ledMatrix = (enabled) ? senseHat.getLedMatrix() : null;
        this.resources = (enabled) ? resources : null;
        displayThread = (enabled) ? new LedDisplayThread(ledMatrix) : null;
    }

    protected Typeface getTypeface() {
        if (null == typeface) {
            typeface = Typeface.createFromAsset(resources.getAssets(), "pixelated.ttf");
        }
        return typeface;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void blankDisplay() {
        if (!enabled) {
            return;
        }
        try {
            ledMatrix.draw(Color.TRANSPARENT);
        } catch (IOException e) {
            Log.e(TAG, "Error while blanking LED Matrix", e);
        }
    }

    public void displayMessage(final String message) {
        if (!enabled) {
            return;
        }
        final LedText msg = new LedText(resources, getTypeface());
        msg.setMessage(message);
        final LedDisplayThread.Job job =
                new LedDisplayThread.JobBuilder().setFrame(msg.createBitmap()).build();
        displayThread.queueJob(job);
    }

    public void displayIcon(final LedIcon icon) {
        if (!enabled) {
            return;
        }
        final LedDisplayThread.Job job =
                new LedDisplayThread.JobBuilder().setFrame(icon.createBitmap()).build();
        displayThread.queueJob(job);
    }

    public void blinkColor(final long intervalDuration, @ColorInt final int color) {
        if (!enabled) {
            return;
        }
        // TODO
    }

    public void blinkColors(final long intervalDuration, @ColorInt final int ... colors) {
        if (!enabled) {
            return;
        }
        // TODO
    }

}
