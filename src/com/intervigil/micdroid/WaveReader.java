package com.intervigil.micdroid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WaveReader {
	private static final int STREAM_BUFFER_SIZE = 4096;
	
	private File input;
	private BufferedInputStream inputStream;
	
	private int mSampleRate;
	private int mChannels;
	private int mSampleBits;
	
	public WaveReader(String path, String name) {
		input = new File(path + File.separator + name);
	}
	
	public void OpenWave() throws FileNotFoundException, IOException {
		FileInputStream fileStream = new FileInputStream(input);
		inputStream = new BufferedInputStream(fileStream, STREAM_BUFFER_SIZE);
		
		byte[] header = new byte[44];
		inputStream.read(header, 0, 44);
		if (header[0] != 'R' ||
            header[1] != 'I' ||
            header[2] != 'F' ||
            header[3] != 'F' ||
            header[8] != 'W' ||
            header[9] != 'A' ||
            header[10] != 'V' ||
            header[11] != 'E') {
            throw new IOException("Not a WAV file");
        }

		mChannels =
			((0xff & header[23]) << 8) |
			((0xff & header[22]));
		mSampleRate =
			((0xff & header[27]) << 24) |
			((0xff & header[26]) << 16) |
			((0xff & header[25]) << 8) |
			((0xff & header[24]));
		mSampleBits = 
			((0xff & header[34]) << 8) |
			((0xff & header[35]));
	}
	
	public int GetSampleRate() {
		return mSampleRate;
	}
	
	public int GetChannels() {
		return mChannels;
	}
	
	public int GetPcmFormat() {
		return mSampleBits;
	}
	
	public int ReadFloat(float[] outBuf, int numSamples) throws IOException {
		byte[] buf = new byte[numSamples*2];
		int bytesRead = inputStream.read(buf);
		
		int outIndex = 0;
		for (int i = 0; i < bytesRead; i+=2) {
			outBuf[outIndex] = (float) ((((0xff & buf[i]) << 8) | (0xff & buf[i+1])) * 1.0f/32768.0f);
			outIndex++;
		}
		
		return outIndex;
	}
	
	public int ReadShort(short[] outBuf, int numSamples) throws IOException {
		byte[] buf = new byte[numSamples*2];
		int bytesRead = inputStream.read(buf);
		
		int outIndex = 0;
		for (int i = 0; i < bytesRead; i+=2) {
			outBuf[outIndex] = (short) (((0xff & buf[i]) << 8) | (0xff & buf[i+1]));
			outIndex++;
		}
		
		return outIndex;
	}
	
	public void CloseWaveFile() throws IOException {
		inputStream.close();
	}
}
