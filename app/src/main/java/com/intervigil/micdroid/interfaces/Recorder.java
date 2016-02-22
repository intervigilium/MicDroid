package com.intervigil.micdroid.interfaces;

public interface Recorder {

    void start();

    void stop();

    void cleanup();

    boolean isRunning();
}
