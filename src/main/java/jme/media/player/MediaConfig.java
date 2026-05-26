package jme.media.player;

import com.jme3.math.ColorRGBA;

/**
 * Configuration settings for the media player.
 *
 * @author capdevon
 */
public class MediaConfig {

    /** Playback mode (e.g., play once or loop). */
    public PlaybackMode playBackMode = PlaybackMode.ONCE;

    /** Original movie width (used only if {@code keepAspect} is true). */
    public int movieWidth = 1;

    /** Original movie height (used only if {@code keepAspect} is true). */
    public int movieHeight = 1;

    /** Whether to preserve the video aspect ratio or stretch it to the screen. */
    public boolean keepAspect = false;

    /** Unique name for the screen geometry. */
    public String screenName = "VideoQuad";

    /** Asset path for the idle state texture. If {@code null}, {@code screenColor} is used. */
    public String idleImagePath;

    /** Asset path for the loading state texture. If {@code null}, {@code screenColor} is used. */
    public String loadingImagePath;

    /** Asset path for the paused state texture. If {@code null}, {@code screenColor} is used. */
    public String pausedImagePath;

    /** Opacity of the screen, from 0.0 (transparent) to 1.0 (opaque). */
    public float alpha = 1f;

    /** Fallback color used when state textures are {@code null}. */
    public ColorRGBA screenColor = ColorRGBA.Green.clone();

    /** Asset path of the video file to play. Must not be {@code null}. */
    public String videoFilePath;

    /** Asset path of the audio file. Use {@code null} for no audio. */
    public String audioFilePath;

    /** Target video frame rate (typically 25 or 30 FPS). */
    public int framesPerSec = 30;
}