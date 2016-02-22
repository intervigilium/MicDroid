/* WaveWriter.java

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WaveWriter {

    private FileOutputStream mOutputStream;

    private int mSampleRate;
    private int mChannels;
    private int mSampleBits;

    private int mBytesWritten;

    /**
     * Constructor; initializes WaveWriter with file name and path
     *
     * @param stream     output stream
     * @param sampleRate output sample rate
     * @param channels   number of channels
     * @param sampleBits number of bits per sample (S8LE, S16LE)
     */
    public WaveWriter(FileOutputStream stream, int sampleRate, int channels, int sampleBits) {
        mOutputStream = stream;
        mSampleRate = sampleRate;
        mChannels = channels;
        mSampleBits = sampleBits;
        mBytesWritten = 0;
    }

    /**
     * Create output WAV file
     *
     * @return whether file creation succeeded
     * @throws IOException if file I/O error occurs allocating header
     */
    public void createWaveFile() throws IOException {
        // write 44 bytes of space for the header
        mOutputStream.write(new byte[44]);
    }

    /**
     * Write audio data to output file (mono). Does
     * nothing if output file is not mono channel.
     *
     * @param src    mono audio data input buffer
     * @param offset offset into src buffer
     * @param length buffer size in number of samples
     * @throws IOException if file I/O error occurs
     */
    public void write(short[] src, int offset, int length) throws IOException {
        if (mChannels != 1) {
            return;
        }
        if (offset > length) {
            throw new IndexOutOfBoundsException(
                    ("offset " + offset + " is greater than length " + length));
        }
        for (int i = offset; i < length; i++) {
            writeUnsignedShortLE(mOutputStream, src[i]);
            mBytesWritten += 2;
        }
    }

    /**
     * Write audio data to output file (stereo). Does
     * nothing if output file is not stereo channel.
     *
     * @param left   left channel audio data buffer
     * @param right  right channel audio data buffer
     * @param offset offset into left/right buffers
     * @param length buffer size in number of samples
     * @throws IOException if file I/O error occurs
     */
    public void write(short[] left, short[] right, int offset, int length) throws IOException {
        if (mChannels != 2) {
            return;
        }
        if (offset > length) {
            throw new IndexOutOfBoundsException(
                    ("offset " + offset + " is greater than length " + length));
        }
        for (int i = offset; i < length; i++) {
            writeUnsignedShortLE(mOutputStream, left[i]);
            writeUnsignedShortLE(mOutputStream, right[i]);
            mBytesWritten += 4;
        }
    }

    /**
     * Close output WAV file and write WAV header. WaveWriter
     * cannot be used again following this call.
     *
     * @throws IOException if file I/O error occurs writing WAV header
     */
    public void closeWaveFile() throws IOException {
        if (mOutputStream != null) {
            writeWaveHeader();
            mOutputStream.flush();
            mOutputStream.close();
        }
    }

    private void writeWaveHeader() throws IOException {
        // rewind to beginning of the file
        mOutputStream.getChannel().position(0);

        int bytesPerSec = (mSampleBits + 7) / 8;

        mOutputStream.write("RIFF".getBytes()); // WAV chunk header
        writeUnsignedIntLE(mOutputStream, mBytesWritten + 36); // WAV chunk size
        mOutputStream.write("WAVE".getBytes()); // WAV format

        mOutputStream.write("fmt ".getBytes()); // format subchunk header
        writeUnsignedIntLE(mOutputStream, 16); // format subchunk size
        writeUnsignedShortLE(mOutputStream, (short) 1); // audio format
        writeUnsignedShortLE(mOutputStream, (short) mChannels); // number of channels
        writeUnsignedIntLE(mOutputStream, mSampleRate); // sample rate
        writeUnsignedIntLE(mOutputStream, mSampleRate * mChannels * bytesPerSec); // byte rate
        writeUnsignedShortLE(mOutputStream, (short) (mChannels * bytesPerSec)); // block align
        writeUnsignedShortLE(mOutputStream, (short) mSampleBits); // bits per sample

        mOutputStream.write("data".getBytes()); // data subchunk header
        writeUnsignedIntLE(mOutputStream, mBytesWritten); // data subchunk size
    }

    private static void writeUnsignedIntLE(OutputStream stream, int sample)
            throws IOException {
        stream.write((sample & 0x000000ff));
        stream.write((sample & 0x0000ff00) >> 8);
        stream.write((sample & 0x00ff0000) >> 16);
        stream.write((sample & 0xff000000) >> 24);
    }

    private static void writeUnsignedShortLE(OutputStream stream, short sample)
            throws IOException {
        // write already writes the lower order byte of this short
        stream.write(sample);
        stream.write((sample >> 8));
    }
}