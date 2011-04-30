package com.intervigil.micdroiddonate.interfaces;


public interface Recorder {

    public void start();
    
    public void stop();
    
    public void cleanup();
    
    public boolean isRunning(); 
}
