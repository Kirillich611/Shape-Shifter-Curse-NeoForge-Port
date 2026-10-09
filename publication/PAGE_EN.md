# Shape Shifter Curse - NeoForge Port

A reworked version of [wuhenqiubai's unofficial Minecraft 1.21.1 port](https://github.com/wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port) of **Shape Shifter Curse**, focused on compatibility with **NeoForge 1.21.1 through Sinytra Connector**.

The upstream Fabric port can also be launched on NeoForge through Connector, but our testing encountered important mechanics that did not work or behaved incorrectly in that environment. This branch reworks those compatibility paths to bring their behavior closer to the original mod.

Although this mod is built in **Fabric format**, its supported and tested runtime is **NeoForge with Sinytra Connector**. Direct Fabric use is not supported by this branch.

## Which port should I use?

- **NeoForge 1.21.1:** use this port together with Sinytra Connector and the dependencies below.
- **Fabric 1.21.1:** use [wuhenqiubai's unofficial port](https://www.curseforge.com/minecraft/mc-mods/shape-shifter-curse-unofficial-port).

Choose one SSC port for your installation; do not install both together.

## What the mod does

Gradually transform into Minecraft creatures with custom models, animations and form-specific abilities. Progress through transformation stages and discover how each form changes movement, survival and combat.

## Installation

Install the mod on both the client and server. Use Java 21 and Minecraft 1.21.1.

Tested compatibility stack:
- NeoForge 21.1.248
- Sinytra Connector 2.0.0-beta.17+1.21.1
- Forgified Fabric API 0.116.15+2.3.5+1.21.1

Also install the compatible 1.21.1 versions of Pehkui, Satin, GeckoLib and Player Animation Library. Connector-compatible Fabric dependencies may be necessary where a native equivalent does not expose the required API. Cardinal Components, Apoli Legacy, Calio Legacy and Cloth Config components are bundled where specified in the JAR metadata.

Do not install this port alongside the original Shape Shifter Curse or another SSC port: they share mod identifiers and gameplay namespaces.

## Credits and licensing

Original mod: [onixary/shape-shifter-curse-fabric](https://github.com/onixary/shape-shifter-curse-fabric), by onixary, XuHaoNan and contributors.

Minecraft 1.21.1 port base: [wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port](https://github.com/wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port).

NeoForge/Connector compatibility maintenance: [Kirillich611](https://github.com/Kirillich611).

Code is MIT. Original models, textures, animations, sounds and other media are CC BY-NC 4.0; see [Licensing](https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/blob/public-connector/LICENSING.md) and [Third-party notices](https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/blob/public-connector/THIRD-PARTY-NOTICES.md). Keep upstream attribution. This mixed-license distribution is for non-commercial use. The avatar adapts the original mod icon by adding the official NeoForged organization icon as a compatibility badge; it does not indicate endorsement.

## Reporting issues

Report issues at https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector/issues with the mod version, loader/Connector versions, form and stage, reproduction steps and latest.log.