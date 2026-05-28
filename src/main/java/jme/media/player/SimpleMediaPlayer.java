package jme.media.player;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleMediaPlayer {

    private static final Logger logger = Logger.getLogger(SimpleMediaPlayer.class.getName());

    private final AssetManager assetManager;

    private ByteBuffer buffer;
    private Texture2D videoTexture;
    private Image textureImage;
//    private final AWTLoader m_AWTLoader = new AWTLoader();
    private final List<byte[]> frames = new ArrayList<>();

    // Loading variables
    private ExecutorService executor;
    private Future<?> loadingResult;

    private PlayerState playerState = PlayerState.IDLE;
    private boolean playOnLoad;

    private int prevFrameIndex = -1;
    private long startTime = 0;
    private long pauseTime = 0;
    private long pausePeriod = 0;

    private VideoCodec m_videoCodec = new VideoMjpegCodec();
    private VideoScreenListener videoScreenListener = null;

    // Main audio player
    private AudioNode audioNode;
    private final Geometry screenGeom;
    private final Material screenMat;
    private final MediaEffectManager effectManager;

    private final float fps;
    private final String screenName;
    private String videoAssetPath;
    private String audioAssetPath;
    private final String idleImageAssetPath;
    private final String loadingImageAssetPath;
    private final String pausedImageAssetPath;
    private final ColorRGBA screenColor;

    private enum PlayerState {
        IDLE,       // Player is idle, no media loaded, idle image or screen color displayed
        LOADING,    // Media is being loaded, loading image displayed
        LOADED,     // Media loaded and ready to play, idle image or screen color displayed
        SYNCING,    // Audio is playing, waiting for it to start to sync video (calcFrame) - still idle image or screen color displayed
        PLAYING,    // Media is playing, video frames displayed
        PAUSED      // Media is paused, paused image or last frame displayed
    }

    /**
     * Constructs a SimpleMediaPlayer configured for the provided
     * display geometry and media configuration.
     *
     * @param assetManager the JME3 AssetManager (must not be {@code null})
     * @param screen the Geometry representing the screen for displaying media (must not be {@code null})
     * @param config the {@link MediaConfig} containing media configuration parameters (must not be {@code null})
     */
    protected SimpleMediaPlayer(AssetManager assetManager, Geometry screen, MediaConfig config) {
        this.assetManager = assetManager;
        this.screenGeom = screen;
        this.screenMat = screen.getMaterial();
        this.effectManager = new MediaEffectManager(screenMat);

        this.screenName = config.screenName;
        this.videoAssetPath = config.videoFilePath;
        this.audioAssetPath = config.audioFilePath;
        this.idleImageAssetPath = config.idleImagePath;
        this.loadingImageAssetPath = config.loadingImagePath;
        this.pausedImageAssetPath = config.pausedImagePath;
        this.screenColor = config.screenColor;
        this.fps = 1.0f / config.framesPerSec;

        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @return Screen name
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * @return geometry used for displaying media.
     */
    public Geometry getGeometry() {
        return screenGeom;
    }

    /**
     * @return material used for displaying media.
     */
    public Material getMaterial() {
        return screenMat;
    }

    public MediaEffectManager getEffectManager() {
        return effectManager;
    }

    /**
     * @return Is currently loading
     */
    public boolean isLoading() {
        return playerState == PlayerState.LOADING;
    }

    /**
     * @return Are frames data preloaded.
     */
    public boolean isLoaded() {
        return playerState != PlayerState.IDLE && playerState != PlayerState.LOADING;
    }

    /**
     * @return Is playback in action - playing
     */
    public boolean isPlaying() {
        return playerState == PlayerState.PLAYING;
    }

    /**
     * @return Is paused
     */
    public boolean isPaused() {
        return playerState == PlayerState.PAUSED;
    }

    /**
     * Sets volume for audio
     */
    public void setAudioVolume(float newVolume) {
        if (audioNode != null) {
            audioNode.setVolume(newVolume);
        }
    }

    /**
     * Sets new data (video and audio) for the player. Requires reloading to take
     * effect.
     */
    @Deprecated
    public void setMedia(String videoAssetPath, String audioAssetPath) {
        this.videoAssetPath = videoAssetPath;
        this.audioAssetPath = audioAssetPath;
    }

    private void prepareLoading() {

        notifyOnPreLoad();
        setStaticImage(loadingImageAssetPath);

        if (audioAssetPath != null) {
            audioNode = new AudioNode(assetManager, audioAssetPath, AudioData.DataType.Stream);
            audioNode.setPositional(false);
            audioNode.setLooping(false);
            audioNode.setVolume(1);
        }

        frames.clear();
        loadingResult = executor.submit(new LoadingTask());
        playerState = PlayerState.LOADING;
    }

    private void startMedia() {
        // If audio is present starts syncing - listening for audio to start
        if (audioNode != null) {
            playerState = PlayerState.SYNCING;
            audioNode.play();
        } else {
            startVideo();
        }
    }

    private void syncAudioAndVideo() {
        if (audioNode.getStatus() == AudioSource.Status.Playing) {
            startVideo();
        }
    }

    private void startVideo() {
        notifyOnPrePlay();
        startTime = System.currentTimeMillis();
        prevFrameIndex = -1;
        playerState = PlayerState.PLAYING;
    }

    /**
     * Stops media in any state. Also releases any memory resources.
     */
    public void stop() {
        if (playerState == PlayerState.IDLE) {
            return;
        }
        if (playerState == PlayerState.LOADING) {
            abortLoading();
            return;
        }

        shutdownMediaResources();
        setStaticImage(idleImageAssetPath);
        notifyOnEnd();
        playerState = PlayerState.IDLE;
    }

    private void shutdownMediaResources() {
        if (audioNode != null) {
            audioNode.stop();
        }

        startTime = 0;
        pausePeriod = 0;
        pauseTime = 0;
        prevFrameIndex = -1;
        frames.clear();

        textureImage = null;
        buffer = null;
        videoTexture = null;

        playOnLoad = false;

        if (loadingResult != null) {
            loadingResult.cancel(true);
        }
    }

    private void setStaticImage(String imageAssetPath) {
        if (imageAssetPath != null) {
            screenMat.setTexture("ColorMap", assetManager.loadTexture(imageAssetPath));
        } else {
            screenMat.setColor("Color", screenColor);
        }
    }

    /**
     * Initiate loading task and play afterward. Preferred way to start playback.
     */
    public void loadAndPlayMedia() {
        if (playerState != PlayerState.IDLE && playerState != PlayerState.LOADED) {
            return;
        }

        prepareLoading();
        playOnLoad = true;
    }

    /**
     * Initiate loading task without playing. Use playMedia() afterward.
     */
    public void loadMedia() {
        if (playerState != PlayerState.IDLE && playerState != PlayerState.LOADED) {
            return;
        }

        prepareLoading();
        playOnLoad = false;
    }

    /**
     * Play already loaded media. Do not use for unpausing - no effect.
     */
    public void play() {
        if (playerState != PlayerState.LOADED) {
            return;
        }
        startMedia();
    }

    /**
     * Pauses the media. Displays predefined image or last frame.
     */
    public void pause() {
        if (playerState != PlayerState.PLAYING) {
            return;
        }

        // Paused screen or last frame
        if (pausedImageAssetPath != null) {
            screenMat.setTexture("ColorMap", assetManager.loadTexture(pausedImageAssetPath));
        }

        if (audioNode != null) {
            audioNode.pause();
        }

        playerState = PlayerState.PAUSED;
        pauseTime = System.currentTimeMillis();
    }

    /**
     * Unpause paused media
     */
    public void resume() {
        if (playerState != PlayerState.PAUSED) {
            return;
        }

        playerState = PlayerState.PLAYING;
        pausePeriod = pausePeriod + System.currentTimeMillis() - pauseTime;

        // re-establish texture
        if (videoTexture != null) {
            screenMat.setTexture("ColorMap", videoTexture);
        }

        if (audioNode != null) {
            audioNode.play();
        }
    }

    /**
     * Main update method used to display images. Must be called manually from
     * parent object.
     *
     * @param tpf
     */
    public void update(float tpf) {
        // Check if loading is done and notify about it.
        // Start media if playOnLoad is true, otherwise just change state to LOADED.
        if (playerState == PlayerState.LOADING && loadingResult != null && loadingResult.isDone()) {
            notifyOnLoaded();
            if (playOnLoad) {
                startMedia();
            } else {
                playerState = PlayerState.LOADED;
            }
        }

        // If syncing, check if audio started and start video.
        // If playing, calculate current frame.
        if (playerState == PlayerState.SYNCING) {
            syncAudioAndVideo();

        } else if (playerState == PlayerState.PLAYING) {
            calcFrame();
        }
    }

    private void abortLoading() {
        // kill task
        if (loadingResult != null && !loadingResult.isDone()) {
            loadingResult.cancel(true);
        }

        frames.clear();
        playerState = PlayerState.IDLE;
    }

    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // -----------------------LOADER-----------------------
    private class LoadingTask implements Runnable {

        @Override
        public void run() {
            // Video file stream
            AssetInfo aInfo = assetManager.locateAsset(new AssetKey<>(videoAssetPath));
            try (InputStream videoStream = aInfo.openStream()) {
                // read all frames at once
                m_videoCodec.read(videoStream, frames);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

    }

    private void calcFrame() {
        long timeSinceStart = ((System.currentTimeMillis() - startTime) - pausePeriod);
        int currFrameIndex = (int) (timeSinceStart / (1000f * fps));

        if (currFrameIndex == prevFrameIndex) {
            return;
        }

        // new frame
        prevFrameIndex = currFrameIndex;
        if (currFrameIndex >= frames.size()) {
            stop();
        } else {
            decodeFrame(frames.get(currFrameIndex));
        }
    }

    // Main method to convert jpg to texture.
    private void decodeFrame(byte[] jpegBytes) {

        if (jpegBytes == null) {
            return;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(jpegBytes)) {
            BufferedImage image = ImageIO.read(bais);
//            texture.setImage(m_AWTLoader.load(image, flipY));

            int width = image.getWidth();
            int height = image.getHeight();
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

            setImage(width, height, pixels);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void setImage(int width, int height, byte[] pixels) {

        if (buffer == null) {
            buffer = BufferUtils.createByteBuffer(width * height * 3);
            textureImage = new Image(Image.Format.BGR8, width, height, buffer, ColorSpace.sRGB);
            videoTexture = new Texture2D(textureImage);
            screenMat.setTexture("ColorMap", videoTexture);
            // remove static image
            screenMat.clearParam("Color");
        }

        buffer.clear();

        // flipY is needed because of different coordinate system of jme and awt.
        int stride = width * 3;
        for (int r = height - 1; r >= 0; r--) {
            buffer.put(pixels, r * stride, stride);
        }

        buffer.flip();
        textureImage.setUpdateNeeded();
    }

    // -----------------------VideoScreenListener-----------------------

    private void notifyOnEnd() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onEnd: {0}", screenName);
            videoScreenListener.onEnd(screenName);
        }
    }

    private void notifyOnLoaded() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onLoaded: {0}", screenName);
            videoScreenListener.onLoaded(screenName);
        }
    }

    private void notifyOnPreLoad() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onPreLoad: {0}", screenName);
            videoScreenListener.onPreLoad(screenName);
        }
    }

    private void notifyOnPrePlay() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onPrePlay: {0}", screenName);
            videoScreenListener.onPrePlay(screenName);
        }
    }

    public void setVideoScreenListener(VideoScreenListener listener) {
        this.videoScreenListener = listener;
    }

    public VideoScreenListener getVideoScreenListener() {
        return this.videoScreenListener;
    }

}
