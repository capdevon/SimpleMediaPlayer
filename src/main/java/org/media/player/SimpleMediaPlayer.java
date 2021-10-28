package org.media.player;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jme3.app.Application;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.BufferUtils;

public class SimpleMediaPlayer {
    
    private static final Logger logger = Logger.getLogger(SimpleMediaPlayer.class.getName());

    //
    private final Application app;
    
    //Indicates if video is online
    private boolean playing = false;
    private boolean paused = false;
    //Indicates if player is listening to audio being ready
    private boolean syncing = false;
    //Main texture used for playing video
    private Texture2D texture;
    //Temp empty image
    private Image emptyImage = new Image(Image.Format.ABGR8, 1, 1, BufferUtils.createByteBuffer(4), ColorSpace.sRGB);
    //Converter
    private final AWTLoader m_AWTLoader = new AWTLoader();
    //Main list for all the frames - raw data
    private final ArrayList<byte[]> frames = new ArrayList<>();
    //debug 
    //private int frameCount=0;
    //private int count=0;
    private long startTime = 0;
    private long pauseTime = 0;
    private long pausePeriod = 0;
    
    //Loading variables
    private ExecutorService executor;
    private LoadingTask loadingTask;
    private Future<?> loadingResult;
    private boolean loading = false;
    private boolean playOnLoad = false;
    private boolean loaded = false;
    private long timeSinceStart = 0;
    private int prevFrameIndex = -1;
    private boolean running = false;
    
    private VideoCodec m_videoCodec = new VideoMjpegCodec();
    //Internal listener 
    private VideoScreenListener videoScreenListener = null;
    
    //Main audio player
    private AudioNode audioBG;
    private final Geometry screenGeom;
    private final Material screenMat;
    private final MediaEffectManager effectManager;
    
    public enum PlaybackMode {
        ONCE, LOOP
    }

    private final PlaybackMode playBackMode;
    private final float fps;
    private final String screenName;
    private final float movieWidth;
    private final float movieHeight;
    private String videoAssetPath;
    private String audioAssetPath;
    private final String idleImageAssetPath;
    private final String loadingImageAssetPath;
    private final String pausedImageAssetPath;
    private final ColorRGBA screenColor;
    
    /**
     * 
     * @param app
     * @param screen
     * @param config 
     */
    protected SimpleMediaPlayer(Application app, Geometry screen, MediaConfig config) {
        this.app = app;
        this.screenGeom = screen;
        this.screenMat = screen.getMaterial();
        effectManager = new MediaEffectManager(screenMat);
        
        Quad quad = (Quad) screen.getMesh();
        this.movieWidth = quad.getWidth();
        this.movieHeight = quad.getHeight();
        this.screenName = config.screenName;
        this.videoAssetPath = config.videoFilePath;
        this.audioAssetPath = config.audioFilePath;
        this.idleImageAssetPath = config.idleImagePath;
        this.loadingImageAssetPath = config.loadingImagePath;
        this.pausedImageAssetPath = config.pausedImagePath;
        this.screenColor = config.screenColor;
        this.playBackMode = config.playBackMode;
        this.fps = 1f / config.framesPerSec;
    }

    /**
     *
     * @return Screen name
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     *
     * @return width of the quad
     */
    public float getWidth() {
        return movieWidth;
    }

    /**
     *
     * @return height of the quad
     */
    public float getHeight() {
        return movieHeight;
    }

    public Geometry getGeometry() {
        return screenGeom;
    }
    
    public MediaEffectManager getEffectManager() {
        return effectManager;
    }

    /**
     *
     * @return material used for displaying media.
     */
    @Deprecated
    public Material getMaterial() {
        return screenMat;
    }
    
    /**
     * Sets new data (video and audio) for the player. Used mainly with
     * genGeometry or genMaterial. Requires reloading to take effect.
     *
     * @param videoAssetPath
     * @param audioAssetPath
     */
    @Deprecated
    public void setMedia(String videoAssetPath, String audioAssetPath) {
        this.videoAssetPath = videoAssetPath;
        this.audioAssetPath = audioAssetPath;
    }

    /**
     * Sets volume for audio
     *
     * @param newVolume
     */
    @Deprecated
    public void setAudioVolume(float newVolume) {
        if (audioAssetPath != null) {
            audioBG.setVolume(newVolume);
        }
    }

    private void startMedia() {
        //If audio is present starts syncing - listening for audio to start
        if (audioAssetPath != null) {
            //enable syncing 
            syncing = true;
            //play audio
            audioBG.play();
        } else {
            startVideo();
        }
    }

    private void syncAudioAndVideo() {
        if (audioBG.getStatus() == AudioSource.Status.Playing) {
            //disable testing
            syncing = false;
            //play video
            startVideo();
        }
    }

    private void startVideo() {
        //inform listener
        notifyOnPrePlay();

        //remove static image
        screenMat.clearParam("Color");
        //set image that will receive data from runner via emptyImage
        screenMat.setTexture("ColorMap", getTexture());
        // 
        setupRunner();
        //
        playing = true;
    }

    //Main method to convert jpg to texture.    
    private void setImage(byte[] jpegBytes) {

        //System.out.println("setImage=" + jpegBytes.length);
        if (jpegBytes == null) {
            return;
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //Back to tex
        texture.setImage(m_AWTLoader.load(image, true));
    }

    private Texture2D getTexture() {
        if (texture == null) {
            texture = new Texture2D(emptyImage);
        }
        return texture;
    }

    //stop playback and release or replay  
    private void stopPlayBack() {
        //prevents doble stop
        if (!playing) {
            return;
        }

        //Disable audio and video. Release data
        if (audioAssetPath != null) {
            audioBG.stop();
        }
        //
        stopRunner();

        //Release  
        emptyImage = null;
        playing = false;
        playOnLoad = false;
        syncing = false;
        //clean threads - otherwise it may still be alive
        if (loadingResult != null) {
            loadingResult.cancel(true);
        }
        if (executor != null) {
            executor.shutdownNow();
        }

        //If played once - reset idle image. With loop play again
        if (playBackMode == PlaybackMode.ONCE) {
            //Idle again
            if (idleImageAssetPath != null) {
                screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(idleImageAssetPath));
            } else {
                screenMat.setColor("Color", screenColor);
            }

            notifyOnEnd();
            
        } else if (playBackMode == PlaybackMode.LOOP) {
            notifyOnLoopEnd();
            playMedia();
        }
    }

    /**
     * Stops media in any state. Also releases any memory reources.
     */
    public void stopMedia() {
        //prevents stop if already stopped - unless during laoding
        if (!playing) {
            if (loading) {
                cleanLoading();
            }
            return;
        }

        //Disable audio and video. Release data
        if (audioAssetPath != null) {
            audioBG.stop();
        }

        //always release
        stopRunner();
        frames.clear();
        loaded = false;

        //Release  
        emptyImage = null;
        playing = false;
        playOnLoad = false;
        syncing = false;
        
        //clean threads - otherwise it may still be alive
        if (loadingResult != null) {
            loadingResult.cancel(true);
        }
        if (executor != null) {
            executor.shutdownNow();
        }

        //Idle again
        if (idleImageAssetPath != null) {
            screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(idleImageAssetPath));
        } else {
            screenMat.setColor("Color", screenColor);
        }

        notifyOnEnd();
    }

    /**
     * Initiatie loading task and play afterwards. Prefered way to start
     * playback. May be splited into andMedia and playMedia
     */
    public void loadAndPlayMedia() {
        //prevents double play, play during loading
        if (playing || loading) {
            return;
        }

        //if play once - always load. If loop - the first time
        //System.out.println("PLAYED ONCE " + frames.size());
        prepareLoading();
        //Also play once loaded 
        playOnLoad = true;
    }

    /**
     * Initiatie loading task without playing. Use playMedia afterwards.
     */
    public void loadMedia() {
        //prevents double play, play during loading
        if (playing || loading) {
            return;
        }

        //if play once - always load. If loop - the first time
        prepareLoading();
        //Also play once loaded 
        playOnLoad = false;
    }

    /**
     * Play already loaded media. Do not use for unpausing - no effect.
     */
    public void playMedia() {
        //prevents double play, play during loading
        if (playing || loading || !loaded) {
            return;
        }
        startMedia();
    }

    /**
     *
     * @return Is currently loading
     */
    public boolean isLoading() {
        return loading;
    }

    /**
     *
     * @return Are frames data preloaded.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     *
     * @return Is playback in action - playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     *
     * @return Is paused
     */
    public boolean isPaused() {
        return paused;
    }

    private void prepareLoading() {
        //inform listener
        notifyOnPreLoad();
        
        //Loading image
        if (loadingImageAssetPath != null) {
            screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(loadingImageAssetPath));
        } else {
            screenMat.setColor("Color", screenColor);
        }

        //init audio - if any 
        if (audioAssetPath != null) {
            audioBG = new AudioNode(app.getAssetManager(), audioAssetPath, AudioData.DataType.Buffer);
            audioBG.setPositional(false);
            audioBG.setLooping(false);
            audioBG.setVolume(1);
        }

        //For async loading - each time
        executor = Executors.newSingleThreadExecutor();
        //Init loading. Check in update
        loadingTask = new LoadingTask();
        loadingResult = executor.submit(loadingTask);
        //start testing for loaded in update
        loading = true;
    }

    /**
     * Pauses the media. Displays predefined image or last frame. Unpause with
     * Unpause and not Play
     */
    public void pauseMedia() {
        //cannot pause not playing 
        if (!playing || paused) {
            return;
        }

        //Paused screen or last frame
        if (pausedImageAssetPath != null) {
            screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(pausedImageAssetPath));
        }

        if (audioAssetPath != null) {
            audioBG.pause();
        }

        paused = true;
        pauseTime = System.currentTimeMillis();
    }

    /**
     * Unpause paused media
     */
    public void unpauseMedia() {
        if (!paused) {
            return;
        }
        paused = false;
        pausePeriod = pausePeriod + System.currentTimeMillis() - pauseTime;
        //reestablish texture
        screenMat.setTexture("ColorMap", getTexture());

        if (audioAssetPath != null) {
            audioBG.play();
        }
    }

    /**
     * Main update method used to display images. Must be called manually from
     * parent object.
     *
     * @param tpf
     */
    public void update(float tpf) {
        //-----------------------LOADING-----------------------
        if (loading) {

            //check if the loading is complete
            if (loadingResult.isDone()) {

                if (!loaded) {
                    //clean
                    cleanLoading();
                    loaded = true;
                    
                    //inform listener
                    notifyOnLoaded();
                    
                    //Play if not waiting for play - starts audio and waits for syncing  
                    if (playOnLoad) {
                        startMedia();
                    }
                }
            }
        }
        //-----------------------PLAYBACK-----------------------
        //Wait for the audio to start
        if (syncing) {
            syncAudioAndVideo();
            return;
        }
        //Do not play if not ON   
        if (!playing) {
            return;
        }

        //PLAY - true if any frame was retrieved and played
        boolean isPlayed = calcFrame();

        //if end - it is already stop by setEnd. Here just small last cleanup
        if (!isPlayed) {
            // do nothing
        }
    }

    private void cleanLoading() {
        //kill task 
        loadingResult.cancel(true);
        executor.shutdownNow();
        //not loading
        loading = false;
    }

    //-----------------------LOADER-----------------------
    private class LoadingTask implements Runnable {

        @Override
        public void run() {
            //Video file stream  
            InputStream videoStream = openAsset(videoAssetPath);
            //read all frames at once
            m_videoCodec.read(videoStream, frames);
        }

        private InputStream openAsset(String name) {
            AssetInfo aInfo = app.getAssetManager().locateAsset(new AssetKey(name));
            return aInfo.openStream();
        }

    }

    //-----------------------RUNNER-----------------------
    private void setupRunner() {
        startTime = System.currentTimeMillis();
        //System.out.println("START " + frames.size());
        running = true;
    }

    private void stopRunner() {
        running = false;
        paused = false;
        pausePeriod = 0;
        if (playBackMode == PlaybackMode.ONCE) {
            frames.clear();
            loaded = false;
        }
    }

    private boolean calcFrame() {

        if (running && !paused) {
            timeSinceStart = ((System.currentTimeMillis() - startTime) - pausePeriod);
//			for (int a = 0; a < frames.size(); a++)
//				viewer.setImage(frames.get(a));
            int currFrameIndex = (int) ((timeSinceStart / fps) / 1000);

            if (currFrameIndex == prevFrameIndex) {
                return true;
            }

            //new frame
            prevFrameIndex = currFrameIndex;
//			if (prevFrameIndex <= 30)
//				frameCount++;
            if (currFrameIndex >= frames.size()) {
                stopPlayBack();
            } else {
                setImage(frames.get(currFrameIndex));
            }

        }
        //System.out.println("running=" + running);

        return running;
    }

    //-----------------------VideoScreenListener-----------------------
        
    private void notifyOnEnd() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onEnd: {0}", screenName);
            videoScreenListener.onEnd(screenName);
        }
    }
    
    private void notifyOnLoopEnd() {
        if (videoScreenListener != null) {
            logger.log(Level.INFO, "Media event onLoopEnd: {0}", screenName);
            videoScreenListener.onLoopEnd(screenName);
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

    public void setListener(VideoScreenListener listener) {
        this.videoScreenListener = listener;
    }

    public VideoScreenListener getListener() {
        return this.videoScreenListener;
    }

    /**
     * Listener for media events. You can use only one listener.
     */
    public void removeListener() {
        this.videoScreenListener = null;
    }

}
