# Shape Shifter Curse - NeoForge Connector



An unofficial **Minecraft 1.21.1** compatibility port of **Shape Shifter Curse**, intended for **NeoForge with Sinytra Connector**. This is a Fabric-format mod: Connector is required on NeoForge.

## What the mod does

Gradually transform into Minecraft creatures with custom models, animations and form-specific abilities. Progress through transformation stages and discover how each form changes movement, survival and combat. The port preserves the original creature forms, including axolotl, bat, ocelot, spider and polar fox.

## Installation

Install the mod on both the client and server. Use Java 21 and Minecraft 1.21.1.

Tested compatibility stack:
- NeoForge 21.1.248
- Sinytra Connector 2.0.0-beta.17+1.21.1
- Forgified Fabric API 0.116.15+2.3.5+1.21.1

Also install the compatible 1.21.1 versions of Pehkui, Satin, GeckoLib and Player Animation Library. Connector-compatible Fabric dependencies may be necessary where a native equivalent does not expose the required API. Cardinal Components, Apoli Legacy, Calio Legacy and Cloth Config components are bundled where specified in the JAR metadata.

Do not install this port alongside the original Shape Shifter Curse or another SSC port: they share mod identifiers and gameplay namespaces.

## Compatibility fixes

This branch includes fixes for fluid handling, axolotl moisture synchronization and vertical swimming, jump prediction, polar fox air acceleration, chained jump momentum and critical damage under NeoForge. The axolotl water explosion uses Active Skill 1; water exit bursts require an upward-looking, rising exit.

The public branch restores the original axolotl model and textures. Private cosmetic control packets, custom wearable collars, bell physics, outfits and the personal LEA bridge are excluded. Original gameplay accessory items remain available.

## Credits and licensing

Original mod: [onixary/shape-shifter-curse-fabric](https://github.com/onixary/shape-shifter-curse-fabric), by onixary, XuHaoNan and contributors.

Minecraft 1.21.1 port base: [wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port](https://github.com/wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port).

NeoForge/Connector compatibility maintenance: [Kirillich611](https://github.com/Kirillich611).

Code is MIT. Original models, textures, animations, sounds and other media are CC BY-NC 4.0; see [Licensing](https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/blob/public-connector/LICENSING.md) and [Third-party notices](https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/blob/public-connector/THIRD-PARTY-NOTICES.md). Keep upstream attribution. This mixed-license distribution is for non-commercial use. The avatar adapts the original mod icon by adding the official NeoForged organization icon as a compatibility badge; it does not indicate endorsement.

## Development

Current public version: **1.10.alpha.neoforge.1**. Source branch: `public-connector`.

Build with Java 21: `gradlew.bat build`. On Windows, `Start-Server.cmd` runs the local development server in a console window and `Start-Client.cmd` runs the development client from the current source. These launchers use Fabric Loom; NeoForge/Connector verification uses a separate isolated server.

Report issues at https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/issues with the mod version, loader/Connector versions, form and stage, reproduction steps and latest.log.