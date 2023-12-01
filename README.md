# SimpleMediaPlayer

Optimized version of the project https://github.com/polincdev/SimpleMediaPlayer

NEW [Class-Diagram](images/smp-class-diagram-2.jpg)

🔔 If you found this project useful, please let me know by leaving a star to motivate me to improve it. Thanks.

Some graphical effects do not work in the original version, suggestions are welcome.

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

## Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- jdk8+

## Credits
All files under the 'resources' folder belong to polincdev.
