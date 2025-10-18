package jme.media.player;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author capdevon
 */
public interface VideoCodec {

    void read(InputStream streamIn, List<byte[]> ret);

}