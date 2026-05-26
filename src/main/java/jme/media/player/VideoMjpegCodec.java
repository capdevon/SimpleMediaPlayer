package jme.media.player;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Codec implementation for parsing and decoding Motion-JPEG (M-JPEG) video streams.
 * It extracts individual JPEG frames from an encoded input stream by locating SOI and EOI markers.
 */
public class VideoMjpegCodec implements VideoCodec {

    /** Start Of Image marker (0xFFD8) */
    public static final byte SOI = (byte) 0xD8;

    /** End Of Image marker (0xFFD9) */
    public static final byte EOI = (byte) 0xD9;

    /**
     * Reads the entire input stream and extracts all Jpeg frames into the provided list.
     * Uses a buffered stream to ensure high I/O performance without loading the whole file into memory.
     *
     * @param streamIn the source video input stream
     * @param ret      the list where decoded Jpeg frames (as byte arrays) will be stored
     */
    @Override
    public void read(InputStream streamIn, List<byte[]> ret) {
        // Wrap in BufferedInputStream (16KB) to maximize I/O performance while keeping RAM footprint low
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(streamIn, 16384))) {
            while (findMarker(in, SOI)) {
                byte[] data = readJpegData(in, EOI);
                if (data != null) {
                    ret.add(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a single Jpeg frame from the stream.
     *
     * @param in the data input stream positioned at the video sequence
     * @return the Jpeg frame byte array, or {@code null} if the frame could not be read or EOF is reached
     */
    public byte[] readFrame(DataInputStream in) {
        try {
            if (findMarker(in, SOI)) {
                return readJpegData(in, EOI);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Scans the stream until it finds the specified JPEG marker preceded by an 0xFF byte.
     *
     * @param in     the stream to scan
     * @param marker the target marker byte (e.g., SOI or EOI)
     * @return {@code true} if the marker was found, {@code false} if EOF was reached
     * @throws IOException if an I/O error occurs
     */
    private boolean findMarker(DataInputStream in, byte marker) throws IOException {
        boolean foundFF = false;

        while (true) {
            try {
                byte b = in.readByte();
                if (foundFF) {
                    if (b == marker) {
                        return true;
                    } else if (b != (byte) 0xFF) {
                        foundFF = false;
                    }
                } else if (b == (byte) 0xFF) {
                    foundFF = true;
                }
            } catch (EOFException e) {
                return false; // Safely reached end of stream
            }
        }
    }

    /**
     * Reads and accumulates bytes into a byte array until the specified closing marker is encountered.
     * Automatically prepends the SOI marker to ensure the resulting byte array is a valid standalone JPEG.
     *
     * @param in     the stream to read from
     * @param marker the closing marker byte to stop at (usually EOI)
     * @return the complete JPEG frame byte array, or {@code null} if EOF is reached prematurely
     * @throws IOException if an I/O error occurs
     */
    private byte[] readJpegData(DataInputStream in, byte marker) throws IOException {
        // 1024 is a conservative initial capacity; it will auto-grow safely
        ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);

        // Prepend the SOI marker back into the stream data to fix standard compliance
        bout.write(0xFF);
        bout.write(SOI);

        boolean foundFF = false;

        while (true) {
            try {
                byte d = in.readByte();
                if (foundFF) {
                    if (d == marker) {
                        // Successfully reached EOI, write it and stop
                        bout.write(0xFF);
                        bout.write(d);
                        break;
                    } else {
                        bout.write(0xFF);
                        bout.write(d);
                        foundFF = false;
                    }
                } else if (d == (byte) 0xFF) {
                    foundFF = true;
                } else {
                    bout.write(d);
                }
            } catch (EOFException e) {
                // Stream ended before finding the end marker.
                // Return what we have if it's substantial, else null.
                return bout.size() > 2 ? bout.toByteArray() : null;
            }
        }
        return bout.toByteArray();
    }
}