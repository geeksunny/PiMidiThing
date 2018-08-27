package com.radicalninja.pimidithing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbReceiver extends BroadcastReceiver {

    public static final String TAG = UsbReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        synchronized (this) {
            final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (!MassStorageController.isMassStorage(device)) {
                Log.d(TAG, "This device is not USB Mass Storage.");
                return;
            }
            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    App.getInstance().getMassStorageController().onDeviceAttached(device);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    App.getInstance().getMassStorageController().onDeviceDetached(device);
                    break;
                default:
                    Log.d(TAG, "UsbReceiver called with invalid action");
            }
        }
    }

}
