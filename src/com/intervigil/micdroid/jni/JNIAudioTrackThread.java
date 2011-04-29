package com.intervigil.micdroid.jni;

class JNIAudioTrackThread {

    public JNIAudioTrackThread() {
        
    }

    public int fillBuffer() {
        return 0;
    }

    public byte[] getBuffer() {
        return null;
    }

    public int openAudioTrack() {
        return 0;
    }

    public int closeAudioTrack() {
        return 0;
    }

    public int initializeThread() {
        return 0;
    }

    public int pauseAudioTrack() {
        return 0;
    }

    public int resumeAudioTrack() {
        return 0;
    }

    private native int nativeAudioTrackInitCallback();
}
