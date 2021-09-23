package com.nkart.neo.extra;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * TODO Created by Tanay on 29-09-2015 at 05:35 AM.
 */
public class ParallaxPagerTransformer implements ViewPager.PageTransformer {

    private int id;
    private int border;
    private float speed;

    public ParallaxPagerTransformer(int id) {
        this.id = id;
        this.speed = 0.5f;
        border = 0;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void transformPage(View view, float position) {

        View parallaxView = view.findViewById(id);

        try {
        if (parallaxView != null) {
            if (position > -1 && position < 1) {
                float width = parallaxView.getWidth();
                parallaxView.setTranslationX(-(position * width * speed));
                float sc = ((float) view.getWidth() - border) / view.getWidth();
                if (position == 0) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else {
                    view.setScaleX(sc);
                    view.setScaleY(sc);
                }
            } }
        } catch (IllegalArgumentException ignored){

        }
    }


    /**
     *  not needed now
     * @param px

    public void setBorder(int px) {
    border = px;
    }

    /**
     *  setting the parameter of scrolling dynamically
     * @param speed

    public void setSpeed(float speed) {
    this.speed = speed;
    }
     */

}
