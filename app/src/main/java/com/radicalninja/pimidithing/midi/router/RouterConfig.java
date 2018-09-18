package com.radicalninja.pimidithing.midi.router;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.radicalninja.pimidithing.App;
import com.radicalninja.pimidithing.util.FileUtils;
import com.radicalninja.pimidithing.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouterConfig {

    private static final String JSON_KEY_DEVICES = "devices";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_PORT = "port";

    private static final String JSON_KEY_MAPPINGS = "mappings";
    private static final String JSON_KEY_INPUTS = "inputs";
    private static final String JSON_KEY_OUTPUTS = "outputs";
    private static final String JSON_KEY_FILTERS = "filters";
    private static final String JSON_KEY_LISTEN = "listen";

    private static final String JSON_KEY_CLOCK = "clock";
    private static final String JSON_KEY_BPM = "bpm";
    private static final String JSON_KEY_PPQN = "ppqn";
    private static final String JSON_KEY_PATTERN_LENGTH = "patternLength";
    private static final String JSON_KEY_TAP_ENABLED = "tapEnabled";
    private static final String JSON_KEY_ANALOG = "analog";
    private static final String JSON_KEY_VOLUME = "volume";

    private static final String JSON_KEY_SYSEX = "sysex";
    private static final String JSON_KEY_PATH = "path";
    private static final String JSON_KEY_OUTPUT = "output";

    private static final String JSON_KEY_OPTIONS = "options";
    private static final String JSON_KEY_LED = "led";
    private static final String JSON_KEY_ENABLED = "enabled";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_HOTPLUG = "hotplug";
    private static final String JSON_KEY_SYNC_TO_USB = "syncConfigToUsb";
    private static final String JSON_KEY_VERBOSE = "verbose";

    private final Map<String, Device> devices = new HashMap<>();
    private final Map<String, Mapping> mappings = new HashMap<>();
    private final Clock clock = new Clock();
    private final List<Sysex> sysex = new ArrayList<>();
    private final Options options = new Options();

    public static RouterConfig fromPath(final String path) throws IOException {
        final String json = FileUtils.readAsString(path);
        return App.getInstance().getGson().fromJson(json, RouterConfig.class);
    }

    public Device getDevice(final String nickname) {
        return this.devices.get(nickname);
    }

    public boolean save(final String path) {
        final File output = new File(path);
        try {
            final String json = App.getInstance().getGson().toJson(this, RouterConfig.class);
            FileUtils.saveToFile(output, json);
        } catch (IOException e) {
            e.printStackTrace();
            // todo: return false?
        }
        // TODO: Figure out a better method to ensure the writing process worked.
        return output.isFile();
    }

    public Map<String, Device> getDevices() {
        return devices;
    }

    public Map<String, Mapping> getMappings() {
        return mappings;
    }

    public Clock getClock() {
        return clock;
    }

    public List<Sysex> getSysex() {
        return sysex;
    }

    public Options getOptions() {
        return options;
    }

    public static class Device {
        private String name;
        private Integer port;

        public String getName() {
            return name;
        }

        public Integer getPort() {
            return port;
        }
    }

    public static class Listen {
        private boolean clock;
        private boolean sysex;
        private boolean activeSense;

        public boolean isClock() {
            return clock;
        }

        public boolean isSysex() {
            return sysex;
        }

        public boolean isActiveSense() {
            return activeSense;
        }
    }

    public static class Mapping {
        private List<String> inputs;
        private List<String> outputs;
        private Map<String, JsonObject> filters;
        private Listen listen;

        public List<String> getInputs() {
            return inputs;
        }

        public List<String> getOutputs() {
            return outputs;
        }

        public Map<String, JsonObject> getFilters() {
            return filters;
        }

        public Listen getListen() {
            return listen;
        }
    }

    public static class Clock {
        private List<Device> inputs;
        private List<Device> outputs;
        private Integer bpm;
        private Integer ppqn;
        private boolean tapEnabled;
        private JsonObject analog;  // TODO

        public List<Device> getInputs() {
            return inputs;
        }

        public List<Device> getOutputs() {
            return outputs;
        }

        public Integer getBpm() {
            return bpm;
        }

        public Integer getPpqn() {
            return ppqn;
        }

        public boolean isTapEnabled() {
            return tapEnabled;
        }

        public JsonObject getAnalog() {
            return analog;
        }
    }

    public static class Sysex {
        private String path;
        private String output;

        public String getPath() {
            return path;
        }

        public String getOutput() {
            return output;
        }
    }

    public static class Options {
        private boolean hotplug;
        private boolean syncConfigToUsb;
        private boolean verbose;

        public boolean isHotplug() {
            return hotplug;
        }

        public boolean isSyncConfigToUsb() {
            return syncConfigToUsb;
        }

        public boolean isVerbose() {
            return verbose;
        }
    }

    public static class Adapter
            implements JsonSerializer<RouterConfig>, JsonDeserializer<RouterConfig> {

        public List<Device> fetchDevices(
                final JsonElement json, final Map<String, Device> devicePool,
                final JsonDeserializationContext context) {

            final List<Device> results = new ArrayList<>();
            final List<String> nicknames =
                    JsonUtils.getAsList(json, String.class, context);
            for (final String nickname : nicknames) {
                final Device device = devicePool.get(nickname);
                if (null != device) {
                    results.add(device);
                }
            }
            return results;
        }

        public List<Sysex> parseSysexJson(final JsonElement sysexJson) {
            final List<Sysex> result = new ArrayList<>();
            if (sysexJson.isJsonArray()) {
                for (final JsonElement sysexItemJson : sysexJson.getAsJsonArray()) {
                    if (sysexItemJson.isJsonObject()) {
                        final JsonObject sysexItemObject = sysexItemJson.getAsJsonObject();
                        final String path = JsonUtils.getString(sysexItemObject, JSON_KEY_PATH);
                        // TODO: Should this be changed to a device record lookup by output nickname?
                        final String output = JsonUtils.getString(sysexItemObject, JSON_KEY_OUTPUT);
                        if (null != path && null != output) {
                            final Sysex sysex = new Sysex();
                            sysex.path = path;
                            sysex.output = output;
                            result.add(sysex);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public RouterConfig deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            // TODO: try/catch!!!
            final RouterConfig config = new RouterConfig();
            final JsonObject _json = json.getAsJsonObject();
            // Parse devices
            final JsonObject devices = JsonUtils.getObject(_json, JSON_KEY_DEVICES);
            if (null != devices) {
                for (final Map.Entry<String, JsonElement> element : devices.entrySet()) {
                    final Device device = new Device();
                    // TODO: null/type check here
                    final JsonObject _deviceConfig = element.getValue().getAsJsonObject();
                    device.name = _deviceConfig.get(JSON_KEY_NAME).getAsString();
                    device.port = _deviceConfig.get(JSON_KEY_PORT).getAsInt();
                    config.devices.put(element.getKey(), device);
                }
            }
            // Parse mappings
            final JsonObject mappings = JsonUtils.getObject(_json, JSON_KEY_MAPPINGS);
            if (null != mappings) {
                for (final Map.Entry<String, JsonElement> element : mappings.entrySet()) {
                    if (null == element.getValue() || !element.getValue().isJsonObject()) {
                        continue;
                    }
                    final JsonObject mappingJson = element.getValue().getAsJsonObject();
                    final Mapping mapping = new Mapping();
                    mapping.inputs = JsonUtils.getAsList(mappingJson.get(JSON_KEY_INPUTS), String.class, context);
                    mapping.outputs = JsonUtils.getAsList(mappingJson.get(JSON_KEY_OUTPUTS), String.class, context);
                    mapping.filters = JsonUtils.getAllObjects(mappingJson.get(JSON_KEY_FILTERS));
                    // todo: listen
                    config.mappings.put(element.getKey(), mapping);
                }
            }
            // Parse Clock
            if (_json.has(JSON_KEY_CLOCK)) {
                // todo
            }
            // Parse Sysex
            if (_json.has(JSON_KEY_SYSEX)) {
                final List<Sysex> parsedSysex = parseSysexJson(_json.get(JSON_KEY_SYSEX));
                config.sysex.addAll(parsedSysex);
            }
            // Parse options
            if (_json.has(JSON_KEY_OPTIONS)) {
                config.options.hotplug = JsonUtils.getBoolean(_json, JSON_KEY_OPTIONS);
                config.options.syncConfigToUsb = JsonUtils.getBoolean(_json, JSON_KEY_SYNC_TO_USB);
                config.options.verbose = JsonUtils.getBoolean(_json, JSON_KEY_VERBOSE);
            }
            return config;
        }

        @Override
        public JsonElement serialize(
                RouterConfig src, Type typeOfSrc, JsonSerializationContext context) {

            final JsonObject json = new JsonObject();
            // TODO
            return json;
        }
    }
}
