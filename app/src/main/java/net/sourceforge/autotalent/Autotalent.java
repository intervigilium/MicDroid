package net.sourceforge.autotalent;

public class Autotalent {
    private static final String AUTOTALENT_LIB = "autotalent";

    private static Autotalent sInstance = null;
    private static int sSampleRate = -1;

    public static Autotalent getInstance(int sampleRate) {
        if (sInstance == null) {
            sInstance = new Autotalent(sampleRate);
            sSampleRate = sampleRate;
        }
        if (sSampleRate != sampleRate) {
            sInstance.close();
            sInstance = new Autotalent(sampleRate);
            sSampleRate = sampleRate;
        }
        return sInstance;
    }

    private Autotalent(int sampleRate) {
        native_createAutotalent(sampleRate);
    }

    public void setConcertA(float concertA) {
        native_setConcertA(concertA);
    }

    public void setKey(char key) {
        native_setKey(key);
    }

    public void setFixedPitch(float pitch) {
        native_setFixedPitch(pitch);
    }

    public void setFixedPull(float pull) {
        native_setFixedPull(pull);
    }

    public void setStrength(float strength) {
        native_setStrength(strength);
    }

    public void setSmoothness(float smoothness) {
        native_setSmoothness(smoothness);
    }

    public void setPitchShift(float shift) {
        native_setPitchShift(shift);
    }

    public void setScaleRotate(int rotate) {
        native_setScaleRotate(rotate);
    }

    public void setLfoDepth(float depth) {
        native_setLfoDepth(depth);
    }

    public void setLfoRate(float rate) {
        native_setLfoRate(rate);
    }

    public void setLfoShape(float shape) {
        native_setLfoShape(shape);
    }

    public void setLfoSymmetric(float symmetric) {
        native_setLfoSymmetric(symmetric);
    }

    public void setLfoQuantization(int quantization) {
        native_setLfoQuantization(quantization);
    }

    public void enableFormantCorrection(boolean enabled) {
        native_enableFormantCorrection(enabled);
    }

    public void setFormantWarp(float warp) {
        native_setFormantWarp(warp);
    }

    public void setMix(float mix) {
        native_setMix(mix);
    }

    public void process(short[] samples, int numSamples) {
        native_processSamples(samples, numSamples);
    }

    public void process(short[] samples, short[] instrumental, int numSamples) {
        native_processSamples(samples, instrumental, numSamples);
    }

    public void close() {
        native_destroyAutotalent();
        sInstance = null;
    }

    static {
        System.loadLibrary(AUTOTALENT_LIB);
    }

    private static native void native_createAutotalent(int sampleRate);

    private static native void native_setConcertA(float concertA);

    private static native void native_setKey(char key);

    private static native void native_setFixedPitch(float pitch);

    private static native void native_setFixedPull(float pull);

    private static native void native_setStrength(float strength);

    private static native void native_setSmoothness(float smooth);

    private static native void native_setPitchShift(float shift);

    private static native void native_setScaleRotate(int rotate);

    private static native void native_setLfoDepth(float depth);

    private static native void native_setLfoRate(float rate);

    private static native void native_setLfoShape(float shape);

    private static native void native_setLfoSymmetric(float symmetric);

    private static native void native_setLfoQuantization(int quantization);

    private static native void native_enableFormantCorrection(boolean enabled);

    private static native void native_setFormantWarp(float warp);

    private static native void native_setMix(float mix);

    private static native void native_processSamples(short[] samples, int numSamples);

    private static native void native_processSamples(short[] samples, short[] instrumental,
                                                     int numSamples);

    private static native void native_destroyAutotalent();
}