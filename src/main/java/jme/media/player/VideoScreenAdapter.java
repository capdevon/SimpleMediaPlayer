/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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