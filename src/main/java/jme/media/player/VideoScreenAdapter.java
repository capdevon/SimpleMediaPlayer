package jme.media.player;

/**
 *
 * @author capdevon
 */
public abstract class VideoScreenAdapter implements VideoScreenListener {

    @Override
    public void onPreLoad(String screenName) {}

    @Override
    public void onLoaded(String screenName) {}

    @Override
    public void onPrePlay(String screenName) {}

    @Override
    public void onLoopEnd(String screenName) {}

    @Override
    public void onEnd(String screenName) {}

}