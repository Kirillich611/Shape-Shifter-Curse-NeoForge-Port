# Public build verification

Version 1.11.19+1.21.1; Minecraft 1.21.1; Fabric-format artifact for NeoForge through Connector.

- Gradle `build --offline`: passed (28 tasks); configured `check` tasks passed.
- Isolated dedicated server: NeoForge 21.1.248, Connector 2.0.0-beta.17+1.21.1, Forgified Fabric API 0.116.15+2.3.5+1.21.1. Native smoke suite: 693 checks, 524 registered powers, passed.
- Test fixture changes: expire default cooldowns in a fresh world and separate knockback actor/target positions. No game code was changed for those fixture corrections.
- Packaged JAR inspected: version matches, original axolotl geometry is restored, private cosmetic classes, textures and credential files are absent.
- Original geometric models and axolotl base textures/color masks were copied byte-for-byte from the preserved 1.20.1 source. Existing 1.21.1 resource namespace mappings are retained.
- Source publication contains no credential storage, signing tools, private keys, run directories or local personal snapshots.

Every ability and every optional integration was not exercised separately in game. Development launchers use Fabric Loom, while compatibility assertions used NeoForge/Connector. See publication/CHANGELOG_EN.md for installation and release limits.