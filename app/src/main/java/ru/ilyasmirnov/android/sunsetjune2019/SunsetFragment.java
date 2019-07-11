package ru.ilyasmirnov.android.sunsetjune2019;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunView;
    private View mSkyView;

    private View mReflectionSunView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    private float mSunYStart;
    private float mSunYEnd;

    private float mSunReflectionYStart;
    private float mSunReflectionYEnd;

    private AnimatorSet mAnimatorSet;
    private AnimatorSet mReverseAnimatorSet;

    private ObjectAnimator mNightSkyAnimator;

    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mHeightReflectionAnimator;

    private int mHeightAnimatorDuration = 3000;
    private int mNightSkyAnimatorDuration = 1500;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSkyView = view.findViewById(R.id.sky);

        mReflectionSunView = view.findViewById(R.id.reflectionSun);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        mAnimatorSet = new AnimatorSet();
        mReverseAnimatorSet = new AnimatorSet();

        mSunView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSunYStart = mSunView.getTop();
                mSunYEnd = mSkyView.getHeight() - mSunView.getHeight() * 0.5f;
                mSunReflectionYStart = mReflectionSunView.getTop();
                mSunReflectionYEnd =  - mReflectionSunView.getHeight() * 0.5f;
            }
        });

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSunView.getY() == mSunYStart) {

                    if (!mReverseAnimatorSet.isRunning()) {

                        startAnimation(mBlueSkyColor, true, mSunView.getY());
                    }

                } else if (mAnimatorSet.isRunning()) {

                    if (mNightSkyAnimator.isRunning()) {

                        startReverseAnimation(getBackgroundColor(mSkyView), true, mSunView.getY());

                    } else {

                        startReverseAnimation(getBackgroundColor(mSkyView), false, mSunView.getY());
                    }

                    mAnimatorSet.cancel();

                } else if (!mReverseAnimatorSet.isRunning()) {

                    startReverseAnimation(mNightSkyColor, true, mSunView.getY());

                } else if (mReverseAnimatorSet.isRunning()) {

                    if (mHeightAnimator.isRunning()) {

                        startAnimation(getBackgroundColor(mSkyView), true, mSunView.getY());

                    } else {

                        startAnimation(getBackgroundColor(mSkyView), false, mSunView.getY());
                    }

                    mReverseAnimatorSet.cancel();
                }
            }
        });

        return view;
    }

    private void startAnimation(int backgroundColor, boolean isStartHeightAnimator, float sunY) {

        if (isStartHeightAnimator) {

            mNightSkyAnimator = getSkyAnimator(mNightSkyAnimatorDuration, mSunsetSkyColor, mNightSkyColor);

        } else {

            mNightSkyAnimator = getSkyAnimator(mNightSkyAnimatorDuration, backgroundColor, mNightSkyColor);
        }

        mAnimatorSet = new AnimatorSet();

        if (isStartHeightAnimator) {

            int animDuration = getAnimatorDuration(sunY, mSunYEnd);

            mHeightAnimator = getHeightAnimator(mSunView, animDuration, mSunYEnd);
            mHeightReflectionAnimator = getHeightAnimator(mReflectionSunView, animDuration, mSunReflectionYEnd);

            ObjectAnimator sunsetSkyAnimator = getSkyAnimator(animDuration, backgroundColor, mSunsetSkyColor);

            int repeatCount = (animDuration / mHeightAnimatorDuration) * 31;

            ObjectAnimator shakeAnimator = getRepeatShakeAnimator(mSunView, 15, repeatCount, 50, 0);
            ObjectAnimator shakeReflectionAnimator = getRepeatShakeAnimator(mReflectionSunView, 15, repeatCount, 50, 0);

            ObjectAnimator widthAnimator = getWidthAnimator(mSunView);
            ObjectAnimator widthReflectionAnimator = getWidthAnimator(mReflectionSunView);

            mAnimatorSet
                    .play(mHeightAnimator)
                    .with(sunsetSkyAnimator)

                    .with(shakeAnimator)

                    .with(mHeightReflectionAnimator)
                    .with(shakeReflectionAnimator)

                    .before(widthAnimator)
                    .before(widthReflectionAnimator)

                    .before(mNightSkyAnimator);
        }

        if (!isStartHeightAnimator) {

            mAnimatorSet.play(mNightSkyAnimator);
        }

        mAnimatorSet.start();
    }

    private void startReverseAnimation(int backgroundColor, boolean isStartNightSkyAnimator, float sunY) {

        int animDuration = getAnimatorDuration(sunY, mSunYStart);

        mHeightAnimator = getHeightAnimator(mSunView, animDuration, mSunYStart);
        mHeightReflectionAnimator = getHeightAnimator(mReflectionSunView, animDuration, mSunReflectionYStart);

        ObjectAnimator sunsetSkyAnimator;
        if (isStartNightSkyAnimator) {

            sunsetSkyAnimator = getSkyAnimator(animDuration, mSunsetSkyColor, mBlueSkyColor);

        } else {

            sunsetSkyAnimator = getSkyAnimator(animDuration, backgroundColor, mBlueSkyColor);
        }

        int delay = (mHeightAnimatorDuration / 2) - (mHeightAnimatorDuration - animDuration);

        ObjectAnimator shakeAnimator = getRepeatShakeAnimator(mSunView, 15, 31, 50, delay);
        ObjectAnimator shakeReflectionAnimator = getRepeatShakeAnimator(mReflectionSunView, 15, 31, 50, delay);

        ObjectAnimator widthAnimator = getWidthAnimator(mSunView);
        ObjectAnimator widthReflectionAnimator = getWidthAnimator(mReflectionSunView);

        mReverseAnimatorSet = new AnimatorSet();

        if (isStartNightSkyAnimator) {

            mNightSkyAnimator = getSkyAnimator(mNightSkyAnimatorDuration, backgroundColor, mSunsetSkyColor);

            mReverseAnimatorSet
                    .play(mHeightAnimator)
                    .with(sunsetSkyAnimator)

                    .with(shakeAnimator)

                    .with(mHeightReflectionAnimator)
                    .with(shakeReflectionAnimator)

                    .after(mNightSkyAnimator)

                    .before(widthAnimator)
                    .before(widthReflectionAnimator);
        }

        if (!isStartNightSkyAnimator) {

            mReverseAnimatorSet
                    .play(mHeightAnimator)
                    .with(sunsetSkyAnimator)

                    .with(shakeAnimator)

                    .with(mHeightReflectionAnimator)
                    .with(shakeReflectionAnimator)

                    .before(widthAnimator)
                    .before(widthReflectionAnimator)
            ;
        }

        mReverseAnimatorSet.start();
    }

    private ObjectAnimator getHeightAnimator(View view, int animatorDuration, float yEnd) {

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(view, "y", yEnd)
                .setDuration(animatorDuration);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        return heightAnimator;
    }

    private ObjectAnimator getSkyAnimator(int animatorDuration, int backgroundColorStart, int backgroundColorEnd) {

        ObjectAnimator skyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", backgroundColorStart, backgroundColorEnd)
                .setDuration(animatorDuration);
        skyAnimator.setEvaluator(new ArgbEvaluator());

        return skyAnimator;
    }

    private ObjectAnimator getRepeatShakeAnimator(View view, float value, int repeatCount,
                                                  int duration, int delay) {

        ObjectAnimator shakeAnimator = ObjectAnimator
                .ofFloat(view, "translationX", value)
                .setDuration(duration);
        shakeAnimator.setRepeatCount(repeatCount);
        shakeAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        shakeAnimator.setStartDelay(delay);

        return shakeAnimator;
    }

    private ObjectAnimator getWidthAnimator(View view) {

        return ObjectAnimator.ofFloat(view, "x", view.getLeft());
    }

    private int getAnimatorDuration(float sunY1 , float sunY2) {

        float animatorDuration = mHeightAnimatorDuration * (Math.abs(sunY1 - sunY2) / (mSunYEnd - mSunYStart));

        return (int) animatorDuration;
    }

    private int getBackgroundColor(View view) {
        int color = Color.BLACK;
        if(view.getBackground() instanceof ColorDrawable) {
                color = ((ColorDrawable)view.getBackground()).getColor();
        }

        return color;
    }
}

