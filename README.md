# Geyser Extension Template
A Geyser extension template to make making extensions easier.

## What can extensions do?
- register custom items (https://wiki.geysermc.org/geyser/custom-items/) in code
- soon: register custom blocks
- soon: send additional resource packs for a session
- use the [Geyser API](https://github.com/GeyserMC/Geyser/blob/master/api/src/main/java/org/geysermc/geyser/api/) to e.g. not show some commands to bedrock players
- send forms to bedrock players via [Cumulus](https://github.com/GeyserMC/Cumulus)
- more to be added - if you have suggestions, reach out to the GeyserMC discord: https://discord.gg/geysermc

## Existing extensions:
They're here as reference points to make your own, not all are operational.

- GeyserConnect: https://github.com/GeyserMC/GeyserConnect 
- MCXboxBroadcast: https://github.com/rtm516/MCXboxBroadcast
- HideCommands: https://github.com/Redned235/HideCommands
- per-player-packs (not yet operational, PR in Geyser still open): https://github.com/onebeastchris/packs-extension/
- Slimefun (not working atm): https://github.com/SofiaRedmond/Slimefun-Geyser
- EmoteCraft (used for the emotectaft mod): https://github.com/KosmX/geyser-emote-extension 

## Usage of this template:
1. Create a new repository using this template, make your extension - basic structure already exists
2. Run `./gradlew build` to build the extension
3. Copy the built jar from `build/libs` to your Geyser's extensions folder

Notes:
- extension.yml is required for Geyser to load the extension. It must be in the resources folder.
- Geyser Extensions: https://github.com/GeyserMC/Geyser/blob/master/api/src/main/java/org/geysermc/geyser/api/extension/Extension.java
- Geyser API docs: https://github.com/GeyserMC/Geyser/blob/master/api/src/main/java/org/geysermc/geyser/api/

