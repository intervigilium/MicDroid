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
	private int mDataSize;
	
	public WaveReader(String path, String name) {
		input = new File(path + File.separator + name);
	}
	
	public WaveReader(File file) {
		input = file;
	}
	
	public void openWave() throws FileNotFoundException, IOException {
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
			((0xff & header[35]) << 8) |
			((0xff & header[34]));
		mDataSize = 
			((0xff & header[43]) << 24) |
			((0xff & header[42]) << 16) |
			((0xff & header[41]) << 8) |
			((0xff & header[40]));
	}
	
	public int getSampleRate() {
		// returns sample rate, typically 22050
		return mSampleRate;
	}
	
	public int getChannels() {
		// returns number of channels, mono or stereo
		return mChannels;
	}
	
	public int getPcmFormat() {
		// returns PCM format, typically 16 bit PCM
		return mSampleBits;
	}
	
	public int getLength() {
		// returns length in seconds
		return mDataSize/(mSampleRate * mChannels * ((mSampleBits + 7)/8));
	}
	
	public int readShort(short[] outBuf, int numSamples) throws IOException {
		byte[] buf = new byte[numSamples*2];
		int bytesRead = inputStream.read(buf);
		
		int outIndex = 0;
		for (int i = 0; i < bytesRead; i+=2) {
			outBuf[outIndex] = (short) ((0xff & buf[i]) | ((0xff & buf[i+1]) << 8));
			outIndex++;
		}
		
		return outIndex;
	}
	
	public void closeWaveFile() throws IOException {
		inputStream.close();
	}
}
