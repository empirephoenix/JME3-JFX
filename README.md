JME3-JFX
========

JFX Gui bridge for JME with usefull utilities for common usecases.

License is the New BSD License (same as JME3) 
http://opensource.org/licenses/BSD-3-Clause

Binary releases are available at :

 * https://bintray.com/empirephoenix/VGS-OSS-Releases/jfx/view/files/com/jme3x/jfx ( [maven repo](http://dl.bintray.com/empirephoenix/VGS-OSS-Releases) )
 * https://bintray.com/jmonkeyengine/contrib ( [maven repo](http://dl.bintray.com/jmonkeyengine/contrib) )

also it can be used as maven repository (see 'set me up' button).

## Notes

* For jme 3.1:
  * version: 2.+
  * branch: master
* For jme 3.0 (compatible with jme-3.0.10):
  * version: 1.+
  * branch: jme_3.0
* JME3-JFX require java 8, but it is possible to draw video on textures using java 7.
  For jME SDK, you should create a java 8 platform, but the java8 support is very bad (because it's based on netbeans 7). As alternative you can try to install jME netbeans plugins into netbeans 8+ (use update center url copied from jME SDK).

## Building

pre-installed : jdk8, gradle 2+

```
git clone https://github.com/empirephoenix/JME3-JFX.git
cd JME3-JFX
gradle assemble
```

## MediaPlayer notes

The JavaFX MediaPlayer supports:

    Audio: MP3; AIFF containing uncompressed PCM; WAV containing uncompressed PCM; MPEG-4 multimedia container with Advanced Audio Coding (AAC) audio
    Video: FLV containing VP6 video and MP3 audio; MPEG-4 multimedia container with H.264/AVC (Advanced Video Coding) video compression
    
H.264/AVC is preinstalled on Windows 7/8 only, on Vista/XP playing video may cause problems.
For those, who needs backward compatibility (note that XP is no longer suppoerted by M$) the solution is to use VP6 format in FLV container. Here is a quick guide of encoding a media file into .flv:

1) Download and install VP6 codec - http://www.afterdawn.com/software/audio_video/codecs/vp6_vfw_codec.cfm
2) Open your video file in VirtualDub, compress it using VP6, save as AVI (it supports only avi afaik)
3) Open the AVI in FFCoder, select .flv as file format, select 'copy' in video. 

The newly created media file is fully compatibile with all JavaFX and Windows versions. You can use it for non commercial products, as it stands in VP6's license.

If you need a file encoded with VP6 for commercial use, the only way is to produce it in some online service, like zencoding.com


