package com.radicalninja.pimidithing.midi.router.filter;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.midi.router.RouterResult;
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
            populateAccessList(whitelist, json);
        }
        if (settings.has(KEY_BLACKLIST)) {
            final JsonArray json = settings.getAsJsonArray(KEY_BLACKLIST);
            populateAccessList(blacklist, json);
        }
    }

    protected void populateAccessList(final NumberArray accessList, final JsonArray jsonArray) {
        for (final JsonElement item : jsonArray) {
            try {
                final JsonPrimitive _item = item.getAsJsonPrimitive();
                if (_item.isNumber()) {
                    accessList.add(_item.getAsInt());
                } else if (_item.isString()) {
                    final String typeString = _item.getAsString();
                    final MidiMessage.MessageType type = MidiMessage.MessageType.fromString(typeString);
                    if (null != type) {
                        accessList.add(type.value);
                    }
                }
            } catch (ClassCastException | IllegalStateException e) {
                Log.e(TAG, "Encountered an error populating the access list.", e);
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
    RouterResult onProcess(MidiMessage message) {
        if (!whitelist.isEmpty()) {
            if (whitelist.has(message.getType().value)) {
                return RouterResult.failed();
            }
        } else if (!blacklist.isEmpty()) {
            if (blacklist.has(message.getType().value)) {
                return RouterResult.failed();
            }
        }
        return new RouterResult(message);
    }

}
