package com.example.rainbow.lab10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by 123 on 2016/12/8.
 */

public class ArrowView extends ImageView {
    private static final float ROTATION_THRESHOLD = 1.0f;
    public ArrowView(Context context){
        super(context);
        init();
    }
    public ArrowView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }
    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }
    private float mCurRotation;
    private void init(){
        mCurRotation = 0;
    }
    public float getCurRotation(){
        return mCurRotation;
    }
    public void onUpdateRotation(float newRotation){
        if(Math.abs(newRotation-mCurRotation)>ROTATION_THRESHOLD){
            RotateAnimation animation = new RotateAnimation(mCurRotation, newRotation,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);
            animation.setFillAfter(true);
            startAnimation(animation);

            mCurRotation = newRotation;
        }

    }
}
