package net.onebeastchris.extension.magicmenu.util;

import net.onebeastchris.extension.magicmenu.MagicMenu;
import net.onebeastchris.extension.magicmenu.config.Config;
import org.geysermc.cumulus.component.Component;
import org.geysermc.cumulus.component.DropdownComponent;
import org.geysermc.cumulus.component.StepSliderComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuHandler {

    static CompletableFuture<Config.Button> mainForm(GeyserConnection connection, Config.Form formDefinition) {

        Map<String, Config.Button> temp = new HashMap<>();

        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(formDefinition.title());

        if (formDefinition.description() != null && !formDefinition.description().isEmpty()) {
            simpleForm.content(formDefinition.description());
        }

        for (Config.Button button : formDefinition.buttons()) {
            if (PlayerMenuHandler.hasPerms(button.allowedUsers(), connection.bedrockUsername())) {
                if (button.imageUrl() == null || button.imageUrl().isEmpty()) {
                    simpleForm.button(button.name());
                } else {
                    simpleForm.button(button.name(), FormImage.Type.URL, button.imageUrl());
                }
                temp.put(button.name(), button);
            }
        }

        CompletableFuture<Config.Button> futureResult = new CompletableFuture<>();

        simpleForm.validResultHandler((form, response) -> {
            Config.Button button = temp.get(response.clickedButton().text());
            temp.clear();
            futureResult.complete(button);
        });

        simpleForm.invalidResultHandler((form, response) ->
        {
            temp.clear();
            futureResult.complete(null);
        });
        connection.sendForm(simpleForm);

        return futureResult;
    }

    static CompletableFuture<Config.CommandHolder> showButtonForm(GeyserConnection connection, Config.Button button) {

        Map<String, Config.CommandHolder> temp = new HashMap<>();
        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(button.name())
                .content(button.description());

        for (Config.CommandHolder commandHolder : button.commandHolders()) {
            if (commandHolder.command().isEmpty()) {
                MagicMenu.getLogger().error(commandHolder + " is invalid, has no command!");
            } else if (PlayerMenuHandler.hasPerms(commandHolder.allowedUsers(), connection.bedrockUsername())) {
                String name = commandHolder.name();
                if (commandHolder.imageUrl() != null && !commandHolder.imageUrl().isEmpty()) {
                    simpleForm.button(name, FormImage.Type.URL, commandHolder.imageUrl());
                } else {
                    simpleForm.button(name);
                }
                temp.put(name, commandHolder);
            }
        }

        CompletableFuture<Config.CommandHolder> completableFuture = new CompletableFuture<>();

        simpleForm.validResultHandler((buttonform, response) -> {
            Config.CommandHolder holder = temp.get(response.clickedButton().text());
            temp.clear();
            completableFuture.complete(holder);
        });

        simpleForm.closedOrInvalidResultHandler((buttonform, response) -> {
            temp.clear();
            completableFuture.complete(null);
        });
        connection.sendForm(simpleForm);
        return completableFuture;
    }

    static RestultType executeCommand(GeyserConnection connection, Config.CommandHolder commandHolder) {
        if (commandHolder.command().isEmpty()) {
            MagicMenu.getLogger().error(commandHolder + " is invalid, has no command!");
            return RestultType.FAILURE;
        }
        if (!PlayerMenuHandler.hasPerms(commandHolder.allowedUsers(), connection.bedrockUsername())) {
            // only way i see this happening would be a weird config, where a button is for all,
            // but the command behind it locked...
            return RestultType.CANCELLED;
        }

        // unholy, yes, but api is way too limited.
        GeyserSession session = (GeyserSession) connection;

        if (commandHolder.command().contains("%")) {
            String command = commandHolder.command();

            command.replace("%username%", connection.javaUsername());
            command.replace("%xuid%", connection.xuid());
            command.replace("%bedrockusername%", connection.bedrockUsername());
            command.replace("%device%", connection.inputMode().name());
            command.replace("%lang%", connection.languageCode());
            command.replace("%x%", String.valueOf(session.getPlayerEntity().getPosition().getX()));
            command.replace("%y%", String.valueOf(session.getPlayerEntity().getPosition().getY()));
            command.replace("%z%", String.valueOf(session.getPlayerEntity().getPosition().getZ()));
            command.replace("%location%", session.getPlayerEntity().getPosition().toString());

            Pattern pattern_input = Pattern.compile("!%(.*?)%");
            Matcher matcher = pattern_input.matcher(command);

            List<String> placeholders = new ArrayList<>();
            List<String> defaultValues = new ArrayList<>();

            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }

            CustomForm.Builder customForm = CustomForm.builder()
                    .title(commandHolder.name());

            for (String placeholder : placeholders) {
                String[] splitString = placeholder.split(":");

                String type = splitString[0];
                String[] options = splitString[1].split(", ");
                switch (type) {
                    case "input" -> {
                        if (options.length < 2) {
                            MagicMenu.getLogger().error("Invalid input placeholder: " + placeholder + " in " + commandHolder.name());
                            return RestultType.FAILURE;
                        }
                        defaultValues.add(options[1]);
                        customForm.input(options[0], options[1]);
                    }
                    case "toggle" -> {
                        if (options.length < 2) {
                            MagicMenu.getLogger().error("Invalid toggle placeholder: " + placeholder + " in " + commandHolder.name());
                            return RestultType.FAILURE;
                        }
                        boolean defaultValue = Boolean.parseBoolean(options[1]);
                        defaultValues.add(String.valueOf(defaultValue));
                        customForm.toggle(options[0], defaultValue);
                    }
                    case "dropdown" -> {
                        if (options.length < 4) {
                            MagicMenu.getLogger().error("Invalid dropdown placeholder: " + placeholder + " in " + commandHolder.name());
                            return RestultType.FAILURE;
                        }
                        String defaultValue = options[1];
                        defaultValues.add(defaultValue);
                        int index = 0;
                        String[] dropdownOptions = Arrays.copyOfRange(options, 2, options.length);
                        // Find the index of the default value
                        for (int i = 0; i < dropdownOptions.length; i++) {
                            if (options[i].equals(defaultValue)) {
                                index = i;
                                break;
                            }
                        }
                        customForm.dropdown(options[0], index, dropdownOptions);

                    }
                    case "slider" -> {
                        if (options.length < 5) {
                            MagicMenu.getLogger().error("Invalid slider placeholder: " + placeholder + " in " + commandHolder.name());
                            return RestultType.FAILURE;
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
                            MagicMenu.getLogger().error("Invalid slider placeholder: " + placeholder + " in " + commandHolder.name());
                            return RestultType.FAILURE;
                        }
                        defaultValues.add(options[1]);
                        customForm.stepSlider(options[0], Integer.parseInt(options[1].trim()), Arrays.copyOfRange(options, 2, options.length));
                    }
                }
            }
            AtomicReference<String> commandToSend = new AtomicReference<>(command);
            AtomicReference<RestultType> result = new AtomicReference<>(RestultType.SUCCESS);

            customForm.closedOrInvalidResultHandler((form, response) -> {
                defaultValues.clear();
                placeholders.clear();
                MagicMenu.getLogger().info("setting cancelled");
                result.set(RestultType.CANCELLED);
            });

            customForm.validResultHandler((form, response) -> {
                for (int i = 0; i < placeholders.size(); i++) {

                    String replace = placeholders.get(i);
                    String defaultValue = defaultValues.get(i);
                    var value = response.valueAt(i);

                    if (value != null && !value.toString().isEmpty()) {
                        Component component = form.content().get(i);
                        // special handling for dropdowns, and sliders.

                        switch (Objects.requireNonNull(component).type()) {
                            case DROPDOWN -> // we want to get the text, not the index
                                    value = ((DropdownComponent) component).options().get(response.asDropdown(i));
                            case SLIDER -> // we want an int, not float
                                    value = String.valueOf(value).replace(".0", "");
                            case STEP_SLIDER -> // we want to get text, not the index
                                    value = ((StepSliderComponent) component).steps().get(response.asStepSlider(i));
                        }
                        MagicMenu.getLogger().info("Replacing: " + replace + " with " + value);
                        commandToSend.set(commandToSend.get().replaceFirst("!%" + replace + "%", String.valueOf(value)));
                    } else {
                        MagicMenu.getLogger().info("Replacing: " + replace + " with " + defaultValue);
                        commandToSend.set(commandToSend.get().replaceFirst("!%" + replace + "%", defaultValue));
                    }
                }
                MagicMenu.getLogger().info("Sending command: " + commandToSend.get());
                session.sendCommand(commandToSend.get());

                defaultValues.clear();
                placeholders.clear();
                MagicMenu.getLogger().info("setting success return");
                result.set(RestultType.SUCCESS);
            });
            connection.sendForm(customForm);
            MagicMenu.getLogger().info(result.get().name());
            MagicMenu.getLogger().info("executeCommand: " + result.get());
            return result.get();
        } else {
            session.sendCommand(commandHolder.command());

            return RestultType.SUCCESS;
        }

    }

    enum RestultType {
        SUCCESS,
        FAILURE,
        CANCELLED
    }
}
