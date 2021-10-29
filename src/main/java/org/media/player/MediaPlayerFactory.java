package org.media.player;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author capdevon
 */
public class MediaPlayerFactory {

    private final Application app;
    
    /**
     * Main constructor
     *
     * @param app
     */
    public MediaPlayerFactory(Application app) {
        this.app = app;
    }

    /**
     * 
     * @param config
     * @return
     */
    public SimpleMediaPlayer createMediaPlayer(MediaConfig config) {

        //Calculates dimensions of the geometry
        int width = app.getCamera().getWidth();
        int height = app.getCamera().getHeight();
        float zoomingFactor = config.zoomingFactor;
        
        /**
         * If keepAspect is true the screen is not stretched. Instead it is centered
         * according to the width. ScreenColor is used to fill the screen.
         */
        if (config.keepAspect) {
            width = config.movieWidth;
            height = config.movieHeight;
            app.getViewPort().setBackgroundColor(config.screenColor);
        }
        
        float quadWidth = width * zoomingFactor;
        float quadHeight = height * zoomingFactor;
        
        Geometry screen = new Geometry(config.screenName, new Quad(quadWidth, quadHeight));
        Material mat = createScreenMaterial(config);
        screen.setMaterial(mat);
        
        // Setup screen location
        int offsetX = (int) ((quadWidth - width) / 2f);
        int offsetY = (int) ((quadHeight - height) / 2f);
        screen.setLocalTranslation(new Vector3f(-offsetX, -offsetY, 1));
        
        SimpleMediaPlayer mediaPlayer = new SimpleMediaPlayer(app, screen, config);
        //mediaPlayer.loadMedia();
        
        return mediaPlayer;
    }

    private Material createScreenMaterial(MediaConfig config) { 

        //material and texture
        Material mat = new Material(app.getAssetManager(), "MatDefs/SimpleMediaPlayer/SimpleMediaPlayer.j3md");

        //Set transparency
        mat.setFloat("Alpha", FastMath.clamp(config.alpha, 0, 1));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        //Initial idle
        if (config.idleImagePath != null) {
            mat.setTexture("ColorMap", app.getAssetManager().loadTexture(config.idleImagePath));
        } else {
            mat.setColor("Color", config.screenColor);
        }
        
        return mat;
    }

}
