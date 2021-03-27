package com.ramadan.notify.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class LinedTextView extends androidx.appcompat.widget.AppCompatTextView {
    private static final Paint linePaint;

    static {
        linePaint = new Paint();
        linePaint.setColor(0x80000000);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    public LinedTextView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect bounds = new Rect();
        int firstLineY = getLineBounds(0, bounds);
        int lineHeight = getLineHeight();
        int totalLines = Math.max(getLineCount(), getHeight() / lineHeight);

        for (int i = 0; i < totalLines; i++) {
            int lineY = firstLineY + i * lineHeight;
            canvas.drawLine(bounds.left, lineY, bounds.right, lineY, linePaint);
        }

        super.onDraw(canvas);
    }


}
