package com.radicalninja.pimidithing.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

public class LedIcon extends LedDrawable {

    // For best results, icon drawables should be 64x64 with each 8x8 range accounting for each LED in the matrix.
    //      Use a non-black color to allow for a default icon color.

    @NonNull
    public static Bitmap createIconBitmap(@NonNull final Resources resources,
                                          @DrawableRes final int iconDrawable,
                                          @ColorInt final int iconColor) {

        // TODO: Should add exception handling here and throws in the class methods. null checks, missing resources, etc.
        final Layer layer = new Layer(iconDrawable, iconColor);
        return new LedIcon(resources, layer).createBitmap();
    }

    @NonNull
    public static Bitmap createIconBitmap(@NonNull final Resources resources,
                                          @DrawableRes final int iconDrawable,
                                          @ColorInt final int iconColor,
                                          @ColorInt final int backgroundColor) {

        // TODO: Should add exception handling here and throws in the class methods. null checks, missing resources, etc.
        final Layer layer = new Layer(iconDrawable, iconColor);
        return new LedIcon(resources, backgroundColor, layer).createBitmap();
    }

    private final List<Layer> layers = new ArrayList<>();

    public LedIcon(@NonNull final Resources resources) {
        super(resources);
    }

    public LedIcon(@NonNull final Resources resources, @ColorInt final int backgroundColor) {
        super(resources, backgroundColor);
    }

    public LedIcon(@NonNull final Resources resources, @NonNull final Layer initialLayer) {
        super(resources);
        layers.add(initialLayer);
    }

    public LedIcon(@NonNull final Resources resources,
                   @ColorInt final int backgroundColor,
                   @NonNull final Layer initialLayer) {

        super(resources, backgroundColor);
        layers.add(initialLayer);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        final Resources resources = getResources();
        for (final Layer layer : layers) {
            layer.draw(canvas, resources);
        }
    }

    @Override
    public int getWidth() {
        return LedMatrix.WIDTH;
    }

    @Override
    public int getHeight() {
        return LedMatrix.HEIGHT;
    }

    public void addLayer(@NonNull final Layer layer) {
        layers.add(layer);
    }

    public void addLayerToBack(@NonNull final Layer layer) {
        layers.add(0, layer);
    }

    public void removeLayer(@NonNull final Layer layer) {
        layers.remove(layer);
    }

    public List<Layer> getLayers() {
        return new ArrayList<>(layers);
    }

    public static class Layer {

        @DrawableRes private int iconDrawable = -1;
        @ColorInt private int iconColor = -1;

        public Layer(@DrawableRes final int iconDrawable) {
            this.iconDrawable = iconDrawable;
        }

        public Layer(@DrawableRes final int iconDrawable, @ColorInt final int iconColor) {
            this.iconDrawable = iconDrawable;
            this.iconColor = iconColor;
        }

        @NonNull
        public Layer setIcon(@DrawableRes final int iconDrawable) {
            this.iconDrawable = iconDrawable;
            return this;
        }

        @NonNull
        public Layer setIconColor(@ColorInt int iconColor) {
            this.iconColor = iconColor;
            return this;
        }

        void draw(@NonNull final Canvas canvas, @NonNull final Resources resources) {
            if (iconDrawable != -1) {
                final Bitmap icon = BitmapFactory.decodeResource(resources, iconDrawable);
                final Paint paint = new Paint();
                if (iconColor != -1) {
                    paint.setColorFilter(
                            new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
                }
                final Rect srcRect = new Rect(0, 0, icon.getWidth(), icon.getHeight());
                final Rect dstRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.drawBitmap(icon, srcRect, dstRect, paint);
                icon.recycle();
            }
        }

    }

}
