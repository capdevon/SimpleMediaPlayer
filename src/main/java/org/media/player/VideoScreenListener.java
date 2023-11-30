package org.media.player;

/**
 * Listener for media events. You can use only one setListener. Marks key
 * moments for the playback
 */
public interface VideoScreenListener {

    /**
     * Triggered when loadAndPlayMedia or loadMedia is called
     *
     * @param screenName to call it.
     */
    public void onPreLoad(String screenName);

    /**
     * Triggered when video is loaded before syncing with audio
     *
     * @param screenName to call it.
     */
    public void onLoaded(String screenName);

    /**
     * Triggered right before playing media
     *
     * @param screenName to call it.
     */
    public void onPrePlay(String screenName);

    /**
     * Triggered after playback loop is finished. Called only in LOOP mode
     *
     * @param screenName to call it.
     */
    public void onLoopEnd(String screenName);

    /**
     * Triggered after playback is finished. Called in LOOP and ONCE mode at the
     * very end.
     *
     * @param screenName to call it.
     */
    public void onEnd(String screenName);

}
