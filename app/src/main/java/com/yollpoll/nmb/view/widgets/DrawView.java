package com.yollpoll.nmb.view.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yollpoll.nmb.R;


/**
 * Created by 鹏祺 on 2017/6/19.
 * 涂鸦板
 */

public class DrawView extends View {
    private int mPaintWidth = 20;
    private Bitmap imgCache, fakeCache;
    private boolean isCleanMode = false;
    private Paint mPaint, mCleanPaint;
    private Path mPath, mCleanPath;
    private int bgColor = Color.WHITE;
    private Canvas mCanvas;
    private OnCleanModeChangerListener onCleanModeChangerListener;
    private Bitmap bpBackGround;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPaintColor(int color) {
        //更新缓存
        fakeCache = Bitmap.createBitmap(imgCache);
        mPaint.setColor(color);
        mPath.reset();
    }

    public void setPaintWidth(int px) {
        //更新缓存
        fakeCache = Bitmap.createBitmap(imgCache);
        mPaint.setStrokeWidth(px);
        mPath.reset();
        this.mPaintWidth = px;
        mCleanPaint.setStrokeWidth(px);
    }


    public void setBackGround(int bgColor) {
        this.bgColor = bgColor;
        //重新设置橡皮擦颜色
        mCleanPaint.setColor(bgColor);
        //根据新颜色重新绘制一遍缓存
        mCanvas.drawPath(mCleanPath, mCleanPaint);
        mCanvas.drawColor(bgColor);
        //取出缓存
        fakeCache = Bitmap.createBitmap(imgCache);
        postInvalidate();
    }

    public void setBackGround(Bitmap bitmap) {
        bpBackGround = bitmap;
        Rect rectF = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        mCanvas.drawBitmap(bitmap, rectF, rectF, mPaint);
        //取出缓存
        fakeCache = Bitmap.createBitmap(imgCache);
        postInvalidate();
    }

    public void setCleanModeListener(OnCleanModeChangerListener onCleanModeChangerListener) {
        this.onCleanModeChangerListener = onCleanModeChangerListener;
    }

    public void setCleanMode() {
        isCleanMode = true;
        //更新缓存
        fakeCache = Bitmap.createBitmap(imgCache);
        mPath.reset();
        initCleaner();
        if (null != onCleanModeChangerListener)
            onCleanModeChangerListener.onChange(isCleanMode);
//        mCleanPath.reset();
    }

    public void cancelCleanMode() {
        isCleanMode = false;
        fakeCache = Bitmap.createBitmap(imgCache);
        if (null != onCleanModeChangerListener)
            onCleanModeChangerListener.onChange(isCleanMode);
    }

    public boolean isCleanMode() {
        return isCleanMode;
    }


    public int getPaintWidth() {
        return (int) mPaint.getStrokeWidth();
    }

    public Bitmap getBitmapCache() {
        fakeCache = Bitmap.createBitmap(imgCache);
        return fakeCache;
    }

    public void clear() {
        mCleanPath.reset();
        mPath.reset();
        mCanvas = null;
        fakeCache = null;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画背景颜色
        canvas.drawColor(bgColor);
//        //画背景图片
//        if (null != bpBackGround) {
//            Rect rectF = new Rect(0, 0, fakeCache.getWidth(), fakeCache.getHeight());
//            canvas.drawBitmap(bpBackGround, rectF, rectF, mPaint);
//        }
        if (null == mCanvas) {
            //新建一个canvas 模仿自带的画一样的图
            imgCache = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(imgCache);
            mCanvas.drawColor(bgColor);
        }
        if (null != fakeCache) {
            //每次绘制都会把缓存中的绘制一遍
            Rect rectF = new Rect(0, 0, fakeCache.getWidth(), fakeCache.getHeight());
            Paint paint = new Paint();
            //设置覆盖模式，让新画的内容覆盖在bitmap上面
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            canvas.drawBitmap(fakeCache, rectF, rectF, paint);
        }

        if (isCleanMode) {
            //清除模式
            if (null != mCanvas) {
                //自定义一个canvas抄袭绘制过程并且保存在bitmap中
                mCanvas.drawPath(mCleanPath, mCleanPaint);
            }
            canvas.drawPath(mCleanPath, mCleanPaint);
            mCleanPath.moveTo(lastX, lastY);
        } else {
            if (null != mCanvas) {
                //自定义一个canvas抄袭绘制过程并且保存在bitmap中
                mCanvas.drawPath(mPath, mPaint);
            }
            canvas.drawPath(mPath, mPaint);
            mPath.moveTo(lastX, lastY);
        }
    }


    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.black));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mPaintWidth);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPath = new Path();

        initCleaner();
    }

    private void initCleaner() {
        mCleanPath = new Path();
        mCleanPaint = new Paint();
        mCleanPaint.setColor(bgColor);
        mCleanPaint.setAntiAlias(true);
        mCleanPaint.setStrokeWidth(mPaintWidth);
        mCleanPaint.setDither(true);
        mCleanPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCleanPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private float lastX = 0, lastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //橡皮擦模式
                if (isCleanMode) {
                    if (lastY == 0 && lastX == 0) {
                        lastX = event.getX();
                        lastY = event.getY();
                        mCleanPath.moveTo(event.getX(), event.getY());
                    } else {
                        mCleanPath.lineTo(event.getX(), event.getY());
                    }
                } else {
                    //绘图模式
                    if (lastX == 0 && lastY == 0) {
                        lastX = event.getX();
                        lastY = event.getY();
                        mPath.moveTo(lastX, lastY);
                    } else {
                        mPath.lineTo(event.getX(), event.getY());
                    }
                }
                postInvalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                //橡皮擦模式
                if (isCleanMode) {
                    if (lastY == 0 && lastX == 0) {
                        lastX = event.getX();
                        lastY = event.getY();
                        mCleanPath.moveTo(lastX, lastY);
                    } else {
                        lastX = event.getX();
                        lastY = event.getY();
                        mCleanPath.lineTo(lastX, lastY);
                    }
                } else {
                    //绘图模式
                    if (lastX == 0 && lastY == 0) {
                        lastX = event.getX();
                        lastY = event.getY();
                        mPath.moveTo(lastX, lastY);
                    } else {
                        lastX = event.getX();
                        lastY = event.getY();
                        mPath.lineTo(lastX, lastY);
                    }
                }
                postInvalidate();
                return true;
            case MotionEvent.ACTION_UP:
                lastX = 0;
                lastY = 0;
                return true;
            default:
                return true;
        }
    }

    public interface OnCleanModeChangerListener {
        void onChange(boolean isCleanMode);
    }
}
