package com.intervigil.micdroid;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class Timer {
    private static final int DEFAULT_TIMER_COUNT = 1000;
    private static final int TIMER_START = 8;
    private static final int TIMER_STOP = 9;
    private static final int TIMER_RESET = 10;
    private static final int TIMER_COUNT_UP = 11;

    private TimerHandler timerHandler;
    private TextView display;
    private int timer;

    public Timer(TextView view) {
        this.display = view;
        this.timerHandler = new TimerHandler();
        this.timer = 0;
    }

    public void registerDisplay(TextView view) {
        this.display = view;
    }

    public void start() {
        Message startMsg = timerHandler.obtainMessage(TIMER_START);
        timerHandler.sendMessage(startMsg);
    }

    public void stop() {
        Message stopMsg = timerHandler.obtainMessage(TIMER_STOP);
        timerHandler.sendMessage(stopMsg);
    }

    public void reset() {
        Message resetMsg = timerHandler.obtainMessage(TIMER_RESET);
        timerHandler.sendMessage(resetMsg);
    }

    private String getTime() {
        int minutes = timer / 60;
        int seconds = timer % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private class TimerHandler extends Handler {
        // handler processes updates to the timer
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TIMER_START:
                display.setText(getTime());
                this.sleep(DEFAULT_TIMER_COUNT);
                break;
            case TIMER_STOP:
                this.removeMessages(TIMER_COUNT_UP);
                this.removeMessages(TIMER_START);
                this.removeMessages(TIMER_STOP);
                this.removeMessages(TIMER_RESET);
                break;
            case TIMER_RESET:
                timer = 0;
                display.setText(getTime());
                break;
            case TIMER_COUNT_UP:
                display.setText(getTime());
                timer++;
                this.sleep(DEFAULT_TIMER_COUNT);
                break;
            }
        }

        public void sleep(long delayMillis) {
            this.removeMessages(TIMER_COUNT_UP);
            sendMessageDelayed(this.obtainMessage(TIMER_COUNT_UP), delayMillis);
        }
    };
}
