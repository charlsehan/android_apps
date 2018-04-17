package com.cloudminds.arcviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class ArcView extends View {

    public ArcView(Context context) {
        super(context);
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.parseColor("#EAEAEA"));
        canvas.drawArc(0, 0, width, height, 0, 360, true, paint);

        paint.setColor(Color.parseColor("#6DCCA0"));
        canvas.drawArc(0, 0, width, height, -90, 28.8f, true, paint);

        paint.setColor(Color.parseColor("#F7F7F7"));
        int radius = width / 2 - 50;
        canvas.drawCircle(width / 2, height / 2, radius, paint);

    }
}
