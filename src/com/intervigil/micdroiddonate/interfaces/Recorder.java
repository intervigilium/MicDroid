package com.intervigil.micdroiddonate.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Recorder {

    public void start() throws IllegalStateException, FileNotFoundException, IOException;
    
    public void stop();
    
    public void cleanup();
    
    public boolean isRunning(); 
}
