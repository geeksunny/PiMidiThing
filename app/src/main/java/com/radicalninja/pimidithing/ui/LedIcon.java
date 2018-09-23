package com.radicalninja.pimidithing.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

public class LedIcon {

    public static Bitmap createIconBitmap(@NonNull final Resources resources,
                                          @DrawableRes final int iconDrawable,
                                          @ColorInt final int iconColor) {

        // TODO: Should add exception handling here and throws in the class methods. null checks, missing resources, etc.
        return new LedIcon(resources)
                .setIcon(iconDrawable)
                .setIconColor(iconColor)
                .createBitmap();
    }

    public static Bitmap createIconBitmap(@NonNull final Resources resources,
                                          @DrawableRes final int iconDrawable,
                                          @ColorInt final int iconColor,
                                          @ColorInt final int backgroundColor) {

        // TODO: Should add exception handling here and throws in the class methods. null checks, missing resources, etc.
        return new LedIcon(resources)
                .setIcon(iconDrawable)
                .setIconColor(iconColor)
                .setBackgroundColor(backgroundColor)
                .createBitmap();
    }

    private final Resources resources;

    // TODO: Move these into an IconLayer interface. Static methods create the single layer within. All layers are considered in the rendering process.
    @DrawableRes private int iconDrawable;
    @ColorInt private int iconColor;
    @ColorInt private int backgroundColor;

    public LedIcon(@NonNull final Resources resources) {
        this.resources = resources;
    }

    @NonNull
    public LedIcon setIcon(@DrawableRes final int iconDrawable) {
        this.iconDrawable = iconDrawable;
        return this;
    }

    @NonNull
    public LedIcon setIconColor(@ColorInt int iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    @NonNull
    public LedIcon setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @NonNull
    public Bitmap createBitmap() {
        // TODO: draw to canvas, return new bitmap
        return null;
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

}
