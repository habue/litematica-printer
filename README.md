# Litematica Printer — Minecraft 26.1.2

This branch builds the IceTank printer fork for **Minecraft 26.1.2** with **Java 25**.
The original Minecraft 1.21.4 version remains available on the `1.21.4` branch.

## Build and download

- Build locally with JDK 25: `bash ./gradlew :v26_1_2:build`.
- The mod is written to `v26_1_2/build/libs/litematica-printer-3.4.0+mc26.1.2.jar`.
- GitHub Actions builds pushes and pull requests to `26.1.2`. Open a successful
  run under **Actions → Build Minecraft 26.1.2** and download the
  **litematica-printer-mc26.1.2** artifact, then extract the JAR.
- Install Fabric Loader 0.19.3 or newer, MaLiLib 0.28.12 or newer for 26.1.2,
  and Litematica 0.27.14 or newer for 26.1.2 alongside the printer.

## Port source

The port adapts the Minecraft 26.2 implementation from
[EvModder/litematica-printer](https://github.com/EvModder/litematica-printer),
commit `16c222b`, which retains IceTank's AirPlace, Grim rotation, inventory
management, and FreeLook, and fixes offhand synchronization and FreeLook compatibility.
The build targets 26.1.2 and uses its matching MaLiLib/Litematica dependencies.

## Installation

1. Download and install [Fabric](https://fabricmc.net/use/installer/) if you haven't already.
2. Download the latest Litematica Printer release for your Minecraft version from the
   [releases page](https://github.com/aleksilassila/litematica-printing/releases/latest) (The files can be found under
   "assets").
3. Download [Litematica + MaLiLib](https://www.curseforge.com/minecraft/mc-mods/litematica)
   and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/) (≠ Fabric).
4. Place the downloaded .jar files in your `mods/` folder.

[If this is the first fabric mod you are installing, here's an informative video on how to install Fabric mods.](https://www.youtube.com/watch?v=x7gmfib4gHg)

# How To Use

Using the printer is straightforward: You can toggle the feature by pressing `CAPS_LOCK` by default. To configure
variables such as printing speed and range, open Litematica's settings by pressing `M + C` and navigate to "Generic" 
tab. Printer's configuration can be found at the bottom of the page. All printer settings start with the word 'printer'. 
You can also rebind the printing toggle under "Hotkeys" tab. Holding down `V` by default will also print regardless if 
the printer is toggled on or off. All printer settings are designed to work on 2b2t out of the box. Some variations for
setting are listed below.

## How to use - Grim bypass setup
This configuration uses a grim bypass to place blocks in the correct orientation and without having to rotate the player.
This setup is recommended as long as it still works.

### Mods
- Install printer for your version. You need at least **3.3.17** or newer, or it won't work correctly.
  Download: https://github.com/IceTank/litematica-printer/releases

### Printer Settings
Most important settings:
- Printer range 4.5 - 5
- printerRotatePlayer false
- printerGrimRotate true (may not work on 1.20.4+ clients, if you get lagbag disable and use the settings from the Strict anticheat setup section)
- printerAirPlace true
- For the rest default values should work

## How to use - Grim rotations bypass
This configuration uses a grim bug in combination with via version to allow for placing blocks without having to rotate 
the player. Blocks placed in this way won't have the correct rotations unless the player rotation is correct.

### Mods
- Install printer for your version. You need at least 3.3.16 or newer, or it won't work correctly on 2b2t.
  Download: https://github.com/IceTank/litematica-printer/releases
- You also need ViaVersion Plus if you are not using 1.20.4

### In Game
- Before joining 2b you need to set viaversion plus to use version 1.20.6 or lower
### Printer Settings
Most important settings:
- Printer range at 4.5 - 4.3
- printerRotatePlayer false
- printerGrimRotate true
- For the rest default values should work

## How to use - Strict anticheat setup

This configuration setup is designed to work as vanilla as possible. If all other bypasses get patched this setup should still work.

### Mods
- Install printer for your version. You need at least 3.3.16 or newer, or it won't work correctly on 2b2t.
  Download: https://github.com/IceTank/litematica-printer/releases

### In Game - Printer Settings
Most important settings:
- Set freelook toggle to the same key as printer toggle
- Printer range at 4.5 - 4.3
- printerRotatePlayer true
- printerGrimRotate false
- For the rest default values should work

## Recommended Mapart settings
- printerIgnoreRotations true (this will work for most blocks. But be aware some logs have a different color on maps depending on the rotation.)

### Other features added by this fork:
- Airplace (printerAirPlace). A bit buggy but turning up printerTickDelay makes it more reliable.
- Working inventory management. The default litematica printer has issues on 2b with inventory management. This fork fixes that.
- autoConvertSchematicToLitematicOnLoad. This setting auto converts schematic files to litematic files when loading them. 
    This is useful when using schematics produced by Rebanes Mapartcraft Website [text](https://rebane2001.com/mapartcraft/).
    Without a litematica file baritone won't work with the `#litematica` command

## Issues

This is a 2b2t specific fork of printer and might not work on other anarchy servers besides 2b2t. If you have issues 
with the printer, **do not** bother the original creator ofLitematica (maruohon) with them.
If you have issues with printer on 2b2t you can message me on discord `@icetank` and we can try and figure out your config issue.

### List of know issues

Currently, the following features are still broken or missing:

- Placing liquids (printing **in** liquids works though)
- Current algorithm for placing rails isn't perfect,
  sometimes it can't place all the rails (to avoid placing anything incorrectly).
- Sometimes rotations on blocks are wrong even with printerIgnoreRotations off
- You might get some ghost items in your inventory
- In rare cases the printer might place the wrong block. This is usually a 1 in about 1000 chance. Can get worse with high ping or unstable connections.

Also, I have decided that features that fix existing builds,
such as automatic excavation or correcting incorrectly placed blocks are out of the scope of this mod.

## Building and Contributing

Use JDK 25 and run `bash ./gradlew :v26_1_2:build`.
