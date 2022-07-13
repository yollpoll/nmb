package com.example.xlm.mydrawerdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.xlm.mydrawerdemo.R;


/**
 * Created by 鹏祺 on 2016/10/17.
 */

public class ChangeLineViewGroup extends RelativeLayout {
    private final static int VIEW_MARGIN = 40;
    public int row = 0;
    private int maxLine = -1;
    private OnRowChangeListener onRowChangeListener;

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public void setOnRowChangeListener(OnRowChangeListener onRowChangeListener) {
        this.onRowChangeListener = onRowChangeListener;
    }

    public ChangeLineViewGroup(Context context) {
        super(context);
    }

    public ChangeLineViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeLine);
        maxLine = a.getInteger(R.styleable.ChangeLine_max_line, -1);
        a.recycle();
    }

    public ChangeLineViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childWidth = 0;
        int childHeight = 0;
        int row = 0;
        int childCount = getChildCount();
        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);
            // measure
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            if (index == 0) {
                childHeight = child.getMeasuredHeight() + VIEW_MARGIN;
            }
            childWidth += measureWidth(child.getMeasuredWidth()) + VIEW_MARGIN;
            if (childWidth + VIEW_MARGIN > measureWidth(widthMeasureSpec)) {
                row++;
                if (maxLine != -1 && row == maxLine)
                    break;
                childHeight += child.getMeasuredHeight() + VIEW_MARGIN;
                childWidth = measureWidth(child.getMeasuredWidth()) + VIEW_MARGIN;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        setMeasuredDimension(width, childHeight + VIEW_MARGIN);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        invalidate();
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        // Default size if no limits are specified.  
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            // Calculate the ideal size of your  
            // control within this maximum size.  
            // If your control fills the available  
            // space return the outer bound.  
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            // If your control can fit within these bounds return that value.  
            result = specSize;
        }
        return specSize;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        // Default size if no limits are specified.  
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            // Calculate the ideal size of your control  
            // within this maximum size.  
            // If your control fills the available space  
            // return the outer bound.  
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            // If your control can fit within these bounds return that value.  
            result = specSize;
        }
        return specSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
//        int margin = ScreenUtils.calculateDpToPx(5, getContext());
        int margin = VIEW_MARGIN;

        int row = 0;// which row lay you view relative to parent
        int lengthX = 0; // right position of child relative to parent
        int lengthY = 0;// bottom position of child relative to parent
        for (int i = 0; i < count; i++) {

            final View child = this.getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            lengthX += width + margin;
            //if it can't drawing on a same line , skip to next line
            if (lengthX > r) {
                lengthX = width + margin;
                row++;
                if (maxLine != -1 && row == maxLine) {
                    return;
                }
                this.row = row;
            }
            lengthY = (row + 1) * (height + margin);
            if (null != onRowChangeListener) {
                onRowChangeListener.onChange(row);
            }
            child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
        }
    }

    public int getRows() {
        return this.row;
    }

    public static interface OnRowChangeListener {
        public void onChange(int row);
    }

}
