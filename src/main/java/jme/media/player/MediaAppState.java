/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jme.media.player;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author capdevon
 */
public class MediaAppState extends BaseAppState {
    
    private static final Logger LOGGER = Logger.getLogger(MediaAppState.class.getName());
    
    private static final String KEY_SKIP = "SKIP_VIDEO";
    
    private final MediaConfig config;
    private InputManager inputManager;
    private Node guiNode;
    private SimpleMediaPlayer mediaPlayer;
    private Geometry screen;
    private boolean isStopped;
    private boolean isDisposed;
    
    public MediaAppState(MediaConfig config) {
        this.config = config;
    }

    @Override
    protected void initialize(Application app) {
        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.inputManager = app.getInputManager();
        
        isStopped = isDisposed = false;
        
        MediaPlayerFactory factory = new MediaPlayerFactory(app);
        mediaPlayer = factory.createMediaPlayer(config);
        screen = mediaPlayer.getGeometry();

        guiNode.attachChild(screen);
        mediaPlayer.loadAndPlayMedia();

        mediaPlayer.setVideoScreenListener(new VideoScreenAdapter() {
            @Override
            public void onEnd(String screenName) {
                isStopped = true;
            }
        });
        
        LOGGER.log(Level.INFO, "MediaAppState initialized");
    }

    @Override
    protected void cleanup(Application app) {
        isDisposed = true;
        mediaPlayer.stopMedia();
        guiNode.detachChild(screen);
    }

    @Override
    protected void onEnable() {
        // Assign video skipping keys
        inputManager.addMapping(KEY_SKIP, new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, KEY_SKIP);
        LOGGER.log(Level.INFO, "MediaAppState enable input listener");
    }

    @Override
    protected void onDisable() {
        // Clean our mapping
        inputManager.deleteMapping(KEY_SKIP);
        inputManager.removeListener(actionListener);
        LOGGER.log(Level.INFO, "MediaAppState disable input listener");
    }
  
    @Override
    public void update(float tpf) {
        mediaPlayer.update(tpf);
    }
    
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) {
                mediaPlayer.stopMedia();
            }
        }
    };
    
    public boolean isStopped() {
        return isStopped;
    }
    
    public boolean isDisposed() {
        return isDisposed;
    }
    
}
