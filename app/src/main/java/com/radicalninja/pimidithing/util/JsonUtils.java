package com.radicalninja.pimidithing.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonUtils {

    public static <T> List<T> getAsList(
            final JsonElement json, final Class<T> tClass, final JsonDeserializationContext context) {
        final Class listClass = new TypeToken<List<T>>(){}.getClass();
        return context.deserialize(json, listClass);
    }

}
