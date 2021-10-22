/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.media.player.test;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import org.media.player.MediaConfig;
import org.media.player.MediaPlayerFactory;
import org.media.player.SimpleMediaPlayer;

/**
 * Test to show how to use MediaPlayer.
 */
public class MenuGeometryTest extends SimpleApplication {

    /**
     * Start the jMonkeyEngine application
     * @param args 
     */
    public static void main(String[] args) {
        MenuGeometryTest app = new MenuGeometryTest();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        app.setSettings(settings);
        app.start();
    }
    
    //Map for picking
    private final HashMap<Rectangle, SimpleMediaPlayer> pickPlayer = new HashMap<>();
    //Map for selecting
    private final ArrayList<SimpleMediaPlayer> mplayersList = new ArrayList<>();

    @Override
    public void simpleInitApp() {

        viewPort.setBackgroundColor(ColorRGBA.White);
        flyCam.setEnabled(false);
        
        //Config
        MediaConfig config = new MediaConfig();
        
        //Relative size 16/9
        int width = (int) (cam.getHeight() / 2.3f);
        int height = (int) ((width / 16f) * 9f);
        int margin = width / 3;
        config.movieWidth = width;
        config.movieHeight = height;
        config.keepAspect = true;

        //--------------------GEOMETRIES--------------------
        MediaPlayerFactory factory = new MediaPlayerFactory(this);
        
        //1
        config.screenName = "Menu1";
        config.idleImagePath = "Textures/Media/idle-image.jpg";
        config.loadingImagePath = "Textures/Media/loading-image.jpg";
        config.pausedImagePath = "Textures/Media/paused-image.jpg";
        config.screenColor = ColorRGBA.Black;
        config.videoFilePath = "Media/960_540.mjpg";
        config.audioFilePath = "Media/audio.ogg";
        config.framesPerSec = 30;
        config.playBackMode = SimpleMediaPlayer.PlaybackMode.LOOP;
        config.alpha = 1f;
        //Effect
        SimpleMediaPlayer mplayer1 = factory.createMediaPlayer(config);
        Geometry menuGeo1 = mplayer1.getGeometry();
        mplayer1.getEffectManager().enableVHSEffect(true);
        //Add to gui 
        guiNode.attachChild(menuGeo1);
        //Position      
        menuGeo1.setLocalTranslation(cam.getWidth() / 4 - width / 2, cam.getHeight() * 0.75f - height / 2, 1.0f);
        //Add to map for picking
        pickPlayer.put(createRectangle(menuGeo1, mplayer1), mplayer1);
        mplayersList.add(mplayer1);
        
        //2
        config.screenName = "Menu2";
        config.screenColor = ColorRGBA.Red;
        config.videoFilePath = "Media/960_540.mjpg";
        config.idleImagePath = null;
        config.playBackMode = SimpleMediaPlayer.PlaybackMode.ONCE;
        //Effect
        SimpleMediaPlayer mplayer2 = factory.createMediaPlayer(config);
        Geometry menuGeo2 = mplayer2.getGeometry();
//        mplayer2.getEffectManager().enableLineEffect(true); //doesn't work
//        mplayer2.getEffectManager().enableGrainEffect(true); //doesn't work
        //Add to gui 
        guiNode.attachChild(menuGeo2);
        //Position      
        menuGeo2.setLocalTranslation(cam.getWidth() * 0.75f - width / 2, (int) menuGeo1.getLocalTranslation().y, 1.0f);
        //Add to map for picking
        pickPlayer.put(createRectangle(menuGeo2, mplayer2), mplayer2);
        mplayersList.add(mplayer2);
        
        //3
        config.screenName = "Menu3";
        config.screenColor = ColorRGBA.Green;
        config.videoFilePath = "Media/800_480.mjpg";
        config.idleImagePath = "Textures/Media/idle-image.jpg";
        config.loadingImagePath = null;
        config.playBackMode = SimpleMediaPlayer.PlaybackMode.ONCE;
        //Effect
        SimpleMediaPlayer mplayer3 = factory.createMediaPlayer(config);
        Geometry menuGeo3 = mplayer3.getGeometry();
        mplayer3.getEffectManager().enableScanlineEffect(true);
        mplayer3.getEffectManager().enableBlackAndWhiteEffect(true);
        //Add to gui
        guiNode.attachChild(menuGeo3);
        //Position      
        menuGeo3.setLocalTranslation(menuGeo1.getLocalTranslation().x, cam.getHeight() / 4 - height / 2, 1.0f);
        //Add to map for picking
        pickPlayer.put(createRectangle(menuGeo3, mplayer3), mplayer3);
        mplayersList.add(mplayer3);
        
        //4
        config.screenName = "Menu4";
        config.videoFilePath = "Media/640_360.mjpg";
        config.idleImagePath = "Textures/Media/idle-image.jpg";
        config.loadingImagePath = "Textures/Media/loading-image.jpg";
        config.pausedImagePath = null;
        config.audioFilePath = null;
        //Effect
        SimpleMediaPlayer mplayer4 = factory.createMediaPlayer(config);
        Geometry menuGeo4 = mplayer4.getGeometry();
//        mplayer4.getEffectManager().enableVignetteEffect(true); //doesn't work
        mplayer4.getEffectManager().enableLCDEffect(true);
        //Add to gui 
        guiNode.attachChild(menuGeo4);
        //Position      
        menuGeo4.setLocalTranslation(menuGeo2.getLocalTranslation().x, menuGeo3.getLocalTranslation().y, 1.0f);
        //Add to map for picking
        pickPlayer.put(createRectangle(menuGeo4, mplayer4), mplayer4);
        mplayersList.add(mplayer4);

        createLabel("960/540 Full image decoration. Loop.", menuGeo1.getLocalTranslation(), margin);
        createLabel("800/480 No idle image. Red. Run once.", menuGeo2.getLocalTranslation(), margin);
        createLabel("640/360 No loading image. Green. Run once.", menuGeo3.getLocalTranslation(), margin);
        createLabel("320/180 No pause image. No audio. Run once.", menuGeo4.getLocalTranslation(), margin);

        //
        inputManager.addRawInputListener(new MediaPlayerInputListener());
    }
    
    private Rectangle createRectangle(Geometry geo, SimpleMediaPlayer mediaPlayer) {
        int x = (int) geo.getLocalTranslation().x;
        int y = (int) geo.getLocalTranslation().y;
        int width = (int) mediaPlayer.getWidth();
        int height = (int) mediaPlayer.getHeight();
        return new Rectangle(x, y, width, height);
    }
    
    private void createLabel(String text, Vector3f pos, float yOffset) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText bmp = new BitmapText(font);
        bmp.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        bmp.setColor(ColorRGBA.Black);
        bmp.setText(text);
        bmp.setLocalTranslation(pos.x, pos.y - yOffset / 4, 1.0f);
        guiNode.attachChild(bmp);
    }
    
    private class MediaPlayerInputListener extends RawInputAdapter {

        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt) {

            if (evt.isPressed()) {
                return;
            }
            
            // System.out.println("Click "+evt.getX()+" "+evt.getY()+" "+ key.contains( evt.getX() ,evt.getY() ));

            for (Rectangle key : pickPlayer.keySet()) {
                //
                if (key.contains(evt.getX(), evt.getY())) {
                    SimpleMediaPlayer mediaPlayer = pickPlayer.get(key);

                    //Pause the rest if playing
                    for (SimpleMediaPlayer smp : pickPlayer.values()) {
                        if (!mediaPlayer.getScreenName().equals(smp.getScreenName()) && smp.isPlaying()) {
                            smp.pauseMedia();
                        }
                    }

                    //enable    
                    if (mediaPlayer.isPaused()) {
                        mediaPlayer.unpauseMedia();
                    } else if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pauseMedia();
                    } else if (!mediaPlayer.isLoaded()) {
                        mediaPlayer.loadAndPlayMedia();
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //!!!!!!!!!!IMPORTANT
        mplayersList.forEach(mp -> mp.update(tpf));
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

}
