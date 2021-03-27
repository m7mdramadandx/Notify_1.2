package com.ramadan.notify.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DrawView extends View {

    private final ArrayList<Path> paths = new ArrayList<>();
    private final ArrayList<Integer> colors = new ArrayList<>();
    private int currentColor = 0xFF000000;
    private final ArrayList<Integer> widths = new ArrayList<>();
    private int currentWidth = 6;
    private Paint mBitmapPaint;
    public Bitmap mBitmap;
    public Canvas mCanvas;


    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void addPath(Path path) {
        paths.add(path);
        colors.add(currentColor);
        widths.add(currentWidth);
    }

    public Path getLastPath() {
        if (paths.size() > 0) {
            return paths.get(paths.size() - 1);
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = 0;
        for (Path path : paths) {
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            Paint paint = new Paint();
            paint.setColor(colors.get(i));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setStrokeWidth(widths.get(i));
            canvas.drawPath(path, paint);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            i++;
        }
    }

    public void setCurrentColor(int color) {
        currentColor = color;
    }

    public void clear() {
        paths.clear();
        colors.clear();
        widths.clear();
        invalidate();
    }

    public void setCurrentWidth(int width) {
        currentWidth = (width + 1) * 2;
    }

}





