package jme.media.player.test;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import jme.media.player.MediaAppState;
import jme.media.player.MediaConfig;
import jme.media.player.SimpleMediaPlayer;

/**
 * Test to show how to use MediaPlayer as intro or cutscene/outro.
 */
public class IntroStateTest extends SimpleApplication {

    private MediaAppState introState;
    //State shown after the intro
    private MenuState menuState;

    /**
     * Start the jMonkeyEngine application
     * @param args 
     */
    public static void main(String[] args) {
        IntroStateTest app = new IntroStateTest();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        setDisplayStatView(false);
        //setDisplayFps(false);
        flyCam.setDragToRotate(true);

        //Config
        MediaConfig config = new MediaConfig();
        //Original movie dimensions - relevant only for keeping aspect ratio
        config.movieWidth = 960;
        config.movieHeight = 540;
        //True if aspect ratio should be kept. False is the movie should be stretched to the screen 
        //config.keepAspect = true;
        //Unique name
        config.screenName = "Intro";
        //Image to display when player is idle. Null to use screenColor
        config.idleImagePath = "Textures/Media/idle-image.jpg";
        //Image to display when player is loading. Null to use screenColor
        config.loadingImagePath = "Textures/Media/loading-image.jpg";
        //Image to display when player is paused. Null to last frame
        config.pausedImagePath = "Textures/Media/paused-image.jpg";
        //Color to use if above pictures are not provided.
        config.screenColor = ColorRGBA.Black;
        //Video to play. Must not be null
        config.videoFilePath = "Media/960_540.mjpg";
        //Audio to play. Null if no audio
        config.audioFilePath = "Media/audio.ogg";
        //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
        config.framesPerSec = 30;
        //Playback mode. Play once or loop
        config.playBackMode = SimpleMediaPlayer.PlaybackMode.ONCE;
        //Transparency of the screen. 1 for intro, material and menu geometries. Below 1 for HUD geometries
        config.alpha = 1f;
        
        //Generate state
        introState = new MediaAppState(config);
        //Add intro state. Auto load and play video on enabled
        stateManager.attach(introState);

        //Menu state - switched after intro. Initially detached. Attached on movie end
        menuState = new MenuState();
    }
    
    /**
     * Method to switch between player and other screen/state.
     */
    private void switchFromIntroToMenu() {
        stateManager.detach(introState);
        stateManager.attach(menuState);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (introState.isStopped()) {
            switchFromIntroToMenu();
        }
    }

    //Fake state representing main menu  
    private class MenuState extends BaseAppState {

        Node guiNode;
        Node rootNode;
        BitmapText hintText;
        Geometry boxGeo;

        @Override
        protected void initialize(Application app) {
            this.rootNode = ((SimpleApplication) app).getRootNode();
            this.guiNode = ((SimpleApplication) app).getGuiNode();

            BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
            hintText = new BitmapText(font);
            hintText.setSize(font.getCharSet().getRenderedSize() * 3.0f);
            hintText.setColor(ColorRGBA.Red);
            hintText.setText("GAME MENU");
            hintText.setLocalTranslation(20, 400, 1.0f);
            
            Box boxMesh = new Box(1f, 1f, 1f);
            boxGeo = new Geometry("Colored Box", boxMesh);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Green);
            boxGeo.setMaterial(mat);
        }

        @Override
        protected void cleanup(Application app) {
        }

        @Override
        protected void onEnable() {
            rootNode.attachChild(boxGeo);
            guiNode.attachChild(hintText);
        }

        @Override
        protected void onDisable() {
            rootNode.detachChild(boxGeo);
            guiNode.detachChild(hintText);
        }

    }

}
