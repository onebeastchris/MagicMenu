package net.onebeastchris.extension.emotecommandmenu.util;

import net.onebeastchris.extension.emotecommandmenu.EmoteCommandMenu;
import net.onebeastchris.extension.emotecommandmenu.config.Config;
import org.geysermc.cumulus.component.DropdownComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuHandler {
    public static void sendForm(GeyserSession session, Config.FormDefinition formDefinition) {

        if (formDefinition.command() != null) {
            executeCommand(session, Optional.empty(), formDefinition.command());
            return;
        }

        Map<String, Config.Button> temp = new HashMap<>();

        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(formDefinition.title());

        if (formDefinition.description() != null && !formDefinition.description().isEmpty()) {
            simpleForm.content(formDefinition.description());
        }

        for (Config.Button button : formDefinition.buttons()) {
            if (button.imageUrl() == null || button.imageUrl().isEmpty()) {
                simpleForm.button(button.name());
            } else {
                simpleForm.button(button.name(), FormImage.Type.URL, button.imageUrl());
            }
            temp.put(button.name(), button);
        }

        simpleForm.validResultHandler((form, response) -> {
            Config.Button commandsHolder = temp.get(response.clickedButton().text());
            showButtonForm(session, form, commandsHolder);
            temp.clear();
        });

        simpleForm.invalidResultHandler((form, response) -> {
            temp.clear();
            if (EmoteCommandMenu.getConfig().goBackOnClosed()) {
                session.sendForm(simpleForm);
            }
        });
        session.sendForm(simpleForm);
    }

    private static void showButtonForm(GeyserSession session, SimpleForm form, Config.Button button) {
        if (button.command() != null) {
            executeCommand(session, Optional.empty(), button.command());
            return;
        }

        Map<String, Config.CommandHolder> temp = new HashMap<>();
        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(button.name())
                .content(button.description());

        for (Config.CommandHolder commandHolder : button.commandHolders()) {
            if (commandHolder.command().isEmpty()) {
                EmoteCommandMenu.getLogger().error(commandHolder.toString() + " is invalid, has no command!");
            } else {
                String name = commandHolder.name();
                if (commandHolder.imageUrl() != null && !commandHolder.imageUrl().isEmpty()) {
                    simpleForm.button(name, FormImage.Type.URL, commandHolder.imageUrl());
                } else {
                    simpleForm.button(name);
                }
                temp.put(name, commandHolder);
            }
        }

        simpleForm.validResultHandler((buttonform, response) -> {
            executeCommand(session, Optional.of(buttonform), temp.get(response.clickedButton().text()));
            temp.clear();
        });

        simpleForm.invalidResultHandler((buttonform, response) -> {
            temp.clear();
            if (EmoteCommandMenu.getConfig().goBackOnClosed()) {
                session.sendForm(simpleForm);
            }
        });
        session.sendForm(simpleForm);
    }

    private static void executeCommand(GeyserSession session, Optional<SimpleForm> optionalSimpleForm, Config.CommandHolder commandHolder) {
        if (commandHolder.command().isEmpty()) {
            EmoteCommandMenu.getLogger().error(commandHolder.toString() + " is invalid, has no command!");
            return;
        }
        if (commandHolder.command().contains("%")) {

            CustomForm.Builder customForm = CustomForm.builder()
                    .title(commandHolder.name());

            // custom inputs pls
            Pattern pattern = Pattern.compile("!%(.*?)%");
            Matcher matcher = pattern.matcher(commandHolder.command());

            List<String> placeholders = new ArrayList<>();
            List<String> defaultValues = new ArrayList<>();

            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }

            for (String placeholder : placeholders) {
                String[] splitString = placeholder.split(":");
                switch (splitString[0]) {
                    case "input" -> {
                        String[] splitInput = splitString[1].split(",");
                        if (splitInput.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid input placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(splitInput[1]);
                        customForm.input(splitInput[0], splitInput[1]);
                    }
                    case "toggle" -> {
                        String[] splitToggle = splitString[1].split(",");
                        if (splitToggle.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid toggle placeholder: " + placeholder);
                            return;
                        }
                        boolean defaultValue = Boolean.parseBoolean(splitToggle[1]);
                        defaultValues.add(String.valueOf(defaultValue));
                        customForm.toggle(splitToggle[0], defaultValue);
                    }
                    case "dropdown" -> {
                        String[] splitDropdown = splitString[1].split(",");
                        if (splitDropdown.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid dropdown placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(splitDropdown[1]);
                        //fuck me. really? why....
                        //TODO - set default
                        customForm.dropdown(splitDropdown[0], Arrays.copyOfRange(splitDropdown, 2, splitDropdown.length));
                    }
                    case "slider" -> {
                        String[] splitSlider = splitString[1].split(",");
                        if (splitSlider.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid slider placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(splitSlider[4]);
                        customForm.slider(splitSlider[0],
                                Float.parseFloat(splitSlider[1]),
                                Float.parseFloat(splitSlider[2]),
                                Float.parseFloat(splitSlider[3]),
                                Float.parseFloat(splitSlider[4]));
                    }
                    case "step-slider" -> {
                        String[] splitSlider = splitString[1].split(",");
                        if (splitSlider.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid slider placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(splitSlider[1]);
                        customForm.stepSlider(splitSlider[0], Integer.parseInt(splitSlider[1]), Arrays.copyOfRange(splitSlider, 2, splitSlider.length));
                    }
                }

                AtomicReference<String> commandToSend = new AtomicReference<>(commandHolder.command());

                customForm.invalidResultHandler((form, response) -> {
                    if (EmoteCommandMenu.getConfig().goBackOnClosed()) {
                        session.sendForm(customForm);
                    }
                    defaultValues.clear();
                    placeholders.clear();
                });

                customForm.validResultHandler((form, response) -> {
                    for (int i = 0; i < placeholders.size(); i++) {
                        var value = response.valueAt(i);
                        if (value != null && !value.toString().isEmpty()) {
                            if (form.content().get(i) instanceof DropdownComponent dropdownComponent) {
                                    value = dropdownComponent.text();
                            }
                            EmoteCommandMenu.getLogger().info("Replacing " + placeholders.get(i) + " with " + value);
                            commandToSend.set(commandToSend.get().replaceFirst("!%" + placeholders.get(i) + "%", String.valueOf(value)));
                        } else {
                            EmoteCommandMenu.getLogger().info("Replacing " + placeholders.get(i) + " with " + defaultValues.get(i));
                            commandToSend.set(commandToSend.get().replaceFirst("!%" + placeholders.get(i) + "%", defaultValues.get(i)));
                        }
                    }
                    EmoteCommandMenu.getLogger().info("Sending command: " + commandToSend.get());
                    session.sendCommand(commandToSend.get());

                });
            }
            session.sendForm(customForm);
        } else {
            session.sendCommand(commandHolder.command());
        }

    }
}
