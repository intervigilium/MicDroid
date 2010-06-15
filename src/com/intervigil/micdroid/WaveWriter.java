package com.intervigil.micdroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.util.Log;

public class WaveWriter {
	private static final int OUTPUT_STREAM_BUFFER = 4096;
	
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
	
	public boolean CreateWaveFile() throws IOException {
		if (output.exists()) {
			output.delete();
			Log.d("WaveWriter", "deleted existing file!");
		}
		
		if (output.createNewFile()) {
			// create file, set up output stream
			FileOutputStream fileStream = new FileOutputStream(output);
			outputStream = new BufferedOutputStream(fileStream);
			// write 44 bytes of space for the header
			outputStream.write(new byte[44]);
			Log.d("WaveWriter", "writing 44 bytes for header");
			return true;
		}
		return false;
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
		outputStream.close();
		WriteWaveHeader();
	}
	
	private void WriteWaveHeader() throws IOException {
		// rewind to beginning of the file
		RandomAccessFile file = new RandomAccessFile(output, "rw");
		file.seek(0);
		
		int bytesPerSec = (sampleBits + 7) / 8;
		
		file.write(new byte[] { 'R', 'I', 'F', 'F' }); // label
		Write32BitsLowHigh(file, (short)(bytesWritten + 44 - 8)); // length in bytes without header
		file.write(new byte[] { 'W', 'A', 'V', 'E', 'f', 'm', 't', ' ' }); // 2 labels?
		Write32BitsLowHigh(file, 2 + 2 + 4 + 4 + 2 + 2); // length of pcm format declaration area
		Write16BitsLowHigh(file, (short)1); // is PCM?
		Write16BitsLowHigh(file, (short)channels); // number of channels, this is mono
		Write32BitsLowHigh(file, sampleRate); // sample rate, this is 22050 Hz
		Write32BitsLowHigh(file, sampleRate * channels * bytesPerSec); // bytes per second
		Write16BitsLowHigh(file, (short)(channels * bytesPerSec)); // bytes per sample time
		Write16BitsLowHigh(file, (short)sampleBits); // bits per sample, this is 6 bit pcm
		file.write(new byte[] { 'd', 'a', 't', 'a' });
		Write32BitsLowHigh(file, bytesWritten); // length of raw pcm data in bytes
		
		file.close();
	}
	
	private void Write16BitsLowHigh(OutputStream stream, short sample) throws IOException {
		// write already writes the lower order byte of this short
		stream.write(sample);
		stream.write((sample >> 8));
	}
	
	private void Write16BitsLowHigh(RandomAccessFile file, short sample) throws IOException {
		// write already writes the lower order byte of this short
		file.write(sample);
		file.write((sample >> 8));
	}
	
	private void Write32BitsLowHigh(RandomAccessFile file, int sample) throws IOException {
		// TODO: I don't think we need to mask because java typecasts it for us?
		Write16BitsLowHigh(file, (short)(sample & 0xffff));
		Write16BitsLowHigh(file, (short)((sample >> 16) & 0xffff));
	}
}
