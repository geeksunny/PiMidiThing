package com.radicalninja.pimidithing.midi.router;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.radicalninja.pimidithing.midi.MidiInputController;
import com.radicalninja.pimidithing.midi.MidiMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiRouter {

    public interface OnRouterReadyListener {
        void onRouterReady();
        void onRouterError(final String message, @Nullable final Throwable error);
    }

    private final List<RouterMapping> mappings = new ArrayList<>();

    private boolean started = false;
    private boolean paused = false;
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
        final Configurator.OnConfigFinishedListener onConfigFinished =
                new Configurator.OnConfigFinishedListener() {
                    @Override
                    public void onFinish() {
                        started = true;
                    }
                };
        final Configurator configurator = new Configurator(this, onConfigFinished);
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
                        started = true;
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
            final RouterResult result = mapping.process(message);
            if (result.shouldBroadcast()) {
                try {
                    mapping.broadcast(result.getMessages());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // TODO: Make sure the return value here is correct...
            return result.isConsumed();
        }
    };

    public boolean started() {
        return started;
    }

    void pause() {
        paused = true;
    }

    void unpause() {
        paused = false;
    }

    void toggle() {
        paused = !paused;
    }

    void stop() {
        // TODO:
    }

    /* package */
    void addMapping(final RouterMapping mapping) {
        if (!mappings.contains(mapping)) {
            mappings.add(mapping);
            mapping.activate(onMessage);
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
