package com.radicalninja.pimidithing.midi.router;

import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiMessage;

import java.util.ArrayList;
import java.util.List;

public class MidiRouter {

    private final List<RouterMapping> mappings = new ArrayList<>();

    private boolean started, paused;
    private RouterConfig config;

    public MidiRouter(final RouterConfig config) {
        setConfig(config);
    }

    protected void setConfig(final RouterConfig config) {
        this.config = config;
        // TODO: SETUP MAPPINGS AND OTHER FEATURES HERE
    }

    RouterMapping.MappingMessageListener onMessage = new RouterMapping.MappingMessageListener() {
        @Override
        public boolean onMessage(MidiInputController input, MidiMessage message, RouterMapping mapping) {
            if (paused || !started) {
                return false;
            }
            final RouterMapping.Result result = mapping.process(message);
            // TODO: IF result === true, return true
            // TODO: IF processed != null - BROADCAST ALL MESSAGES RETURNED
            return false;
        }
    };

    void pause() {
        // TODO: Pause
    }

    void unpause() {
        // TODO: Unpause
    }

    void toggle() {
        // TODO: Toggle paused value
    }

    void stop() {
        // TODO:
    }

    /* package */
    void addMapping(final RouterMapping mapping) {
        if (!mappings.contains(mapping)) {
            mappings.add(mapping);
        }
    }

    /* package */
    void removeMapping(final String name) {
        // TODO: Remove mapping by name?
        // TODO: Deactivate and delete!
    }

    /* package */
    void removeMapping(final RouterMapping mapping) {
        // TODO: Deactivate and delete!
    }

}
