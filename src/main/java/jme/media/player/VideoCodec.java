package jme.media.player;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author capdevon
 */
public interface VideoCodec {

    public void read(InputStream streamIn, List<byte[]> ret);

}