package com.intervigil.micdroid.jni;

class JNIAudioRecordThread {

    public JNIAudioRecordThread() {
        
    }

    public int fillBuffer() {
        return 0;
    }

    public byte[] getBuffer() {
        return null;
    }

    public int openAudioRecord() {
        return 0;
    }

    public int closeAudioRecord() {
        return 0;
    }

    public int initializeThread() {
        return 0;
    }

    private native int nativeAudioRecordInitCallback();
}
