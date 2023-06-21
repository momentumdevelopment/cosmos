![minecraft](https://img.shields.io/badge/Minecraft-1.12.2-blue.svg)
![GitHub All Releases](https://img.shields.io/github/downloads/linustouchtips/cosmos/total?color=purple)
[![discord](https://img.shields.io/badge/Discord-JK2Zz2CDpM-8080c0)](https://discord.gg/JK2Zz2CDpM)
![minecraft](https://img.shields.io/badge/Key--bind-Right--shift-brightgreen)
![minecraft](https://img.shields.io/badge/Client--Prefix-*-blueviolet)
![logo](https://github.com/momentumdevelopment/cosmos/blob/main/src/main/resources/assets/cosmos/textures/imgs/logotransparent.png)

Cosmos is a free, open-source, Minecraft 1.12.2 Forge PvP Client aimed at the anarchy community. The client is no longer updated.

## Usage:
- Download the .jar file from the [releases](https://github.com/momentumdevelopment/cosmos/releases/) tab
- Put the .jar in your "mods" folder (Create one if it does not exist)
    - Windows: Press `Windows + R` and type in `%appdata%` open the `.minecraft/mods` folder.
    - MacOS: Open Finder and open the `minecraft/mods` folder.
- Download and install [Forge 1.12.2 2855](https://adfoc.us/serve/sitelinks/?id=271228&url=https://maven.minecraftforge.net/net/minecraftforge/forge/1.12.2-14.23.5.2855/forge-1.12.2-14.23.5.2855-installer.jar)
- Press `RSHIFT` to open the ClickGUI
- The command prefix is `*`

## Setting up:
- Run `gradlew setupDecompWorkspace` in terminal.
- Refresh Gradle.

## Debugging:
- Run `gradlew genIntelliJRuns` in terminal and then launch as Minecraft Client.

## Building:
- Run `gradlew build` in terminal and you will find your built jar in build/libs.
- Note: Building directly from the main branch might produce a build that is not functional or bugged, this is due to the fact that the developers use this branch when adding, modifying, or testing features, for the best experience either build from tags of previous releases or download the jar file from the releases tab.

## Licensing:
This project is under the [GPL-3.0 license](https://www.gnu.org/licenses/gpl-3.0.en.html) meaning that you **must** disclose the source code of the project you are adding our code into. The project you are working on must also be under the GPL-3.0 license. We all like open source, but give credit where credit is due.
