package com.radicalninja.pimidithing.midi.router;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiMessage;

import java.util.ArrayList;
import java.util.List;

public class MidiRouter {

    public interface OnRouterReadyListener {
        void onRouterReady();
        void onRouterError(final String message, @Nullable final Throwable error);
    }

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

    public void init() {
        if (started) {
            // Router is already started!
            return;
        }
        final Configurator configurator = new Configurator(this, null);
        configurator.start(config);
    }

    public void init(@NonNull final OnRouterReadyListener listener,
                     @NonNull final Handler callbackHandler) {

        if (started) {
            listener.onRouterError("Router already started!", null);
            return;
        }
        final Configurator.OnConfigFinishedListener onConfigFinished =
                new Configurator.OnConfigFinishedListener() {
                    @Override
                    public void onFinish() {
                        final Runnable callback = new Runnable() {
                            @Override
                            public void run() {
                                listener.onRouterReady();
                            }
                        };
                        callbackHandler.post(callback);
                    }
                };
        final Configurator configurator = new Configurator(this, onConfigFinished);
        configurator.start(config);
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
