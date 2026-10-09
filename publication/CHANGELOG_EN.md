# Public Connector alpha 1.10.alpha.neoforge.1

Initial public branch for Minecraft 1.21.1, distributed as a Fabric-format JAR for NeoForge through Sinytra Connector.

- Removed private axolotl wearable collars, modifier bonuses, bell sounds/physics, outfits, altered face and personal LEA control packets.
- Restored upstream axolotl geometry, textures and color masks; original creature forms and gameplay accessory items remain.
- Kept the fluid, moisture synchronization, vertical swimming, chained jump prediction, air speed and NeoForge critical-hit compatibility fixes.
- Added the original mod avatar with an official NeoForged badge, upstream attribution, dependency instructions and source launchers.
- Supported the public alpha version label in Origins/Apoli initialization and version handshakes.
- Built with Java 21. Gradle build/check succeeded. A dedicated NeoForge 21.1.248 + Connector beta.17 + Forgified Fabric API 0.116.15 test passed 693 checks and registered 524 powers.

This first public build is marked alpha. Tests cover definitions, serialization and selected gameplay paths; every ability in every possible modpack was not individually tested. Both client and server need the mod and the listed dependencies. Do not combine it with another SSC port.

Code: MIT. Media: CC BY-NC 4.0. Original authors and the 1.21.1 port base are credited in README.md and LICENSING.md.