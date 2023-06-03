# MagicMenu

A Geyser Extension that allows you to run commnds by using an emote (or command). Has support for various placeholders, and allows for user input on parts of the commands. 
Fully configurable! Define multiple forms (different ones for different players), chain them to one another, and even add images to buttons so they're more lively!

Showcase: 
https://www.youtube.com/watch?v=_n-IL6YiIbQ

examples:
![image](https://github.com/onebeastchris/MagicMenu/assets/105284508/adda4857-afce-40ff-9729-6758dcb736a3)
![image](https://github.com/onebeastchris/MagicMenu/assets/105284508/98edc067-bf27-4d7e-bccb-7938302485aa)
![image](https://github.com/onebeastchris/MagicMenu/assets/105284508/f06621ff-23be-4e8d-8444-7f64de77b7ad)

### This is an extension for Geyser; NOT a plugin/mod. To install it, drop the .jar file in Geyser's extensions folder.

For help with this project: https://discord.gg/WdmrRHRJhS

Download available in the releases tab.

## DISCLAIMER: While this project is made to work with Geyser (literally a geyser extension), it is not an official one - for help, ask in issues here or on the linked discord.

#### IMPORTANT:
- The config is in YAML format, so make sure you don't mess up the indentation.
- This will only work if 'emote-offhand-workaround' in Geysers config is set to either emotes-and-offhand or disabled.

#### Configuration:
There are main "elements":
- menus: to run show forms/commands for different emotes/commands. Or both!
- forms: to show different buttons, with or without images
- buttons: When clicked, they can execute commands, or show another form
- command holders: Hold the command, the name for it, the users allowed to run it, and the command to run them (with placeholder/player input placeholders support)

For more info on how to set up the config, check out the default config below.

List of all available [Placeholders](https://github.com/onebeastchris/MagicMenu/blob/master/setup.md) <br>
The [Default/Example Config](https://github.com/onebeastchris/MagicMenu/blob/master/src/main/resources/config.yml) for reference
