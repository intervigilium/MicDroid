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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WaveWriter {
    private static final int OUTPUT_STREAM_BUFFER = 16384;

    private File mOutFile;
    private BufferedOutputStream mOutStream;

    private int mSampleRate;
    private int mChannels;
    private int mSampleBits;

    private int mBytesWritten;


    /**
     * Constructor; initializes WaveWriter with file name and path
     *
     * @param path  output file path
     * @param name  output file name
     * @param sampleRate  output sample rate
     * @param channels  number of channels
     * @param sampleBits  number of bits per sample (S8LE, S16LE)
     */
    public WaveWriter(String path, String name, int sampleRate, int channels,
            int sampleBits) {
        this.mOutFile = new File(path + File.separator + name);

        this.mSampleRate = sampleRate;
        this.mChannels = channels;
        this.mSampleBits = sampleBits;

        this.mBytesWritten = 0;
    }

    /**
     * Constructor; initializes WaveWriter with file name and path
     *
     * @param file  output file handle
     * @param sampleRate  output sample rate
     * @param channels  number of channels
     * @param sampleBits  number of bits per sample (S8LE, S16LE)
     */
    public WaveWriter(File file, int sampleRate, int channels, int sampleBits) {
        this.mOutFile = file;

        this.mSampleRate = sampleRate;
        this.mChannels = channels;
        this.mSampleBits = sampleBits;

        this.mBytesWritten = 0;
    }

    /**
     * Create output WAV file
     *
     * @return whether file creation succeeded
     *
     * @throws IOException if file I/O error occurs allocating header
     */
    public boolean createWaveFile() throws IOException {
        if (mOutFile.exists()) {
            mOutFile.delete();
        }

        if (mOutFile.createNewFile()) {
            FileOutputStream fileStream = new FileOutputStream(mOutFile);
            mOutStream = new BufferedOutputStream(fileStream, OUTPUT_STREAM_BUFFER);
            // write 44 bytes of space for the header
            mOutStream.write(new byte[44]);
            return true;
        }
        return false;
    }

    /**
     * Write audio data to output file (mono). Does
     * nothing if output file is not mono channel.
     *
     * @param src  mono audio data input buffer
     * @param offset offset into src buffer
     * @param length  buffer size in number of samples
     *
     * @throws IOException if file I/O error occurs
     */
    public void write(short[] src, int offset, int length) throws IOException {
        if (mChannels != 1) {
            return;
        }
        if (offset > length) {
            throw new IndexOutOfBoundsException(String.format("offset %d is greater than length %d", offset, length));
        }
        for (int i = offset; i < length; i++) {
            writeUnsignedShortLE(mOutStream, src[i]);
            mBytesWritten += 2;
        }
    }

    /**
     * Write audio data to output file (stereo). Does
     * nothing if output file is not stereo channel.
     *
     * @param left  left channel audio data buffer
     * @param right  right channel audio data buffer
     * @param offset  offset into left/right buffers
     * @param length  buffer size in number of samples
     *
     * @throws IOException if file I/O error occurs
     */
    public void write(short[] left, short[] right, int offset, int length) throws IOException {
        if (mChannels != 2) {
            return;
        }
        if (offset > length) {
            throw new IndexOutOfBoundsException(String.format("offset %d is greater than length %d", offset, length));
        }
        for (int i = offset; i < length; i++) {
            writeUnsignedShortLE(mOutStream, left[i]);
            writeUnsignedShortLE(mOutStream, right[i]);
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
        if (mOutStream != null) {
            this.mOutStream.flush();
            this.mOutStream.close();
        }
        writeWaveHeader();
    }

    private void writeWaveHeader() throws IOException {
        // rewind to beginning of the file
        RandomAccessFile file = new RandomAccessFile(this.mOutFile, "rw");
        file.seek(0);

        int bytesPerSec = (mSampleBits + 7) / 8;

        file.writeBytes("RIFF"); // WAV chunk header
        file.writeInt(Integer.reverseBytes(mBytesWritten + 36)); // WAV chunk size
        file.writeBytes("WAVE"); // WAV format

        file.writeBytes("fmt "); // format subchunk header
        file.writeInt(Integer.reverseBytes(16)); // format subchunk size
        file.writeShort(Short.reverseBytes((short) 1)); // audio format
        file.writeShort(Short.reverseBytes((short) mChannels)); // number of channels
        file.writeInt(Integer.reverseBytes(mSampleRate)); // sample rate
        file.writeInt(Integer.reverseBytes(mSampleRate * mChannels * bytesPerSec)); // byte rate
        file.writeShort(Short.reverseBytes((short) (mChannels * bytesPerSec))); // block align
        file.writeShort(Short.reverseBytes((short) mSampleBits)); // bits per sample

        file.writeBytes("data"); // data subchunk header
        file.writeInt(Integer.reverseBytes(mBytesWritten)); // data subchunk size

        file.close();
        file = null;
    }

    private static void writeUnsignedShortLE(BufferedOutputStream stream, short sample)
            throws IOException {
        // write already writes the lower order byte of this short
        stream.write(sample);
        stream.write((sample >> 8));
    }
}
