package net.onebeastchris.extension.emotecommandmenu.util;

import net.onebeastchris.extension.emotecommandmenu.EmoteCommandMenu;
import net.onebeastchris.extension.emotecommandmenu.config.Config;
import org.geysermc.cumulus.component.DropdownComponent;
import org.geysermc.cumulus.component.SliderComponent;
import org.geysermc.cumulus.component.StepSliderComponent;
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

        simpleForm.closedOrInvalidResultHandler((buttonform, response) -> {
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

            Pattern pattern = Pattern.compile("!%(.*?)%");
            Matcher matcher = pattern.matcher(commandHolder.command());

            List<String> placeholders = new ArrayList<>();
            List<String> defaultValues = new ArrayList<>();

            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }

            for (String placeholder : placeholders) {
                String[] splitString = placeholder.split(":");

                String type = splitString[0];
                splitString[1].replace(", ", ",");
                String[] options = splitString[1].split(",");
                switch (type) {
                    case "input" -> {
                        if (options.length < 2) {
                            EmoteCommandMenu.getLogger().error("Invalid input placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(options[1]);
                        customForm.input(options[0], options[1]);
                    }
                    case "toggle" -> {
                        if (options.length < 2) {
                            EmoteCommandMenu.getLogger().error("Invalid toggle placeholder: " + placeholder);
                            return;
                        }
                        boolean defaultValue = Boolean.parseBoolean(options[1]);
                        defaultValues.add(String.valueOf(defaultValue));
                        customForm.toggle(options[0], defaultValue);
                    }
                    case "dropdown" -> {
                        if (options.length < 4) {
                            EmoteCommandMenu.getLogger().error("Invalid dropdown placeholder: " + placeholder);
                            return;
                        }
                        String defaultValue = options[1];
                        defaultValues.add(defaultValue);
                        int index = 0;
                        String[] dropdownOptions = Arrays.copyOfRange(options, 2, options.length);
                        // Find the index of the default value
                        for (int i = 0; i < dropdownOptions.length; i++) {
                            if (options[i].equals(defaultValue)) {
                                index = i;
                                customForm.dropdown(options[0], index, dropdownOptions);
                                break;
                            }
                        }
                    }
                    case "slider" -> {
                        if (options.length < 5) {
                            EmoteCommandMenu.getLogger().error("Invalid slider placeholder: " + placeholder);
                            return;
                        }
                        String text = options[0];

                        for (int i = 1; i < options.length; i++) {
                            // fix for slider values with spaces. bad.
                            options[i] = options[i].replace(" ", "");
                        }

                        defaultValues.add(options[4]);
                        customForm.slider(text,
                                Float.parseFloat(options[1]),
                                Float.parseFloat(options[2]),
                                Float.parseFloat(options[3]),
                                Float.parseFloat(options[4]));
                    }
                    case "step-slider" -> {
                        if (options.length < 1) {
                            EmoteCommandMenu.getLogger().error("Invalid slider placeholder: " + placeholder);
                            return;
                        }
                        defaultValues.add(options[1]);
                        customForm.stepSlider(options[0], Integer.parseInt(options[1].trim()), Arrays.copyOfRange(options, 2, options.length));
                    }
                }

                AtomicReference<String> commandToSend = new AtomicReference<>(commandHolder.command());

                customForm.closedOrInvalidResultHandler((form, response) -> {
                    if (EmoteCommandMenu.getConfig().goBackOnClosed()) {
                        session.sendForm(customForm);
                    }
                    defaultValues.clear();
                    placeholders.clear();
                });

                customForm.validResultHandler((form, response) -> {
                    for (int i = 0; i < placeholders.size(); i++) {

                        String replace = placeholders.get(i);
                        String defaultValue = defaultValues.get(i);
                        var value = response.valueAt(i);

                        if (value != null && !value.toString().isEmpty()) {
                            var component = form.content().get(i);

                            // special handling for dropdowns, and sliders.
                            if (component instanceof DropdownComponent dropdownComponent) {
                                // we want to get the text, not the index
                                value = dropdownComponent.options().get(response.asDropdown(i));
                            } else if (component instanceof SliderComponent) {
                                // we want an int, not float
                                value.toString().replace(".0", "");
                            } else if (component instanceof StepSliderComponent stepSliderComponent) {
                                // we want to get text, not the index
                                value = stepSliderComponent.steps().get(response.asStepSlider(i));
                            }
                            commandToSend.set(commandToSend.get().replaceFirst("!%" + replace + "%", String.valueOf(value)));
                        } else {
                            commandToSend.set(commandToSend.get().replaceFirst("!%" + replace + "%", defaultValue));
                        }
                    }
                    EmoteCommandMenu.getLogger().info("Sending command: " + commandToSend.get());
                    session.sendCommand(commandToSend.get());

                    defaultValues.clear();
                    placeholders.clear();
                });
            }
            session.sendForm(customForm);
        } else {
            session.sendCommand(commandHolder.command());
        }

    }
}
