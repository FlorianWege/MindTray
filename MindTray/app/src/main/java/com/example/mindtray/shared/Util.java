package com.example.mindtray.shared;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mindtray.R;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
    utility stuff (static)
 */

public class Util {
    private static Map<View, Integer> _lockC = new HashMap<>();

    public static void addEnabled(View v, boolean flag) {
        int prevVal = _lockC.containsKey(v) ? _lockC.get(v) : 0;

        _lockC.put(v, flag ? prevVal - 1 : prevVal + 1);

        if (_lockC.get(v) > 0) {
            v.setEnabled(false);
        } else {
            _lockC.remove(v);
            v.setEnabled(true);
        }
    }

    public static void lockViews(View v, boolean flag) {
        if (v instanceof ViewGroup) {
            ViewGroup vGroup = (ViewGroup) v;

            for (int i = 0; i < vGroup.getChildCount(); i++) {
                lockViews(vGroup.getChildAt(i), flag);
            }
        }

        if (flag) {
            addEnabled(v, true);
        } else {
            addEnabled(v, false);
        }
    }

    public static void lockViews(Activity activity, boolean flag) {
        try {
            View decorView = activity.getWindow().getDecorView();

            lockViews(decorView, flag);
        } catch (Exception e) {
            Log.e(new Util().getClass().getSimpleName(), e.toString(), e);
        }
    }

    public static void printToast(final Context context, final String s, final int toastDur) {
        Log.e(new Util().getClass().getSimpleName(), s);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, toastDur).show();
            }
        });
    }

    private static Util instance = new Util();

    public static void printException(Context context, Exception e) {
        try {
            printToast(context, (e == null) ? "null" : e.toString(), Toast.LENGTH_SHORT);
        } catch (Exception e2) {
            Log.e(instance.getClass().getSimpleName(), (e2 == null) ? "null" : e2.toString());
        }
    }

    private static long _animateBackground_curTime = -1;

    public static void animateBackground(Context context, final ImageView backgroundOne) {
        final int screenWidth = getScreenDimensions(context).x;
        final int waveImgWidth = context.getResources().getDrawable(R.drawable.backdrop_repeated).getIntrinsicWidth();
        int animatedViewWidth = 0;

        while(animatedViewWidth<screenWidth) {
            animatedViewWidth += waveImgWidth;
        }

        animatedViewWidth += waveImgWidth;

        /*final View animatedView = findViewById(R.id.button2);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) animatedView.getLayoutParams();
        layoutParams.width = animatedViewWidth;
        animatedView.setLayoutParams(layoutParams);

        //Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.background_anim);

        Animation waveAnimation = new TranslateAnimation(0, -waveImgWidth, 0, 0);
        waveAnimation.setInterpolator(new LinearInterpolator());
        //waveAnimation.setRepeatCount(Animation.INFINITE);
        waveAnimation.setDuration(10000);
        waveAnimation.setFillAfter(true);
        //waveAnimation.setFillBefore(true);
        //waveAnimation.setFillEnabled(true);

        waveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation waveAnimation = new TranslateAnimation(waveImgWidth, -2*waveImgWidth, 0, 0);
                waveAnimation.setInterpolator(new LinearInterpolator());
                //waveAnimation.setRepeatCount(Animation.INFINITE);
                waveAnimation.setDuration(10000);
                waveAnimation.setFillAfter(true);
                //waveAnimation.setFillBefore(true);
                //waveAnimation.setFillEnabled(true);

                animatedView.startAnimation(waveAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animatedView.startAnimation(waveAnimation);*/

        //final View backgroundTwo = findViewById(R.id.bg2);
        //final View backgroundThree = findViewById(R.id.bg3);

        backgroundOne.setScaleType(ImageView.ScaleType.FIT_XY);
        backgroundOne.setScaleX(3F);

        final ValueAnimator animator = ValueAnimator.ofFloat(-1.0F, 1.0F);

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(90 * 1000L);
        animator.setCurrentPlayTime(animator.getDuration() / 2);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate (ValueAnimator animation){
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = width * progress;

                backgroundOne.setTranslationX(translationX);
                //backgroundTwo.setTranslationX(translationX - width);
                //backgroundThree.setTranslationX(translationX - 2*width);

                _animateBackground_curTime = animator.getCurrentPlayTime();
            }
        });

        if (_animateBackground_curTime != -1) {
            animator.setCurrentPlayTime(_animateBackground_curTime);
        }

        animator.start();
    }

    public static Point getScreenDimensions(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return new Point(width, height);
    }

    @Nullable
    public static Context getContext() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            Log.e("getContext", e.getMessage(), e);
        }

        return null;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

        return bos.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] bArr) {
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public static String formatDate(Calendar date) {
        String ret = String.format(Locale.US, "%s %d", date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US), date.get(Calendar.DAY_OF_MONTH));
        //String ret = "" + DateFormat.format(Calendar.getInstance().get(Calendar.YEAR) == date.get(Calendar.YEAR) ? "MMM d" : "yyyy MMM d", date);

        if (ret.endsWith("1")) {
            ret += "st";
        } else if (ret.endsWith("2")) {
            ret += "nd";
        } else if (ret.endsWith("3")) {
            ret += "rd";
        } else {
            ret += "th";
        }

        return ret;
    }
}