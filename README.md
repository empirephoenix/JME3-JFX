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

* JME3-JFX is compatible with jme-3.0.10 .
* JME3-JFX require java 8.
  For jME SDK, you should create a java 8 platform, but the java8 support is very bad (because it's based on netbeans 7). As alternative you can try to install jME netbeans plugins into netbeans 8+ (use update center url copied from jME SDK).

## Building

pre-installed : jdk8, gradle 2+

```
git clone https://github.com/empirephoenix/JME3-JFX.git
cd JME3-JFX
gradle assemble
```
