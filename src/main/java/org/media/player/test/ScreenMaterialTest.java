/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.media.player.test;

import org.media.player.MediaConfig;
import org.media.player.MediaPlayerFactory;
import org.media.player.SimpleMediaPlayer;
import org.media.player.VideoScreenAdapter;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;

/**
 * Test to show how to use MediaPlayer.
 */
public class ScreenMaterialTest extends SimpleApplication implements ActionListener {

    private SimpleMediaPlayer mediaPlayer;
    private BitmapText hintText;
    private PointLight pointLight;
    private Node scene;

    /**
     * Start the jMonkeyEngine application
     * @param args
     */
    public static void main(String[] args) {
        ScreenMaterialTest app = new ScreenMaterialTest();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        //faster cam
        flyCam.setMoveSpeed(15f);
        cam.setLocation(new Vector3f(7.2f, 3.2f, 4.2f));
        cam.lookAtDirection(new Vector3f(-0.90f, -0.22f, -0.36f), Vector3f.UNIT_Y);

        //Scene
        Node sp = (Node) assetManager.loadModel("Models/testScene.j3o");
        sp.depthFirstTraversal((Spatial spatial) -> {
            System.out.println(spatial);
            if (spatial.getName().endsWith(".blend")) {
                spatial.removeFromParent();
            }
        });
        
        scene = (Node) sp.getChild("Scene");
        rootNode.attachChild(scene);
        
        //Shadows
        scene.getChild("Room").setShadowMode(ShadowMode.Receive); // The wall can cast shadows and also receive them.
        scene.getChild("TV").setShadowMode(ShadowMode.CastAndReceive); // Any shadows cast by the floor would be hidden by it.
        scene.getChild("Table").setShadowMode(ShadowMode.CastAndReceive);

        addLighting(scene);
        addHint();

        //-------------------PLAYER-------------------
        mediaPlayer = createMediaPlayer();
        mediaPlayer.setVideoScreenListener(new VideoScreenAdapter() {});
        Material modelMat = mediaPlayer.getMaterial();
        //Effects
//        mediaPlayer.getEffectManager().enableScanlineEffect(true);
//        mediaPlayer.getEffectManager().enableBlackAndWhiteEffect(true);
//        mediaPlayer.getEffectManager().enableLineEffect(true); //doesn't work
//        mediaPlayer.getEffectManager().enableGrainEffect(true); //doesn't work
//        mediaPlayer.getEffectManager().enableVignetteEffect(true); //doesn't work
        mediaPlayer.getEffectManager().enableLCDEffect(true);
//        mediaPlayer.getEffectManager().enableCRTEffect(true); //doesn't work
//        mediaPlayer.getEffectManager().enableGlitchEffect(true);
        //Get submesh from a model. It was separated from different color during the import
        ((Node) scene.getChild("TV")).getChild("mat2").setMaterial(modelMat);
        
        configureInputs();
    }
    
    private SimpleMediaPlayer createMediaPlayer() {
        //Config
        MediaConfig config = new MediaConfig();
        //Unique name
        config.screenName = "Screen Material";
        //Image to display when player is idle. Null to use screenColor
        config.idleImagePath = "Textures/Media/idle-image.jpg";
        //Image to display when player is loading. Null to use screenColor
        config.loadingImagePath = "Textures/Media/loading-image.jpg";
        //Image to display when player is paused. Null to use screenColor
        config.pausedImagePath = "Textures/Media/paused-image.jpg";
        //Color to use if any of above pictures is not provided.
        config.screenColor = ColorRGBA.Black;
        //Video to play
        config.videoFilePath = "Media/320_180.mjpg";
        //Audio to play
        config.audioFilePath = "Media/audio.ogg";
        //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
        config.framesPerSec = 30;
        //Playback mode. Play once or loop
        config.playBackMode = SimpleMediaPlayer.PlaybackMode.LOOP;
        //Transparency of the screen. 1 for intro, material and menu geometries. Below 1f for HUD geometries
        config.alpha = 1f;
        
        MediaPlayerFactory factory = new MediaPlayerFactory(this);
        return factory.createMediaPlayer(config);
    }
    
    private void addLighting(Node root) {
        //Background color
        viewPort.setBackgroundColor(ColorRGBA.Gray);
        
        //Light
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(1.5f));
        sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
        root.addLight(sun);

        pointLight = new PointLight();
        pointLight.setColor(ColorRGBA.White.mult(2.5f));
        pointLight.setRadius(10f);
        pointLight.setPosition(new Vector3f(0, 2, 0));
        root.addLight(pointLight);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 3);
        dlsf.setLight(sun);
        dlsf.setEnabled(true);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
    }
    
    private void addHint() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        hintText = new BitmapText(font);
        hintText.setSize(font.getCharSet().getRenderedSize() * 1.4f);
        hintText.setColor(ColorRGBA.Red);
        hintText.setText("Play/Pause:SPACE Reload:ENTER");
        hintText.setLocalTranslation(0, this.getCamera().getHeight() - 10, 1.0f);
        guiNode.attachChild(hintText);
    }
    
    private void configureInputs() {
        inputManager.addMapping(TOGGLE_PAUSE, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(RELOAD_MEDIA, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, TOGGLE_PAUSE, RELOAD_MEDIA);
    }
    
    private final String TOGGLE_PAUSE = "TOGGLE_PAUSE";
    private final String RELOAD_MEDIA = "RELOAD_MEDIA";

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (!isPressed) {
            return;
        }

        if (name.equals(TOGGLE_PAUSE)) {
            if (mediaPlayer.isLoaded() && mediaPlayer.isPlaying()) {
                //enable    
                if (mediaPlayer.isPaused()) {
                    mediaPlayer.unpauseMedia();
                } else {
                    mediaPlayer.pauseMedia();
                }
            }
        } else if (name.equals(RELOAD_MEDIA)) {
            if (mediaPlayer.isLoaded() && mediaPlayer.isPlaying()) {
                mediaPlayer.stopMedia();
            } else {
                mediaPlayer.loadAndPlayMedia();
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {

        mediaPlayer.update(tpf);

        //Silly blinking screen effect
        if (mediaPlayer.isPlaying() && !mediaPlayer.isPaused()) {
            float radius = oscillate(pointLight.getRadius(), 10f, 12f, tpf);
            pointLight.setRadius(radius);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        mediaPlayer.stopMedia();
    }
    
    private float osciTime = 0;

    private float oscillate(float input, float min, float max, float delta) {
        float coeff = 0.1f;
        if (delta % 2 == 0) {
            osciTime = osciTime + delta;
        } else {
            osciTime = osciTime - delta;
        }

        float newValue = FastMath.clamp((float) (input + Math.sin(osciTime) * coeff), min, max);

        if (newValue == max || newValue == min) {
            newValue = (min + max) / 2;
        }

        return newValue;
    }

}
