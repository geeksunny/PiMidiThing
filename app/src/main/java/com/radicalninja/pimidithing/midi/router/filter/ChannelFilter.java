package com.radicalninja.pimidithing.midi.router.filter;

import android.util.SparseIntArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.NumberArray;

import java.util.Map;

public class ChannelFilter extends BaseFilter {

    private static final String TAG = ChannelFilter.class.getCanonicalName();

    private static final String KEY_MAP = "map";
    private static final String KEY_WHITELIST = "whitelist";
    private static final String KEY_BLACKLIST = "blacklist";

    private SparseIntArray map = new SparseIntArray();
    private NumberArray whitelist = new NumberArray();
    private NumberArray blacklist = new NumberArray();

    public ChannelFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        if (settings.has(KEY_MAP)) {
            final JsonObject json = settings.getAsJsonObject(KEY_MAP);
            for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
                try {
                    final int from = Integer.valueOf(entry.getKey());
                    final int to = entry.getValue().getAsInt();
                    map.put(from, to);
                } catch (ClassCastException | IllegalStateException e) {
                    // TODO: Handle error
                }
            }
        }
        if (settings.has(KEY_WHITELIST)) {
            final JsonArray json = settings.getAsJsonArray(KEY_WHITELIST);
            for (final JsonElement item : json) {
                try {
                    if (item.isJsonPrimitive()) {
                        whitelist.add(item.getAsInt());
                    }
                } catch (ClassCastException | IllegalStateException e) {
                    // TODO: Handle error
                }
            }
        }
        if (settings.has(KEY_BLACKLIST)) {
            final JsonArray json = settings.getAsJsonArray(KEY_BLACKLIST);
            for (final JsonElement item : json) {
                try {
                    if (item.isJsonPrimitive()) {
                        blacklist.add(item.getAsInt());
                    }
                } catch (ClassCastException | IllegalStateException e) {
                    // TODO: Handle error
                }
            }
        }
    }

    @Override
    public JsonObject getSettings() {
        // TODO map, whitelist, blacklist
        return null;
    }

    @Override
    Result onProcess(MidiMessage message) {
        // TODO!!!
        return null;
    }

}
