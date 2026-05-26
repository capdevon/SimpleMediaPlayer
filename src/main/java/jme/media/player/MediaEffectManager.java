package jme.media.player;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;

/**
 * @author capdevon
 */
public class MediaEffectManager {

    private final Material screenMat;

    protected MediaEffectManager(Material screenMat) {
        this.screenMat = screenMat;
    }

    private void toggleEffect(String paramName, boolean enabled) {
        if (enabled) {
            screenMat.setBoolean(paramName, true);
        } else {
            screenMat.clearParam(paramName);
        }
    }

    /**
     * Enables or disables VHS effect - small line glitches
     */
    public void enableVHSEffect(boolean enabled) {
        toggleEffect("EnabledVHS", enabled);
    }

    /**
     * Enables or disables Line effect - wide line moving vertically
     */
    public void enableLineEffect(boolean enabled) {
        toggleEffect("EnabledLine", enabled);
    }

    /**
     * Enables or disables Grain effect - white noise
     */
    public void enableGrainEffect(boolean enabled) {
        toggleEffect("EnabledGrain", enabled);
    }

    /**
     * Enables or disables scanline effect - tv lines across the screen
     */
    public void enableScanlineEffect(boolean enabled) {
        toggleEffect("EnabledScanline", enabled);
    }

    /**
     * Enables or disables vignette effect
     */
    public void enableVignetteEffect(boolean enabled) {
        toggleEffect("EnabledVignette", enabled);
    }

    /**
     * Enables or disables LCD effect - weak pixelation
     */
    public void enableLCDEffect(boolean enabled) {
        toggleEffect("EnabledLCD", enabled);
    }

    /**
     * Enables or disables CRT effect - old monitor pixelation
     */
    public void enableCRTEffect(boolean enabled) {
        toggleEffect("EnabledCRT", enabled);
    }

    /**
     * Enables or disables Glitch effect - strong disturbances
     */
    public void enableGlitchEffect(boolean enabled) {
        toggleEffect("EnabledGlitch", enabled);
    }

    /**
     * Enables or disables Black and White effect
     */
    public void enableBlackAndWhiteEffect(boolean enabled) {
        toggleEffect("EnabledBAW", enabled);
    }
}