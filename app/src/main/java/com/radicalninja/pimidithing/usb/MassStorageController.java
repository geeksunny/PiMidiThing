package com.radicalninja.pimidithing.usb;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MassStorageController {

    public interface UsbMassStorageListener {
        void onStorageAttached(final UsbMassStorageDevice device);
        void onStorageDetached(final UsbMassStorageDevice device);
    }

    public static boolean isMassStorage(final UsbDevice device) {
        try {
            return device.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE;
        } catch (Exception e) {
            return false;
        }
    }

    public static void showUsbDeviceDetails(final UsbDevice device) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Configuration Count: "+device.getConfigurationCount()+"\n");
        // todo: configuration for loop
        sb.append("Device Class: "+device.getDeviceClass()+"\n");
        sb.append("Device ID: "+device.getDeviceId()+"\n");
        sb.append("Device Name: "+device.getDeviceName()+"\n");
        sb.append("Device Protocol: "+device.getDeviceProtocol()+"\n");
        sb.append("Device Subclass: "+device.getDeviceSubclass()+"\n");
        sb.append("Interface Count: "+device.getInterfaceCount()+"\n");
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            final UsbInterface usbInterface = device.getInterface(i);
            sb.append("    INTERFACE NAME: "+usbInterface.getName()+"\n");
            sb.append("    INTERFACE CLASS: "+usbInterface.getInterfaceClass()+"\n");
        }
        sb.append("Manufacturer Name: "+device.getManufacturerName()+"\n");
        sb.append("Product ID: "+device.getProductId()+"\n");
        sb.append("Product Name: "+device.getProductName()+"\n");
        sb.append("Serial Number: "+device.getSerialNumber()+"\n");
        sb.append("Vendor ID: "+device.getVendorId()+"\n");
        sb.append("Version: "+device.getVersion()+"\n");
        Log.d(TAG, sb.toString());
    }

    private static final String TAG = MassStorageController.class.getCanonicalName();

    private final List<UsbMassStorageListener> storageListeners = new ArrayList<>();
    private final Map<String, UsbMassStorageDevice> storageDevices = new HashMap<>();

    private final Context context;

    public MassStorageController(final Context context) {
        this.context = context;
        pollConnectedDevices(false);
    }

    protected synchronized void pollConnectedDevices(final boolean alertListenersOnNew) {
        final UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(context);
        for (final UsbMassStorageDevice device : devices) {
            final String key = device.getUsbDevice().getDeviceName();
            final boolean alert = alertListenersOnNew && !storageDevices.containsKey(key);
            storageDevices.put(key, device);
            if (alert) {
                // TODO: Should there be a flag to cull stray device records?
                for (final UsbMassStorageListener listener : storageListeners) {
                    listener.onStorageAttached(device);
                }
            }
        }
    }

    public synchronized UsbMassStorageDevice getMassStorageDevice(final UsbDevice device) {
        final UsbMassStorageDevice[] storageDevices =
                UsbMassStorageDevice.getMassStorageDevices(context);
        for (final UsbMassStorageDevice storageDevice : storageDevices) {
            if (device.equals(storageDevice.getUsbDevice())) {
                return storageDevice;
            }
        }
        return null;
    }

    public synchronized void onDeviceAttached(final UsbDevice device) {
        Log.d(TAG, "onDeviceAttached called.");
        if (storageDevices.containsKey(device.getSerialNumber())) {
            Log.d(TAG, "USB device was already registered. Skipping.");
            return;
        }
        final UsbMassStorageDevice storageDevice = getMassStorageDevice(device);
        if (null != storageDevice) {
            storageDevices.put(device.getSerialNumber(), storageDevice);
            for (final UsbMassStorageListener listener : storageListeners) {
                listener.onStorageAttached(storageDevice);
            }
        }
    }

    public synchronized void onDeviceDetached(final UsbDevice device) {
        Log.d(TAG, "onDeviceDetached called.");
        if (!storageDevices.containsKey(device.getSerialNumber())) {
            Log.d(TAG, "USB device was already removed. Skipping.");
            return;
        }
        final UsbMassStorageDevice storageDevice = getMassStorageDevice(device);
        if (null == storageDevice) {
            storageDevices.remove(device.getSerialNumber());
            for (final UsbMassStorageListener listener : storageListeners) {
                listener.onStorageDetached(storageDevice);
            }
        }
    }
    public synchronized boolean addListener(final UsbMassStorageListener listener) {
        if (!storageListeners.contains(listener)) {
            return storageListeners.add(listener);
        } else {
            Log.w(TAG, "Listener was already in the list. Skipping add.");
            return false;
        }
    }

    public synchronized boolean removeListener(final UsbMassStorageListener listener) {
        return storageListeners.remove(listener);
    }

    public synchronized void clearListeners() {
        storageListeners.clear();
    }

}
