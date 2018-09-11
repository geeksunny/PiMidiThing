package com.radicalninja.pimidithing.util;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonUtils {

    /**
     * Retrieve a boolean value from a JsonObject. If a valid boolean value cannot be located,
     * false will be returned.
     *
     * Equivalent to getBoolean(json, key, false)
     * @param json the parent JsonObject of the targeted boolean value.
     * @param key the key at which the boolean JSON value is stored.
     * @return the boolean value of the JSON key. Returns false as a default value in the cases of...
     *      * if `json` is null.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a boolean value.
     */
    public static boolean getBoolean(final JsonObject json, final String key) {
        boolean value = false;
        if (null != json && !TextUtils.isEmpty(key) && json.has(key)) {
            final JsonElement targetJson = json.get(key);
            if (targetJson.isJsonPrimitive()) {
                final JsonPrimitive targetJsonPrimitive = targetJson.getAsJsonPrimitive();
                if (targetJsonPrimitive.isBoolean()) {
                    value = targetJsonPrimitive.getAsBoolean();
                }
            }
        }
        return value;
    }

    /**
     * Retrieve a boolean value from a JsonObject. If a valid boolean value does not exist,
     * the provided default value will be returned.
     * @param json the parent JsonObject of the targeted boolean value.
     * @param key the key at which the boolean JSON value is stored.
     * @param defaultValue the value to be returned if a value cannot be located.
     * @return the boolean value of the JSON key. Returns `defaultValue` in the cases of...
     *      * if `json` is null.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a boolean value.
     */
    public static boolean getBoolean(final JsonObject json,
                                     final String key, final boolean defaultValue) {
        boolean value = defaultValue;
        if (null != json && !TextUtils.isEmpty(key) && json.has(key)) {
            final JsonElement targetJson = json.get(key);
            if (targetJson.isJsonPrimitive()) {
                final JsonPrimitive targetJsonPrimitive = targetJson.getAsJsonPrimitive();
                if (targetJsonPrimitive.isBoolean()) {
                    value = targetJsonPrimitive.getAsBoolean();
                }
            }
        }
        return value;
    }

    public static <T> List<T> getAsList(
            final JsonElement json, final Class<T> tClass, final JsonDeserializationContext context) {
        final Class listClass = new TypeToken<List<T>>(){}.getRawType();
        return context.deserialize(json, listClass);
    }

}
