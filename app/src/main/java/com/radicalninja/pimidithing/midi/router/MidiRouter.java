package com.radicalninja.pimidithing.midi.router;

public class MidiRouter {

    private RouterConfig config;

    public MidiRouter(final RouterConfig config) {
        setConfig(config);
    }

    protected void setConfig(final RouterConfig config) {
        this.config = config;
        // TODO: SETUP MAPPINGS AND OTHER FEATURES HERE
    }

}
