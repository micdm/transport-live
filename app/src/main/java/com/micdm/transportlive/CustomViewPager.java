package com.micdm.transportlive;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View view, boolean checkView, int dx, int x, int y) {
        return true;
    }
}
