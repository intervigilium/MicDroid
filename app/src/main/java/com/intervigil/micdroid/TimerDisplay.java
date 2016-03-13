package com.intervigil.micdroid;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDisplay {
    private static final String TAG = "TimerDisplay";

    private static final int TIMER_UPDATE = 8;

    private TextView mDisplay;
    private Timer mTimer;
    private int mElapsed = 0;

    public TimerDisplay(TextView timerText) {
        mDisplay = timerText;
    }

    public void start() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer("DisplayTimer", true);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mElapsed += 1000;
                mHandler.obtainMessage(TIMER_UPDATE).sendToTarget();
            }
        }, 0, 1000);
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mHandler.obtainMessage(TIMER_UPDATE).sendToTarget();
    }

    public void reset() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mElapsed = 0;
        mHandler.obtainMessage(TIMER_UPDATE).sendToTarget();
    }

    private String getTime() {
        int min = (mElapsed / 1000) / 60;
        int sec = (mElapsed / 1000) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_UPDATE:
                    mDisplay.setText(getTime());
                    break;
            }
        }
    };
}
