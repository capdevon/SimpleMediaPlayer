/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013  Wes Monceaux http://www.monceaux.org
 * 
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package wpm.mjpeg;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * This is a small, MJPEG AVI file generator with no dependencies outside of the
 * Java core APIs.
 * 
 * @author monceaux
 */
public class MJPEGGenerator {

    /**
     * Info needed for MJPEG AVI
     *
     * - size of file minus "RIFF & 4 byte file size"
     */
    private int width = 0;
    private int height = 0;
    private double framerate = 0;
    private int numFrames = 0;
    private File aviFile = null;
    private FileOutputStream aviOutput = null;
    private FileChannel aviChannel = null;

    private long riffOffset = 0;
    private long aviMovieOffset = 0;

    private AVIIndexList indexlist = null;
    
    /**
     * Creates a new instance of MJPEGGenerator
     */
    public MJPEGGenerator(File aviFile, int width, int height, double framerate, int numFrames) throws Exception {
        this.aviFile = aviFile;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.numFrames = numFrames;
        aviOutput = new FileOutputStream(aviFile);
        aviChannel = aviOutput.getChannel();

        RIFFHeader rh = new RIFFHeader();
        aviOutput.write(rh.toBytes());
        aviOutput.write(new AVIMainHeader().toBytes());
        aviOutput.write(new AVIStreamList().toBytes());
        aviOutput.write(new AVIStreamHeader().toBytes());
        aviOutput.write(new AVIStreamFormat().toBytes());
        aviOutput.write(new AVIJunk().toBytes());
        aviMovieOffset = aviChannel.position();
        aviOutput.write(new AVIMovieList().toBytes());
        indexlist = new AVIIndexList();
    }

    public void addImage(java.awt.Image image) throws Exception {
        byte[] fcc = new byte[]{'0', '0', 'd', 'b'};
        byte[] imagedata = writeImageToBytes(image);
        int useLength = imagedata.length;
        long position = aviChannel.position();
        int extra = (useLength + (int) position) % 4;
        if (extra > 0) {
            useLength = useLength + extra;
        }

        indexlist.addAVIIndex((int) position, useLength);

        aviOutput.write(fcc);
        aviOutput.write(intBytes(swapInt(useLength)));
        aviOutput.write(imagedata);
        if (extra > 0) {
            for (int i = 0; i < extra; i++) {
                aviOutput.write(0);
            }
        }
        imagedata = null;
    }

    public void finishAVI() throws Exception {
        byte[] indexlistBytes = indexlist.toBytes();
        aviOutput.write(indexlistBytes);
        aviOutput.close();
        long size = aviFile.length();
        RandomAccessFile raf = new RandomAccessFile(aviFile, "rw");
        raf.seek(4);
        raf.write(intBytes(swapInt((int) size - 8)));
        raf.seek(aviMovieOffset + 4);
        raf.write(intBytes(swapInt((int) (size - 8 - aviMovieOffset - indexlistBytes.length))));
        raf.close();
    }
    
    private byte[] writeImageToBytes(java.awt.Image image) throws Exception {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        ImageIO.write(bi, "jpg", baos);
        baos.close();
        bi = null;
        g = null;

        return baos.toByteArray();
    }
    
    public int swapInt(int v) {
        return (v >>> 24) | (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
    }

    public short swapShort(short v) {
        return (short) ((v >>> 8) | (v << 8));
    }

    public byte[] intBytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i >>> 24);
        b[1] = (byte) ((i >>> 16) & 0x000000FF);
        b[2] = (byte) ((i >>> 8) & 0x000000FF);
        b[3] = (byte) (i & 0x000000FF);

        return b;
    }

    public byte[] shortBytes(short i) {
        byte[] b = new byte[2];
        b[0] = (byte) (i >>> 8);
        b[1] = (byte) (i & 0x000000FF);

        return b;
    }

    private class RIFFHeader {

        public byte[] fcc = new byte[]{'R', 'I', 'F', 'F'};
        public int fileSize = 0;
        public byte[] fcc2 = new byte[]{'A', 'V', 'I', ' '};
        public byte[] fcc3 = new byte[]{'L', 'I', 'S', 'T'};
        public int listSize = 200;
        public byte[] fcc4 = new byte[]{'h', 'd', 'r', 'l'};

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(fileSize)));
            baos.write(fcc2);
            baos.write(fcc3);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc4);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIMainHeader {

        /*
         * 
         * FOURCC fcc; DWORD cb; DWORD dwMicroSecPerFrame; DWORD
         * dwMaxBytesPerSec; DWORD dwPaddingGranularity; DWORD dwFlags; DWORD
         * dwTotalFrames; DWORD dwInitialFrames; DWORD dwStreams; DWORD
         * dwSuggestedBufferSize; DWORD dwWidth; DWORD dwHeight; DWORD
         * dwReserved[4];
         */
        public byte[] fcc = new byte[]{'a', 'v', 'i', 'h'};
        public int cb = 56;
        // (1/frames per sec) * 1.000.000
        public int dwMicroSecPerFrame = 0;
        public int dwMaxBytesPerSec = 10000000;
        public int dwPaddingGranularity = 0;
        public int dwFlags = 65552;
        // replace with correct value
        public int dwTotalFrames = 0;
        public int dwInitialFrames = 0;
        public int dwStreams = 1;
        public int dwSuggestedBufferSize = 0;
        // replace with correct value
        public int dwWidth = 0;
        // replace with correct value
        public int dwHeight = 0;
        public int[] dwReserved = new int[4];

        public AVIMainHeader() {
            dwMicroSecPerFrame = (int) ((1.0 / framerate) * 1000000.0);
            dwWidth = width;
            dwHeight = height;
            dwTotalFrames = numFrames;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(dwMicroSecPerFrame)));
            baos.write(intBytes(swapInt(dwMaxBytesPerSec)));
            baos.write(intBytes(swapInt(dwPaddingGranularity)));
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwTotalFrames)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwStreams)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwWidth)));
            baos.write(intBytes(swapInt(dwHeight)));
            baos.write(intBytes(swapInt(dwReserved[0])));
            baos.write(intBytes(swapInt(dwReserved[1])));
            baos.write(intBytes(swapInt(dwReserved[2])));
            baos.write(intBytes(swapInt(dwReserved[3])));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamList {

        public byte[] fcc = new byte[]{'L', 'I', 'S', 'T'};
        public int size = 124;
        public byte[] fcc2 = new byte[]{'s', 't', 'r', 'l'};

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(fcc2);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamHeader {

        /*
         * FOURCC fcc; DWORD cb; FOURCC fccType; FOURCC fccHandler; DWORD
         * dwFlags; WORD wPriority; WORD wLanguage; DWORD dwInitialFrames; DWORD
         * dwScale; DWORD dwRate; DWORD dwStart; DWORD dwLength; DWORD
         * dwSuggestedBufferSize; DWORD dwQuality; DWORD dwSampleSize; struct {
         * short int left; short int top; short int right; short int bottom; }
         * rcFrame;
         */
        public byte[] fcc = new byte[]{'s', 't', 'r', 'h'};
        public int cb = 64;
        public byte[] fccType = new byte[]{'v', 'i', 'd', 's'};
        public byte[] fccHandler = new byte[]{'M', 'J', 'P', 'G'};
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwScale = 0; // microseconds per frame
        public int dwRate = 1000000; // dwRate / dwScale = frame rate
        public int dwStart = 0;
        public int dwLength = 0; // num frames
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 0;
        public int left = 0;
        public int top = 0;
        public int right = 0;
        public int bottom = 0;

        public AVIStreamHeader() {
            dwScale = (int) ((1.0 / framerate) * 1000000.0);
            dwLength = numFrames;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(fccType);
            baos.write(fccHandler);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(shortBytes(swapShort(wPriority)));
            baos.write(shortBytes(swapShort(wLanguage)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwScale)));
            baos.write(intBytes(swapInt(dwRate)));
            baos.write(intBytes(swapInt(dwStart)));
            baos.write(intBytes(swapInt(dwLength)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwQuality)));
            baos.write(intBytes(swapInt(dwSampleSize)));
            baos.write(intBytes(swapInt(left)));
            baos.write(intBytes(swapInt(top)));
            baos.write(intBytes(swapInt(right)));
            baos.write(intBytes(swapInt(bottom)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamFormat {

        /*
         * FOURCC fcc; DWORD cb; DWORD biSize; LONG biWidth; LONG biHeight; WORD
         * biPlanes; WORD biBitCount; DWORD biCompression; DWORD biSizeImage;
         * LONG biXPelsPerMeter; LONG biYPelsPerMeter; DWORD biClrUsed; DWORD
         * biClrImportant;
         */
        public byte[] fcc = new byte[]{'s', 't', 'r', 'f'};
        public int cb = 40;
        public int biSize = 40; // same as cb
        public int biWidth = 0;
        public int biHeight = 0;
        public short biPlanes = 1;
        public short biBitCount = 24;
        public byte[] biCompression = new byte[]{'M', 'J', 'P', 'G'};
        public int biSizeImage = 0; // width x height in pixels
        public int biXPelsPerMeter = 0;
        public int biYPelsPerMeter = 0;
        public int biClrUsed = 0;
        public int biClrImportant = 0;

        public AVIStreamFormat() {
            biWidth = width;
            biHeight = height;
            biSizeImage = width * height;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(biSize)));
            baos.write(intBytes(swapInt(biWidth)));
            baos.write(intBytes(swapInt(biHeight)));
            baos.write(shortBytes(swapShort(biPlanes)));
            baos.write(shortBytes(swapShort(biBitCount)));
            baos.write(biCompression);
            baos.write(intBytes(swapInt(biSizeImage)));
            baos.write(intBytes(swapInt(biXPelsPerMeter)));
            baos.write(intBytes(swapInt(biYPelsPerMeter)));
            baos.write(intBytes(swapInt(biClrUsed)));
            baos.write(intBytes(swapInt(biClrImportant)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIMovieList {

        public byte[] fcc = new byte[]{'L', 'I', 'S', 'T'};
        public int listSize = 0;
        public byte[] fcc2 = new byte[]{'m', 'o', 'v', 'i'};

        // 00db size jpg image data ...
        public AVIMovieList() {

        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc2);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIIndexList {

        public byte[] fcc = new byte[]{'i', 'd', 'x', '1'};
        public int cb = 0;
        public List<AVIIndex> ind = new ArrayList<>();

        @SuppressWarnings("unused")
        public void addAVIIndex(AVIIndex ai) {
            ind.add(ai);
        }

        public void addAVIIndex(int dwOffset, int dwSize) {
            ind.add(new AVIIndex(dwOffset, dwSize));
        }

        public byte[] toBytes() throws Exception {
            cb = 16 * ind.size();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            for (int i = 0; i < ind.size(); i++) {
                AVIIndex in = (AVIIndex) ind.get(i);
                baos.write(in.toBytes());
            }

            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIIndex {

        public byte[] fcc = new byte[]{'0', '0', 'd', 'b'};
        public int dwFlags = 16;
        public int dwOffset = 0;
        public int dwSize = 0;

        public AVIIndex(int dwOffset, int dwSize) {
            this.dwOffset = dwOffset;
            this.dwSize = dwSize;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwOffset)));
            baos.write(intBytes(swapInt(dwSize)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIJunk {

        public byte[] fcc = new byte[]{'J', 'U', 'N', 'K'};
        public int size = 1808;
        public byte[] data = new byte[size];

        public AVIJunk() {
            Arrays.fill(data, (byte) 0);
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(data);
            baos.close();

            return baos.toByteArray();
        }
    }

}
