package com.example.krot.videoplayermanager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Krot on 1/29/18.
 */

public class CustomThumbnailImageView extends android.support.v7.widget.AppCompatImageView {

    private int width = 16;
    private int height = 9;

    public CustomThumbnailImageView(Context context) {
        super(context);
    }

    public CustomThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomThumbnailImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int customWidth = MeasureSpec.getSize(widthMeasureSpec);
        int customHeight = Math.round(((customWidth - getPaddingLeft() - getPaddingRight()) * height) / width) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(customWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(customHeight, MeasureSpec.EXACTLY));
    }
}
