package com.general.mediaplayer.snapchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

public class BaseActivity extends AppCompatActivity {



    public static int FILTER_RESET_DELAY = 1000 * 5 * 60;
    private static Handler mFilterResetHandler = new Handler();
    private static Runnable mFilterResetRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void stopResetTimer() {
        if (mFilterResetRunnable != null) {
            mFilterResetHandler.removeCallbacks(mFilterResetRunnable);
        }
    }

    public void setupFilterResetTimer() {
        stopResetTimer();

        mFilterResetRunnable = new Runnable() {
            @Override
            public void run() {

                moveTaskToBack(true);
            }
        };

        mFilterResetHandler.postDelayed(mFilterResetRunnable, FILTER_RESET_DELAY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        setupFilterResetTimer();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFilterResetTimer();
    }

    @Override
    protected void onPause() {
        stopResetTimer();
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
