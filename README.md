# SimpleMediaPlayer

Optimized version of the project https://github.com/polincdev/SimpleMediaPlayer

NEW [Class-Diagram](images/smp-class-diagram-2.jpg)


Some graphic effects don't work, I don't have the necessary knowledge to fix shaders. Any suggestions are welcome.

```java
mediaPlayer.getEffectManager().enableLineEffect(true); //doesn't work
mediaPlayer.getEffectManager().enableGrainEffect(true); //doesn't work
mediaPlayer.getEffectManager().enableVignetteEffect(true); //doesn't work
mediaPlayer.getEffectManager().enableCRTEffect(true); //doesn't work

mediaPlayer.getEffectManager().enableLCDEffect(true); //ok
mediaPlayer.getEffectManager().enableVHSEffect(true); //ok
mediaPlayer.getEffectManager().enableScanlineEffect(true); //ok
mediaPlayer.getEffectManager().enableGlitchEffect(true); //ok
mediaPlayer.getEffectManager().enableBlackAndWhiteEffect(true); //ok
```

## Credits
All files under the 'resources' folder belong to polincdev.
