package jme.media.player;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author capdevon
 */
public class MediaPlayerFactory {

    private final Application app;

    public MediaPlayerFactory(Application app) {
        this.app = app;
    }

    public SimpleMediaPlayer createMediaPlayer(MediaConfig config) {

        int screenWidth = app.getCamera().getWidth();
        int screenHeight = app.getCamera().getHeight();

        int quadWidth;
        int quadHeight;

        // If keepAspect is true, the original movie dimensions are used to create the screen geometry.
        if (config.keepAspect) {
            quadWidth = config.movieWidth;
            quadHeight = config.movieHeight;
        } else {
            // If keepAspect is false, the screen is stretched to fit the whole viewport.
            quadWidth = screenWidth;
            quadHeight = screenHeight;
        }

        Geometry screen = new Geometry(config.screenName, new Quad(quadWidth, quadHeight));
        Material mat = createScreenMaterial(config);
        screen.setMaterial(mat);

        // Setup screen location
        float offsetX = (screenWidth - quadWidth) / 2f;
        float offsetY = (screenHeight - quadHeight) / 2f;
        screen.setLocalTranslation(new Vector3f(offsetX, offsetY, 1));

        if (config.alpha > 0f && config.alpha < 1f) {
            // Set transparency
            mat.setFloat("Alpha", config.alpha);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            screen.setQueueBucket(RenderQueue.Bucket.Transparent);
        }

        return new SimpleMediaPlayer(app.getAssetManager(), screen, config);
    }

    private Material createScreenMaterial(MediaConfig config) {

        // material and texture
        Material mat = new Material(app.getAssetManager(), "MatDefs/SimpleMediaPlayer/SimpleMediaPlayer.j3md");

        if (config.idleImagePath != null) {
            mat.setTexture("ColorMap", app.getAssetManager().loadTexture(config.idleImagePath));
        } else {
            mat.setColor("Color", config.screenColor);
        }

        return mat;
    }

}
