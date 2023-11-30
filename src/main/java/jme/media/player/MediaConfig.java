package jme.media.player;

import com.jme3.math.ColorRGBA;

import jme.media.player.SimpleMediaPlayer.PlaybackMode;

/**
 *
 * @author capdevon
 */
public class MediaConfig {
    
    //Playback mode. Play once or loop
    public PlaybackMode playBackMode = PlaybackMode.ONCE;
    //Original movie dimensions - relevant only for keeping aspect ratio
    public int movieWidth;
    public int movieHeight;
    public float zoomingFactor = 1f;
    //True if aspect ratio should be kept. False is the movie should be stretched to the screen
    public boolean keepAspect;
    //Unique name
    public String screenName = "Screen";
    //Image to display when player is idle. Null to use screenColor
    public String idleImagePath;
    //Image to display when player is loading. Null to use screenColor
    public String loadingImagePath;
    //Image to display when player is paused. Null to use screenColor
    public String pausedImagePath;
    //Transparency of the screen. 1 for intro, material and menu geometries. Below 1f for HUD geometries
    public float alpha = 1f;
    //Color to use if any of above pictures is not provided.
    public ColorRGBA screenColor = ColorRGBA.Black.clone();
    //Video to play. Must not be null
    public String videoFilePath;
    //Audio to play. Null if no audio
    public String audioFilePath;
    //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
    public int framesPerSec = 30;
    
}
