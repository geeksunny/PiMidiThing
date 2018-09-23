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

    @NonNull
    @Size(value=4)
    public Bitmap[] animateRotation() {
        return animateRotation(true);
    }

    @NonNull
    @Size(value=4)
    public Bitmap[] animateRotation(final boolean clockWise) {
        final Bitmap[] result = new Bitmap[4];
        final Bitmap bitmap = createBitmap();
        result[0] = bitmap;

        final int rotateStart, rotateEnd, rotateOffset;
        if (clockWise) {
            rotateStart = 90;
            rotateEnd = 270;
            rotateOffset = 90;
        } else {
            rotateStart = 270;
            rotateEnd = 90;
            rotateOffset = -90;
        }

        final Matrix matrix = new Matrix();
        int frame = 1;
        for (int rotate = rotateStart; rotate != (rotateEnd + rotateOffset); rotate += rotateOffset) {
            matrix.postRotate(rotate);
            // TODO: verify if filter=true would make a difference with these small 8x8 tiles
            result[frame++] = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        }

        return result;
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
