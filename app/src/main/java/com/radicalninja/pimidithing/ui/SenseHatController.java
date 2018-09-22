package com.radicalninja.pimidithing.ui;

import android.graphics.Color;
import android.hardware.SensorManager;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.SenseHat;
import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;

import androidx.annotation.Nullable;

public class SenseHatController {

    public static SenseHatController init(final SensorManager sensorManager) {
        try {
            SenseHat senseHat = SenseHat.init(sensorManager);
            return new SenseHatController(senseHat);
        } catch (IOException e) {
            Log.w(TAG, "Encountered an error opening SenseHAT. Interactions are disabled.", e);
            return new SenseHatController(null);
        }
    }

    private static final String TAG = SenseHatController.class.getCanonicalName();

    private final boolean enabled;
    private final SenseHat senseHat;
    private final LedMatrix ledMatrix;

    // TODO: Animation thread

    private SenseHatController(@Nullable final SenseHat senseHat) {
        enabled = null != senseHat;
        this.senseHat = senseHat;
        ledMatrix = (enabled) ? senseHat.getLedMatrix() : null;
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

    public void blinkColor(/* color */) {
        // TODO
    }

}
