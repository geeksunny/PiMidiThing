package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.NumberArray;

public class MessageTypeFilter extends BaseFilter {

    private static final String TAG = MessageTypeFilter.class.getCanonicalName();

    private static final String KEY_WHITELIST = "whitelist";
    private static final String KEY_BLACKLIST = "blacklist";

    private NumberArray whitelist = new NumberArray();
    private NumberArray blacklist = new NumberArray();

    public MessageTypeFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        if (settings.has(KEY_WHITELIST)) {
            final JsonArray json = settings.getAsJsonArray(KEY_WHITELIST);
            for (final JsonElement item : json) {
                try {
                    final JsonPrimitive _item = item.getAsJsonPrimitive();
                    if (_item.isNumber()) {
                        whitelist.add(_item.getAsInt());
                    } else if (_item.isString()) {
                        final String typeString = _item.getAsString();
                        // TODO: Look up numerical value of message type string
                        // TODO: Add number to whitelist.
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
                    final JsonPrimitive _item = item.getAsJsonPrimitive();
                    if (_item.isNumber()) {
                        blacklist.add(_item.getAsInt());
                    } else if (_item.isString()) {
                        final String typeString = _item.getAsString();
                        // TODO: Look up numerical value of message type string
                        // TODO: Add number to blacklist.
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
        // TODO!!!
        return null;
    }

}
