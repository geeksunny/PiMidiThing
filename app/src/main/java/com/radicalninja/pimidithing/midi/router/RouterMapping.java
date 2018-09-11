package com.radicalninja.pimidithing.midi.router;

import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.midi.MidiOutputController;
import com.radicalninja.pimidithing.midi.router.filter.BaseFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* package */
class RouterMapping {

    /* package */
    interface MappingMessageListener {
        boolean onMessage(final MidiInputController input,
                          final MidiMessage message, final RouterMapping mapping);
    }

    private final List<MidiInputController> inputs = new ArrayList<>();
    private final List<MidiOutputController> outputs = new ArrayList<>();
    private final List<BaseFilter> filters = new ArrayList<>();
    private final String name;

    private boolean activated = false;
    private MidiInputController.MessageListener midiMessageListener;

    /* package */
    RouterMapping(final String name, final List<MidiInputController> inputs,
                  final List<MidiOutputController> outputs) {

        this.name = name;
        this.inputs.addAll(inputs);
        this.outputs.addAll(outputs);
    }

    /* package */
    <F extends BaseFilter> void addFilter(final F filter) {
        if (!filters.contains(filter)) {
            filters.add(filter);
        }
    }

    /* package */
    <F extends BaseFilter> void addFilters(final List<F> filters) {
        for (final F filter : filters) {
            if (!this.filters.contains(filter)) {
                this.filters.add(filter);
            }
        }
    }

    /* package */
    <F extends BaseFilter> void addFilters(final F[] filters) {
        for (final F filter : filters) {
            if (!this.filters.contains(filter)) {
                this.filters.add(filter);
            }
        }
    }

    /* package */
    Result process(final MidiMessage message) {
        final Result result = new Result();
        for (final BaseFilter filter : filters) {
            // TODO
        }
        return result;
    }

    /* package */
    boolean activate(final MappingMessageListener mappingMessageListener) {
        if (activated || null == mappingMessageListener) {
            return false;
        }
        activated = true;
        midiMessageListener = new MidiInputController.MessageListener() {
            @Override
            public boolean onMessage(MidiInputController input, MidiMessage message) {
                return mappingMessageListener.onMessage(
                        input, message, RouterMapping.this);
            }
        };
        for (final MidiInputController input : inputs) {
            input.addMessageListener(midiMessageListener);
        }
        return true;
    }

    /* package */
    boolean deactivate() {
        if (!activated) {
            return false;
        }
        activated = false;
        for (final MidiInputController input : inputs) {
            input.removeMessageListener(midiMessageListener);
        }
        midiMessageListener = null;
        return true;
    }

    /* package */
    void broadcast(final MidiMessage message) throws IOException {
        for (final MidiOutputController output : outputs) {
            output.send(message);
        }
    }

    /* package */
    static class Result {
        // TODO: Build class around requirements of process(msg)
    }

}
