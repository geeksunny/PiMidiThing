package com.radicalninja.pimidithing.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public abstract class LedDrawable {

    private final Resources resources;

    @ColorInt
    private int backgroundColor = -1;

    public LedDrawable(@NonNull final Resources resources) {
        this.resources = resources;
    }

    public LedDrawable(@NonNull final Resources resources, @ColorInt final int backgroundColor) {
        this.resources = resources;
        this.backgroundColor = backgroundColor;
    }

    public abstract void draw(@NonNull final Canvas canvas);
    public abstract int getWidth();
    public abstract int getHeight();

    public Resources getResources() {
        return resources;
    }

    public void setBackgroundColor(@ColorInt final int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @NonNull
    protected Bitmap createCanvasBitmap() throws IllegalStateException {
        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) {
            final String template =
                    "LedDrawable's width or height is less than or equal to zero. | Width: %d | Height: %d";
            throw new IllegalStateException(String.format(Locale.US, template, width, height));
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true);
    }

    @NonNull
    public Bitmap createBitmap() throws IllegalStateException {
        final Bitmap result = createCanvasBitmap();
        final Canvas canvas = new Canvas(result);
        if (backgroundColor != -1) {
            canvas.drawColor(backgroundColor);
        }
        draw(canvas);
        return result;
    }

}
