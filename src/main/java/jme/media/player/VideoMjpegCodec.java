package jme.media.player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 */
public class VideoMjpegCodec implements VideoCodec {
    
    // start of image
    public static final byte SOI = (byte) 0xD8;
    // end of image
    public static final byte EOI = (byte) 0xD9;

    /**
     * 
     * @param streamIn
     * @param ret 
     */
    @Override
    public void read(InputStream streamIn, List<byte[]> ret) {
        // read the whole movie in at once to make it faster
        try {
            byte[] b = convertToByteArray(streamIn);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));

            while (findMarker(in, SOI) && in.available() > 0) {
                byte data[] = readJpegData(in, EOI);
                ret.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a single frame at a time
     * 
     * @param in
     * @return 
     */
    public byte[] readFrame(DataInputStream in) {
        try {
            if (findMarker(in, SOI) && in.available() > 0) {
                return readJpegData(in, EOI);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param streamIn
     * @return
     * @throws IOException 
     */
    public byte[] convertToByteArray(InputStream streamIn) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream(1024);
        byte[] data = new byte[1024];
        int length;
        while ((length = streamIn.read(data)) != -1) {
            temp.write(data, 0, length);
        }
        return temp.toByteArray();
    }

    /**
     * 
     * @param in
     * @param marker
     * @return
     * @throws IOException 
     */
    private boolean findMarker(DataInputStream in, byte marker) throws IOException {
        boolean foundFF = false;

        while (in.available() > 0) {
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
        }
        return foundFF;
    }

    /**
     * 
     * @param in
     * @param marker
     * @return
     * @throws IOException 
     */
    private byte[] readJpegData(DataInputStream in, byte marker) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

        // add the SOI marker back into it
        bout.write(0xFF);
        bout.write(SOI);

        boolean foundFF = false;

        while (in.available() > 0) {
            byte d = in.readByte();
            if (foundFF) {
                if (d == marker) {
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
        }
        return bout.toByteArray();
    }
}
