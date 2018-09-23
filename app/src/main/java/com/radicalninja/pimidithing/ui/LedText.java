package com.radicalninja.pimidithing.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class LedText extends LedDrawable {

    private final Paint paint = new Paint();
    private final Typeface typeface;

    @ColorInt
    private int textColor = Color.WHITE;
    private String message;

    public LedText(@NonNull final Resources resources, @NonNull final Typeface typeface) {
        super(resources);
        this.typeface = typeface;
        initPaint();
    }

    public LedText(@NonNull final Resources resources,
                   @NonNull final Typeface typeface,
                   @ColorInt final int textColor) {

        super(resources);
        this.typeface = typeface;
        this.textColor = textColor;
        initPaint();
    }

    public LedText(@NonNull final Resources resources,
                   @ColorInt final int backgroundColor,
                   @NonNull final Typeface typeface) {

        super(resources, backgroundColor);
        this.typeface = typeface;
        initPaint();
    }

    public LedText(@NonNull final Resources resources,
                   @ColorInt final int backgroundColor,
                   @NonNull final Typeface typeface,
                   @ColorInt final int textColor) {

        super(resources, backgroundColor);
        this.typeface = typeface;
        this.textColor = textColor;
        initPaint();
    }

    protected void initPaint() {
        paint.setTypeface(typeface);
        paint.setColor(textColor);
        // TODO: Additional params / methods for determining typeface print style? will be hardcoded for now.
        paint.setTextSize(LedMatrix.HEIGHT);
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        // TODO: should Y be set to getHeight?
        canvas.drawText(message, 0, 0, paint);
    }

    @Override
    public int getWidth() {
        return (!TextUtils.isEmpty(message)) ? Math.round(paint.measureText(message)) : 0;
    }

    @Override
    public int getHeight() {
        return (int) paint.getTextSize();
    }
}
