# SimpleMediaPlayer

[![Java Support](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![jMonkeyEngine](https://img.shields.io/badge/jMonkeyEngine-3.9.0--stable-blue.svg)](https://jmonkeyengine.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A lightweight **Media Player** for jMonkeyEngine (JME3).

Easily play **MJPEG videos** and **OGG audio** with support for real-time visual effects (VHS, Glitch, Scanline, LCD, etc.).

🔔 If you found this project useful, please let me know by leaving a star to motivate me to improve it. Thanks.

### Features
- Simple and clean API
- Video and audio playback
- Multiple post-processing effects
- Optimized for jMonkeyEngine

NEW [Class-Diagram](images/smp-class-diagram-2.jpg)

## Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- jdk11+

## Installation & Local Setup

If you want to use this project as a library inside your other jME3 games or applications, you can compile and install it into your local Maven cache (`.m2/repository`).

Run the following command in the project root directory:

```bash
./gradlew install
```

Inside your Project:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'com.github.capdevon:simple-media-player:1.0.0-SNAPSHOT'
}
```