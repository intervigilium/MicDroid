package com.intervigil.micdroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WaveWriter {
	private static final int STREAM_BUFFER_SIZE = 4096;
	
	private File output;
	private BufferedOutputStream outputStream;
	private int bytesWritten;
	
	private int sampleRate;
	private int channels;
	private int sampleBits;
	
	public WaveWriter(String path, String name, int sampleRate, int channels, int sampleBits) {
		output = new File(path + File.separator + name);
		
		this.sampleRate= sampleRate;
		this.channels = channels;
		this.sampleBits = sampleBits;
		
		bytesWritten = 0;
	}
	
	public void CreateWaveFile() throws IOException {
		if (output.exists()) {
			output.delete();
			//Log.d("WaveWriter", "deleted existing file!");
		}
		
		if (output.createNewFile()) {
			// create file, set up output stream
			FileOutputStream fileStream = new FileOutputStream(output);
			outputStream = new BufferedOutputStream(fileStream, STREAM_BUFFER_SIZE);
			// write 44 bytes of space for the header
			outputStream.write(new byte[44]);
			//Log.d("WaveWriter", "writing 44 bytes for header");
		}
	}
	
	public void Write(short[] buffer, int bufferSize) throws IOException {
		for (int i = 0; i < bufferSize; i++) {
			Write16BitsLowHigh(outputStream, buffer[i]);
			bytesWritten += 2;
		}
	}
	
	public void CloseWaveFile() throws IOException {
		// close output stream then rewind and write wave header
		outputStream.flush();
		WriteWaveHeader();
		outputStream.close();
	}
	
	private void WriteWaveHeader() throws IOException {
		int bytesPerSec = (sampleBits + 7) / 8;
		long totalDataLength = bytesWritten + 36;
		long byteRate = sampleRate * channels * bytesPerSec;
		
		byte[] header = new byte[44];
		header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLength & 0xff);
        header[5] = (byte) ((totalDataLength >> 8) & 0xff);
        header[6] = (byte) ((totalDataLength >> 16) & 0xff);
        header[7] = (byte) ((totalDataLength >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);  // sample rate, 22050 typically
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);  // byte rate = sampleRate * numChannels * bitsPerSample/8
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (bytesPerSec * channels);  // block align = bitsPerSample/8 * numChannels
        header[33] = 0;
        header[34] = (byte) (sampleBits & 0xff);  // bits per sample
        header[35] = (byte) ((sampleBits >> 8) & 0xff);
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (bytesWritten & 0xff);  // length of raw pcm data in bytes
        header[41] = (byte) ((bytesWritten >> 8) & 0xff);
        header[42] = (byte) ((bytesWritten >> 16) & 0xff);
        header[43] = (byte) ((bytesWritten >> 24) & 0xff);
        outputStream.write(header, 0, 44);
	}
	
	private void Write16BitsLowHigh(OutputStream stream, short sample) throws IOException {
		// write already writes the lower order byte of this short
		stream.write(sample);
		stream.write((sample >> 8));
	}
}
