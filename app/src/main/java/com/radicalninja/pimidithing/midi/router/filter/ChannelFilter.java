package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.NumberArray;
import com.radicalninja.pimidithing.util.NumberMap;

import java.util.Map;

public class ChannelFilter extends BaseFilter {

    private static final String TAG = ChannelFilter.class.getCanonicalName();

    private static final String KEY_MAP = "map";
    private static final String KEY_WHITELIST = "whitelist";
    private static final String KEY_BLACKLIST = "blacklist";

    private NumberMap map = new NumberMap();
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
        final JsonObject json = new JsonObject();

        final JsonObject jsonMap = new JsonObject();
        for (final NumberMap.Entry entry : map) {
            jsonMap.add(String.valueOf(entry.getKey()), new JsonPrimitive(entry.getValue()));
        }
        json.add(KEY_MAP, jsonMap);

        final JsonArray jsonWhitelist = new JsonArray(whitelist.size());
        NumberArray.NumberArrayIterator i = whitelist.iterator();
        while (i.hasNext()) {
            jsonWhitelist.add(i.nextInt());
        }
        json.add(KEY_WHITELIST, jsonWhitelist);

        final JsonArray jsonBlacklist = new JsonArray(blacklist.size());
        i = blacklist.iterator();
        while (i.hasNext()) {
            jsonBlacklist.add(i.nextInt());
        }
        json.add(KEY_BLACKLIST, jsonBlacklist);

        return json;
    }

    @Override
    Result onProcess(MidiMessage message) {
        final int channel = message.getChannel();
        if (!whitelist.isEmpty()) {
            if (!whitelist.has(channel)) {
                return Result.failed();
            }
        } else if (!blacklist.isEmpty()) {
            if (blacklist.has(channel)) {
                return Result.failed();
            }
        }
        final int mapping = map.get(channel, -1);
        if (mapping > 0) {
            message.setChannel(mapping);
        }
        return new Result(message);
    }

}
