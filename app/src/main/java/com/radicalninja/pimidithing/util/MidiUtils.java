package com.radicalninja.pimidithing.util;

import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.util.Log;

public class MidiUtils {

    public static void listDeviceInfo(final MidiDeviceInfo info) {
        final Bundle props = info.getProperties();
        Log.d("MIDI", String.format("Manufacturer: %s", props.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER)));
        Log.d("MIDI", String.format("Name: %s", props.getString(MidiDeviceInfo.PROPERTY_NAME)));
        Log.d("MIDI", String.format("Product: %s", props.getString(MidiDeviceInfo.PROPERTY_PRODUCT)));
        Log.d("MIDI", String.format("Serial Number: %s", props.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER)));
//            Log.d("MIDI", String.format("USB Device: %s", props.getString(MidiDeviceInfo.PROPERTY_USB_DEVICE)));
        final MidiDeviceInfo.PortInfo[] portInfos = info.getPorts();
        for (final MidiDeviceInfo.PortInfo portInfo : portInfos) {
            Log.d("MIDI", "Starting a PortInfo");
            Log.d("MIDI", String.format("Port Number: %d", portInfo.getPortNumber()));
            Log.d("MIDI", String.format("Port Type: %d", portInfo.getType()));
        }
    }

}
