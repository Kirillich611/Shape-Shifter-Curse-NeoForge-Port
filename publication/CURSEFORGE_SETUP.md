# CurseForge publication package

New project creation requires the Author Console: https://authors.curseforge.com/#/projects/create/choose-game

- Account: Kirillich611
- Game / class: Minecraft / Mods
- Title: Shape Shifter Curse - NeoForge Connector
- Summary: Unofficial Minecraft 1.21.1 Shape Shifter Curse port for NeoForge through Sinytra Connector.
- Description: PAGE_EN.md in this directory
- Logo: icon-512.png in this directory
- Source and issues: https://github.com/Kirillich611/Shape-Shifter-Curse-NeoForge-Connector
- License: Custom mixed license, code MIT and media CC BY-NC 4.0. Use the text and attribution in LICENSING.md; disable rewards/monetization for the non-commercial media distribution.
- Categories: Adventure and RPG, Mobs (as available in the console)
- Environment: client and server; Minecraft 1.21.1; NeoForge with Sinytra Connector and Forgified Fabric API

After the owner provides the new project ID, upload with the prepared author-token script:

`powershell -NoProfile -ExecutionPolicy Bypass -File C:\Modds\tools\Upload-SSC-Public-CurseForge.ps1 -ProjectId <ID>`

Credentials remain outside the repository. No CurseForge page or file upload is claimed until the service returns the corresponding project/file ID.