package com.radicalninja.pimidithing.midi.router.clock;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class ClockService extends IntentService {

    public ClockService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO: Stubbed!!
        // TODO: Operate DigitalClock/AnalogClock here?
    }

}
