package com.yollpoll.nmb.view.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.yollpoll.nmb.R;


/**
 * Created by 鹏祺 on 2017/6/22.
 */

public class ChangeBurshWidthView extends View {
    private Paint mPaint;

    public ChangeBurshWidthView(Context context) {
        super(context);
        init();
    }

    public ChangeBurshWidthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChangeBurshWidthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setWidth(int width){
        mPaint.setStrokeWidth(width);
        postInvalidate();
    }
    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.black));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(20);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0 + getPaddingLeft(), (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2, getMeasuredWidth() - getPaddingRight(),
                (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2, mPaint);
    }
}
