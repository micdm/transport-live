package com.micdm.transportlive.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ViewPager extends android.support.v4.view.ViewPager {

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View view, boolean checkView, int dx, int x, int y) {
        return true;
    }
}
