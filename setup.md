# Configuring

### Available placeholder:

| placeholder       | description                             | example          |
|-------------------|-----------------------------------------|------------------|
| %username%        | username as used on the java server     | .onebeastofchris |
| %uuid%            | UUID as used on the Java server         |                  |
| %xuid%            | xbox uuid of the Bedrock account        |                  |
| %bedrockusername% | Bedrock username of the Bedrock account | onebeastofchris  |
| %version%         | Displays the Bedrock client version     | 1.19.83          |
| %device%          | Shows the DeviceOS of the player        | Windows          |
| %lang%            | shows the language code for the player  | DE_de            |
| %x%<br>%y%<br>%z% | One of the coordinates of the player    | 69               |
| %position%        | The x y z coords of the player          | 69 69 69         |

If you have ideas for more placeholders - let me know on my discord server:
https://discord.gg/NrUwZuXD


### Advanced: Input placeholders

These can be used to run custom commands:
The player can choose the replacement themselves.
If no replacement is chosen, a default value will be run.

Example: '!%toggle: ExampleToggle, true%'
Here, when used in a command, the entire value would be replaced with "true", or "false".
The first value, ExampleToggle, is the name of the toggle, "true" is the default value.

The setup is always the same:
The placeholder is surrounded by '!%..:...%'. 
Infront of the ":", the type is defined.
After it, seperated by commas, all needed arguments (default, other values)

Full list:
| Type        | returns..                                                                                                                                                              | Arguments                            | Image | Example                                                              |
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|-------|----------------------------------------------------------------------|
| Input       | Text, numbers, words                                                                                                                                                   | input:name, defaultvalue             |       | !%input:Name, chris%                                                 |
| Toggle      | a boolean: true or false                                                                                                                                               | toggle:name, default                 |       | !%toggle:Show names?, true%                                          |
| Dropdown    | One of the presets. Text, numbers.. anything<br>Note: default must be included twice!<br>Once in second place, once where desired<br>Can have (way) more than 3 steps. | dropdown:name, default, a,b,default  |       | !%dropdown:Select gamemode, survival, adventure, creative, survival% |
| Slider      | A number between a range.                                                                                                                                              | slider:name, min, max, step, default |       | !%slider:Fav number?, 0, 10, 1, 3%                                   |
| Step-Slider | One of the set steps. Can be text, number..<br>Note: Can have (way) more than 3 steps.                                                                                 | step-slider:name, default, a, b, c   |       | !%step-slider:Fav color?, green, green, red, yellow, pink%           |

