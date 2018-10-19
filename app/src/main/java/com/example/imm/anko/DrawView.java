package com.example.imm.anko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class DrawView extends View {

    private Path path;
    private Paint bitmapOfPaint;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;

    private float X, Y;
    private static final float touch_tolerance = 5;

    public DrawView(Context context, AttributeSet set) {
        super(context,set);
        bitmapOfPaint = new Paint(Paint.DITHER_FLAG);
        path = new Path();
    }

    private void setup() {
        int width = getMeasuredWidth();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setStrokeWidth(width/32);

        this.paint = paint;
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        setup();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, bitmapOfPaint);
        canvas.drawPath(path, paint);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        X = x;
        Y = y;
    }

    private void touchMove(float x, float y) {
        float fx = Math.abs(x - X);
        float fy = Math.abs(y - Y);
        if (fx >= touch_tolerance || fy >= touch_tolerance) {
            path.quadTo(X, Y, (x + X) / 2, (y + Y) / 2);
            X = x;
            Y = y;
        }
    }

    private void touchUp() {
        path.lineTo(X, Y);
        canvas.drawPath(path, paint);
        path.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void reset() {
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        onSizeChanged(width,height,width,height);
        invalidate();
    }
}
