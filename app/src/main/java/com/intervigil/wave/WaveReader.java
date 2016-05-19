/* WaveReader.java

   Copyright (c) 2011 Ethan Chen

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.intervigil.wave;

import com.intervigil.wave.exception.InvalidWaveException;

import java.io.IOException;
import java.io.InputStream;

public class WaveReader {
    private static final int WAV_HEADER_CHUNK_ID = 0x52494646;  // "RIFF"
    private static final int WAV_FORMAT = 0x57415645;  // "WAVE"
    private static final int WAV_FORMAT_CHUNK_ID = 0x666d7420; // "fmt "
    private static final int WAV_DATA_CHUNK_ID = 0x64617461; // "data"

    private static final int WAV_HEADER_SIZE = 44;

    private InputStream mInputStream;

    private int mSampleRate;
    private int mChannels;
    private int mSampleBits;
    private int mFileSize;
    private int mDataSize;


    /**
     * Constructor; initializes WaveReader to read from input stream
     *
     * @param stream inputStream to read from
     */
    public WaveReader(InputStream stream) {
        mInputStream = stream;
    }

    /**
     * Open WAV file for reading
     *
     * @throws InvalidWaveException if input file is not a valid WAVE file
     * @throws IOException          if I/O error occurred during file read
     */
    public void openWave() throws IOException {
        int headerId = readUnsignedInt(mInputStream);  // should be "RIFF"
        if (headerId != WAV_HEADER_CHUNK_ID) {
            throw new InvalidWaveException("Invalid WAVE header chunk ID: " + headerId);
        }
        mFileSize = readUnsignedIntLE(mInputStream);  // length of header
        int format = readUnsignedInt(mInputStream);  // should be "WAVE"
        if (format != WAV_FORMAT) {
            throw new InvalidWaveException("Invalid WAVE format");
        }

        int formatId = readUnsignedInt(mInputStream);  // should be "fmt "
        if (formatId != WAV_FORMAT_CHUNK_ID) {
            throw new InvalidWaveException("Invalid WAVE format chunk ID");
        }
        int formatSize = readUnsignedIntLE(mInputStream);
        if (formatSize != 16) {

        }
        int audioFormat = readUnsignedShortLE(mInputStream);
        if (audioFormat != 1) {
            throw new InvalidWaveException("Not PCM WAVE format");
        }
        mChannels = readUnsignedShortLE(mInputStream);
        mSampleRate = readUnsignedIntLE(mInputStream);
        int byteRate = readUnsignedIntLE(mInputStream);
        int blockAlign = readUnsignedShortLE(mInputStream);
        mSampleBits = readUnsignedShortLE(mInputStream);

        int dataId = readUnsignedInt(mInputStream);
        if (dataId != WAV_DATA_CHUNK_ID) {
            throw new InvalidWaveException("Invalid WAVE data chunk ID");
        }
        mDataSize = readUnsignedIntLE(mInputStream);
    }

    /**
     * Get sample rate
     *
     * @return input file's sample rate
     */
    public int getSampleRate() {
        return mSampleRate;
    }

    /**
     * Get number of channels
     *     * @return whether file creation succeeded
     * @return number of channels in input file
     */
    public int getChannels() {
        return mChannels;
    }

    /**
     * Get PCM format, S16LE or S8LE
     *
     * @return number of bits per sample
     */
    public int getPcmFormat() {
        return mSampleBits;
    }

    /**
     * Get input file's size
     * Basically data size with headers included
     *
     * @return audio data size in bytes
     */
    public int getSize() {
        return mDataSize + WAV_HEADER_SIZE;
    }

    /**
     * Get input file length
     *
     * @return length of file in seconds
     */
    public int getLength() {
        if (mSampleRate == 0 || mChannels == 0 || (mSampleBits + 7) / 8 == 0) {
            return 0;
        } else {
            return mDataSize / (mSampleRate * mChannels * ((mSampleBits + 7) / 8));
        }
    }

    /**
     * Read audio data from input file (mono)
     *
     * @param dst        mono audio data output buffer
     * @param numSamples number of samples to read
     * @return number of samples read
     * @throws IOException if file I/O error occurs
     */
    public int read(short[] dst, int numSamples) throws IOException {
        if (mChannels != 1) {
            return -1;
        }

        byte[] buf = new byte[numSamples * 2];
        int index = 0;
        int bytesRead = mInputStream.read(buf, 0, numSamples * 2);

        for (int i = 0; i < bytesRead; i += 2) {
            dst[index] = byteToShortLE(buf[i], buf[i + 1]);
            index++;
        }

        return index;
    }

    /**
     * Read audio data from input file (stereo)
     *
     * @param left       left channel audio output buffer
     * @param right      right channel audio output buffer
     * @param numSamples number of samples to read
     * @return number of samples read
     * @throws IOException if file I/O error occurs
     */
    public int read(short[] left, short[] right, int numSamples) throws IOException {
        if (mChannels != 2) {
            return -1;
        }
        byte[] buf = new byte[numSamples * 4];
        int index = 0;
        int bytesRead = mInputStream.read(buf, 0, numSamples * 4);

        for (int i = 0; i < bytesRead; i += 2) {
            short val = byteToShortLE(buf[0], buf[i + 1]);
            if (i % 4 == 0) {
                left[index] = val;
            } else {
                right[index] = val;
                index++;
            }
        }

        return index;
    }

    /**
     * Close WAV file. WaveReader object cannot be used again following this call.
     *
     * @throws IOException if I/O error occurred closing filestream
     */
    public void closeWaveFile() throws IOException {
        if (mInputStream != null) {
            mInputStream.close();
        }
    }

    private static short byteToShortLE(byte b1, byte b2) {
        return (short) (b1 & 0xFF | ((b2 & 0xFF) << 8));
    }

    private static int readUnsignedInt(InputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[4];
        ret = in.read(buf);
        if (ret == -1) {
            return -1;
        } else {
            return ((buf[0] & 0xFF) << 24)
                    | ((buf[1] & 0xFF) << 16)
                    | ((buf[2] & 0xFF) << 8)
                    | (buf[3] & 0xFF);
        }
    }

    private static int readUnsignedIntLE(InputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[4];
        ret = in.read(buf);
        if (ret == -1) {
            return -1;
        } else {
            return buf[0] & 0xFF
                    | ((buf[1] & 0xFF) << 8)
                    | ((buf[2] & 0xFF) << 16)
                    | ((buf[3] & 0xFF) << 24);
        }
    }

    private static short readUnsignedShortLE(InputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[2];
        ret = in.read(buf, 0, 2);
        if (ret == -1) {
            return -1;
        } else {
            return byteToShortLE(buf[0], buf[1]);
        }
    }
}
