package jme.media.player;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author capdevon
 */
public interface VideoCodec {

    /**
     * Read video data from the provided input stream and append one or more
     * frame/packet byte arrays into the supplied list.
     *
     * @param streamIn the input stream to read encoded video data from; must
     *                 not be {@code null}
     * @param ret      the list to which extracted frame/packet byte arrays
     *                 will be appended; must not be {@code null}
     */
    void read(InputStream streamIn, List<byte[]> ret);

}