package com.radicalninja.pimidithing.util;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    /**
     * Retrieve a boolean value from a JsonElement. If a valid boolean value cannot be located,
     * false will be returned.
     *
     * Equivalent to getBoolean(json, key, false)
     * @param json the parent JsonElement of the targeted boolean value.
     * @param key the key at which the boolean JSON value is stored.
     * @return the boolean value of the JSON key. Returns false as a default value in the cases of...
     *      * if `json` is null.
     *      * if `json` is not a valid JsonObject.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a boolean value.
     */
    public static boolean getBoolean(final JsonElement json, final String key) {
        return getBoolean(json, key, false);
    }

    /**
     * Retrieve a boolean value from a JsonElement. If a valid boolean value does not exist,
     * the provided default value will be returned.
     * @param json the parent JsonElement of the targeted boolean value.
     * @param key the key at which the boolean JSON value is stored.
     * @param defaultValue the value to be returned if a value cannot be located.
     * @return the boolean value of the JSON key. Returns `defaultValue` in the cases of...
     *      * if `json` is null.
     *      * if `json` is not a valid JsonObject.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a boolean value.
     */
    public static boolean getBoolean(final JsonElement json,
                                     final String key,
                                     final boolean defaultValue) {

        final JsonObject jsonObject = (null != json && json.isJsonObject())
                ? json.getAsJsonObject()
                : null;
        if (null != jsonObject && !TextUtils.isEmpty(key)) {
            final JsonElement targetJson = jsonObject.get(key);
            if (null != targetJson && targetJson.isJsonPrimitive()) {
                final JsonPrimitive targetJsonPrimitive = targetJson.getAsJsonPrimitive();
                if (targetJsonPrimitive.isBoolean()) {
                    return targetJsonPrimitive.getAsBoolean();
                }
            }
        }
        return defaultValue;
    }

    /**
     * Retrieve a String value from a JsonElement. If a valid String value does not exist,
     * null will be returned.
     *
     * Equivalent to getString(json, key, null);
     * @param json the parent JsonElement of the targeted String value.
     * @param key the key at which the String JSON value is stored.
     * @return the String value of the JSON key. Returns false as a default value in the cases of...
     *      * if `json` is null.
     *      * if `json` is not a valid JsonObject.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a String value.
     */
    public static String getString(final JsonElement json, final String key) {
        return getString(json, key, null);
    }

    /**
     * Retrieve a String value from a JsonElement. If a valid String value does not exist,
     * null will be returned.
     *
     * Equivalent to getString(json, key, null);
     * @param json the parent JsonElement of the targeted String value.
     * @param key the key at which the String JSON value is stored.
     * @param defaultValue the value to be returned if a value cannot be located.
     * @return the String value of the JSON key. Returns false as a default value in the cases of...
     *      * if `json` is null.
     *      * if `json` is not a valid JsonObject.
     *      * if `key` is null or empty.
     *      * if `json`.`key` does not exist.
     *      * if `json`.`key` is not a String value.
     */
    public static String getString(final JsonElement json,
                                   final String key,
                                   final String defaultValue) {

        final JsonObject jsonObject = (null != json && json.isJsonObject())
                ? json.getAsJsonObject()
                : null;
        if (null != jsonObject && !TextUtils.isEmpty(key)) {
            final JsonElement targetJson = jsonObject.get(key);
            if (null != targetJson && targetJson.isJsonPrimitive()) {
                final JsonPrimitive targetJsonPrimitive = targetJson.getAsJsonPrimitive();
                if (targetJsonPrimitive.isString()) {
                    return targetJsonPrimitive.getAsString();
                }
            }
        }
        return defaultValue;
    }

    /**
     *
     * @param json
     * @param key
     * @return
     */
    public static JsonObject getObject(final JsonObject json,
                                       final String key) {

        return getObject(json, key, null);
    }

    /**
     *
     * @param json
     * @param key
     * @param defaultValue
     * @return
     */
    public static JsonObject getObject(final JsonObject json,
                                       final String key,
                                       final JsonObject defaultValue) {

        JsonObject value = defaultValue;
        if (null != json) {
            final JsonElement targetJson = json.get(key);
            if (null != targetJson && targetJson.isJsonObject()) {
                value = targetJson.getAsJsonObject();
            }
        }
        return value;
    }

    /**
     *
     * @param json
     * @param tClass
     * @param context
     * @param <T>
     * @return
     */
    public static <T> List<T> getAsList(final JsonElement json,
                                        final Class<T> tClass,
                                        final JsonDeserializationContext context) {

        if (null == json || !json.isJsonArray()) {
            return new ArrayList<>();
        } else {
            final Class listClass = new TypeToken<List<T>>(){}.getRawType();
            return context.deserialize(json, listClass);
        }
    }

    /**
     *
     * @param json
     * @return
     */
    public static Map<String, JsonObject> getAllObjects(final JsonElement json) {
        final JsonObject jsonObject = (null != json && json.isJsonObject())
                ? json.getAsJsonObject()
                : null;
        if (null == jsonObject) {
            return new HashMap<>(0);
        }
        final Map<String, JsonObject> result = new HashMap<>(jsonObject.size());
        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                result.put(entry.getKey(), entry.getValue().getAsJsonObject());
            } else {
                // TODO: should we error here? or populate a null value to this key?
            }
        }
        return result;
    }

}
