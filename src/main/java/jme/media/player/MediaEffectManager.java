/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jme.media.player;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;

/**
 *
 * @author capdevon
 */
public class MediaEffectManager {

    private final Material screenMat;
    

    protected MediaEffectManager(Material screenMat) {
        this.screenMat = screenMat;
    }

    /**
     * Sets alpha for the display
     *
     * @param alpha
     */
    public void setAlpha(float alpha) {
        screenMat.setFloat("Alpha", FastMath.clamp(alpha, 0, 1));
        screenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    }

    /**
     * Enables or disables VHS effect - small line glitches
     *
     * @param enabled
     */
    public void enableVHSEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledVHS", enabled);
        } else {
            screenMat.clearParam("EnabledVHS");
        }
    }

    /**
     * Enables or disables Line effect - wide line moving vertically
     *
     * @param enabled
     */
    public void enableLineEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledLine", enabled);
        } else {
            screenMat.clearParam("EnabledLine");
        }
    }

    /**
     * Enables or disables Grain effect - white noise
     *
     * @param enabled
     */
    public void enableGrainEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledGrain", enabled);
        } else {
            screenMat.clearParam("EnabledGrain");
        }
    }

    /**
     * Enables or disables scanline effect - tv lines across the screen
     *
     * @param enabled
     */
    public void enableScanlineEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledScanline", enabled);
        } else {
            screenMat.clearParam("EnabledScanline");
        }
    }

    /**
     * Enables or disables vignette effect
     *
     * @param enabled
     */
    public void enableVignetteEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledVignette", enabled);
        } else {
            screenMat.clearParam("EnabledVignette");
        }
    }

    /**
     * Enables or disables LCD effect - weak pixelization
     *
     * @param enabled
     */
    public void enableLCDEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledLCD", enabled);
        } else {
            screenMat.clearParam("EnabledLCD");
        }
    }

    /**
     * Enables or disables CRT effect - old monitor pixelization
     *
     * @param enabled
     */
    public void enableCRTEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledCRT", enabled);
        } else {
            screenMat.clearParam("EnabledCRT");
        }
    }

    /**
     * Enables or disables Glitch effect - strong disturbances
     *
     * @param enabled
     */
    public void enableGlitchEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledGlitch", enabled);
        } else {
            screenMat.clearParam("EnabledGlitch");
        }
    }

    /**
     * Enables or disables Black and White effect
     *
     * @param enabled
     */
    public void enableBlackAndWhiteEffect(boolean enabled) {
        if (enabled) {
            screenMat.setBoolean("EnabledBAW", enabled);
        } else {
            screenMat.clearParam("EnabledBAW");
        }
    }

}
