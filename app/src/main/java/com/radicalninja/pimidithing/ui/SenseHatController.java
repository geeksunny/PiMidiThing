package com.radicalninja.pimidithing.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
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

    // TODO: Animation thread

    // All draws take place on the display thread.
    private final LedDisplayThread displayThread;

    private SenseHatController(@Nullable final SenseHat senseHat,
                               @Nullable final Resources resources) {

        enabled = null != senseHat;
        this.senseHat = senseHat;
        this.ledMatrix = (enabled) ? senseHat.getLedMatrix() : null;
        this.resources = (enabled) ? resources : null;
        displayThread = new LedDisplayThread(ledMatrix);
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
        try {
            // TODO: Implement message scrolling
            throw new IOException();
        } catch (IOException e) {
            Log.e(TAG, "Error while displaying message on LED Matrix", e);
        }
    }

    public void displayIcon(final LedIcon icon) {
        if (!enabled) {
            return;
        }
        try {
            final Bitmap iconBitmap = icon.createBitmap();
            ledMatrix.draw(iconBitmap);
            iconBitmap.recycle();
        } catch (IOException e) {
            Log.e(TAG, "Error while displaying icon on LED Matrix", e);
        }
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
