

# Introduction #

Instructions to build the plugins from source.

# Prerequisites #

  * get [Mercurial](http://www.selenic.com/mercurial/wiki/BinaryPackages) for your preferred os
  * get a preconfigured Eclipse 3.5 (Galileo) + TMF Xtext 0.7.2 from for example [itemis](http://xtext.itemis.com/xtext/language=en/23947/downloads)
  * add the following plugins
    * optional
      * [EclipseGraphViz](http://abstratt.com/update/)

# Step by step #

  1. clone the source repository
```
hg clone https://emqcfg.googlecode.com/hg/ emqcfg  
```
  1. add a **`src-gen`** folder to the plugin projects **`org.grassaccords.emqcfg`**, **`org.grassaccords.emqcfg.generator`** and **`org.grassaccords.emqcfg.ui`** (Mercurial does not handle empty folders)
  1. import all plugin projects into your workspace
  1. regenerate all Xtext artifacts from the model **`org.grassaccords.emqcfg/src/mqcfg.xtxt`**
  1. Create a new Eclipse Application
```
Run -> Run Configurations ... -> Eclipse Application
```
  1. Create a new MQCfg project
```
File -> New -> Other -> Xtext -> MQCfg Project
```
> and generate all artifacts

After that you should see something like the following
(the picture was taken from an older version):

![http://emqcfg.googlecode.com/files/eMQCfg-screenshot-1.jpg](http://emqcfg.googlecode.com/files/eMQCfg-screenshot-1.jpg)