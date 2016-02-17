package net.sourceforge.autotalent;

public class Autotalent {
    private static final String AUTOTALENT_LIB = "autotalent";

    static {
        System.loadLibrary(AUTOTALENT_LIB);
    }

    public static native boolean getLiveCorrectionEnabled();

    public static native void instantiateAutotalent(int sampleRate);

    public static native void setConcertA(float concertA);

    public static native void setKey(char key);

    public static native void setFixedPitch(float pitch);

    public static native void setFixedPull(float pull);

    public static native void setCorrectionStrength(float strength);

    public static native void setCorrectionSmoothness(float smooth);

    public static native void setPitchShift(float shift);

    public static native void setScaleRotate(int rotate);

    public static native void setLfoDepth(float depth);

    public static native void setLfoRate(float rate);

    public static native void setLfoShape(float shape);

    public static native void setLfoSymmetric(float symmetric);

    public static native void setLfoQuantization(int quantization);

    public static native void setFormantCorrection(int correction);

    public static native void setFormantWarp(float warp);

    public static native void setMix(float mix);

    public static native void processSamples(short[] samples, int numSamples);

    public static native void processSamples(short[] samples, short[] instrumental, int numSamples);

    public static native void destroyAutotalent();
}