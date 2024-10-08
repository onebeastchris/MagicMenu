package net.onebeastchris.extension.magicmenu.util;

import net.onebeastchris.extension.magicmenu.MagicMenu;
import net.onebeastchris.extension.magicmenu.config.Config;
import org.geysermc.cumulus.component.Component;
import org.geysermc.cumulus.component.DropdownComponent;
import org.geysermc.cumulus.component.StepSliderComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.util.PlatformType;
import org.geysermc.geyser.command.CommandRegistry;
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

    static CompletableFuture<Config.Button> sendForm(GeyserConnection connection, Config.Form formDefinition) {

        Map<String, Config.Button> temp = new HashMap<>();

        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(PlaceHolder.parsePlaceHolders(connection, formDefinition.title()));

        if (formDefinition.description() != null && !formDefinition.description().isEmpty()) {
            simpleForm.content(PlaceHolder.parsePlaceHolders(connection, formDefinition.description()));
        }

        for (Config.Button button : formDefinition.buttons()) {
            if (PlayerMenuHandler.hasPerms(button.allowedUsers(), connection.bedrockUsername())) {
                String name = PlaceHolder.parsePlaceHolders(connection, button.name());
                if (button.imageUrl() == null || button.imageUrl().isEmpty()) {
                    simpleForm.button(name);
                } else {
                    simpleForm.button(name, FormImage.Type.URL, PlaceHolder.parsePlaceHolders(connection, button.imageUrl()));
                }
                temp.put(name, button);
            }
        }

        CompletableFuture<Config.Button> futureResult = new CompletableFuture<>();

        simpleForm.validResultHandler((form, response) -> {
            Config.Button button = temp.get(response.clickedButton().text());
            temp.clear();
            MagicMenu.debug("Clicked button " + button.name() + " in form " + formDefinition.title() + " for " + connection.bedrockUsername());
            futureResult.complete(button);
        });

        simpleForm.closedOrInvalidResultHandler((form, response) -> {
            MagicMenu.debug("Invalid result for " + connection.bedrockUsername() + " in form " + formDefinition.title());
            temp.clear();
            futureResult.complete(null);
        });

        connection.sendForm(simpleForm);
        return futureResult;
    }

    static CompletableFuture<ResultType> executeCommand(GeyserConnection connection, Config.CommandHolder commandHolder) {
        CompletableFuture<ResultType> completableFuture = new CompletableFuture<>();
        if (commandHolder.command().isEmpty()) {
            MagicMenu.getLogger().error(commandHolder + " is invalid, has no command!");
            completableFuture.complete(ResultType.CANCELLED);
            return completableFuture;
        }

        if (!PlayerMenuHandler.hasPerms(commandHolder.allowedUsers(), connection.bedrockUsername())) {
            // Only way I see this happening would be a weird config where a button is for all,
            // but the command behind it is locked...
            MagicMenu.getLogger().error(commandHolder + " cannot be run by " + connection.bedrockUsername() + "!");
            completableFuture.complete(ResultType.CANCELLED);
            return completableFuture;
        }

        String command = PlaceHolder.parsePlaceHolders(connection, commandHolder.command());

        // skip input placeholder if none are present
        if (!command.contains("!%")) {
            sendCommand(connection, command);
            completableFuture.complete(ResultType.SUCCESS);
            return completableFuture;
        }

        Pattern pattern_input = Pattern.compile("!%(.*?)%");
        Matcher matcher = pattern_input.matcher(command);

        List<String> placeholders = new ArrayList<>();
        List<String> defaultValues = new ArrayList<>();

        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        if (placeholders.isEmpty()) {
            sendCommand(connection, command);
            completableFuture.complete(ResultType.SUCCESS);
            return completableFuture;
        }

        String name = PlaceHolder.parsePlaceHolders(connection, commandHolder.name());
        CustomForm.Builder formBuilder = CustomForm.builder()
                .title(name);

        for (String placeholder : placeholders) {
            String[] splitString = placeholder.split(":");

            String type = splitString[0];
            String[] options = splitString[1].split(", ");

            switch (type) {
                case "input" -> {
                    if (options.length < 2) {
                        MagicMenu.getLogger().error("Invalid input placeholder: " + placeholder + " in " + name);
                        completableFuture.complete(ResultType.FAILURE);
                        return completableFuture;
                    }
                    defaultValues.add(options[1]);
                    formBuilder.input(options[0], options[1]);
                }
                case "toggle" -> {
                    if (options.length < 2) {
                        MagicMenu.getLogger().error("Invalid toggle placeholder: " + placeholder + " in " + name);
                        completableFuture.complete(ResultType.FAILURE);
                        return completableFuture;
                    }
                    boolean defaultValue = Boolean.parseBoolean(options[1]);
                    defaultValues.add(String.valueOf(defaultValue));
                    formBuilder.toggle(options[0], defaultValue);
                }
                case "dropdown" -> {
                    if (options.length < 4) {
                        MagicMenu.getLogger().error("Invalid dropdown placeholder: " + placeholder + " in " + name);
                        completableFuture.complete(ResultType.FAILURE);
                        return completableFuture;
                    }
                    String dropdowndefault = options[1];
                    defaultValues.add(dropdowndefault);
                    String[] dropdownOptions = Arrays.copyOfRange(options, 2, options.length);
                    formBuilder.dropdown(options[0], getIndex(dropdownOptions, dropdowndefault), dropdownOptions);
                }
                case "slider" -> {
                    if (options.length < 5) {
                        MagicMenu.getLogger().error("Invalid slider placeholder: " + placeholder + " in " + name);
                        completableFuture.complete(ResultType.FAILURE);
                        return completableFuture;
                    }
                    String text = options[0];
                    for (int i = 1; i < options.length; i++) {
                        // Fix for slider values with spaces. Bad.
                        options[i] = options[i].replace(" ", "");
                    }
                    defaultValues.add(options[4]);
                    formBuilder.slider(text,
                            Float.parseFloat(options[1]),
                            Float.parseFloat(options[2]),
                            Float.parseFloat(options[3]),
                            Float.parseFloat(options[4]));
                }
                case "step-slider" -> {
                    if (options.length < 1) {
                        MagicMenu.getLogger().error("Invalid slider placeholder: " + placeholder + " in " + name);
                        completableFuture.complete(ResultType.FAILURE);
                        return completableFuture;
                    }
                    String stepSliderDefault = options[1];
                    defaultValues.add(stepSliderDefault);
                    String[] stepSliderOptions = Arrays.copyOfRange(options, 2, options.length);
                    formBuilder.stepSlider(options[0], getIndex(stepSliderOptions, stepSliderDefault), stepSliderOptions);
                }
            }
        }

        AtomicReference<String> finalCommand = new AtomicReference<>(command);

        formBuilder.closedOrInvalidResultHandler((form, response) -> {
            defaultValues.clear();
            placeholders.clear();
            MagicMenu.debug("Form closed or invalid, cancelling command " + finalCommand.get());
            completableFuture.complete(ResultType.CANCELLED);
        });

        formBuilder.validResultHandler((form, response) -> {
            for (int i = 0; i < placeholders.size(); i++) {
                String replace = placeholders.get(i);
                String defaultValue = defaultValues.get(i);
                Object value = response.valueAt(i);

                if (value != null && !value.toString().isEmpty()) {
                    Component component = form.content().get(i);
                    // Special handling for dropdowns and sliders.
                    switch (Objects.requireNonNull(component).type()) {
                        case DROPDOWN ->
                            // We want to get the text, not the index
                                value = ((DropdownComponent) component).options().get(response.asDropdown(i));
                        case SLIDER ->
                            // We want an int, not a float
                                value = String.valueOf(value).replace(".0", "");
                        case STEP_SLIDER ->
                            // We want to get the text, not the index
                                value = ((StepSliderComponent) component).steps().get(response.asStepSlider(i));
                    }
                    MagicMenu.debug("Replacing: " + replace + " with " + value);
                    replacePlaceholder(finalCommand, replace, value);
                } else {
                    MagicMenu.debug("Replacing: " + replace + " with default: " + defaultValue);
                    replacePlaceholder(finalCommand, replace, value);
                }
            }
            sendCommand(connection, finalCommand.get());

            defaultValues.clear();
            placeholders.clear();
            completableFuture.complete(ResultType.SUCCESS);
        });

        connection.sendForm(formBuilder);
        return completableFuture;
    }

    // using replaceFirst with the regex does not seem to work reliabl
    private static void replacePlaceholder(AtomicReference<String> finalCommand, String replace, Object value) {
        MagicMenu.debug("attempt to replace: " + replace + " with value " + value);
        String temp = finalCommand.get();
        int index = temp.indexOf("!%" + replace + "%");
        if (index != -1) {
            String before = temp.substring(0, index);
            String after = temp.substring(index + ("!%" + replace + "%").length());
            temp = before + value + after;
        }
        finalCommand.set(temp);
    }

    private static void sendCommand(GeyserConnection connection, String command) {
        GeyserSession session = (GeyserSession) connection;

        MagicMenu.debug("Sending command: " + command);

        if (session.getGeyser().getPlatformType() == PlatformType.STANDALONE ||
                session.getGeyser().getPlatformType() == PlatformType.VIAPROXY) {
            // try to handle the command within the standalone/viaproxy command manager
            String[] args = command.split(" ");
            if (args.length > 0) {
                String root = args[0];

                CommandRegistry registry = GeyserImpl.getInstance().commandRegistry();
                if (registry.rootCommands().contains(root)) {
                    registry.runCommand(session, command);
                    return; // don't pass the command to the java server
                }
            }
        }

        session.sendCommand(command);
    }

    enum ResultType {
        SUCCESS,
        FAILURE,
        CANCELLED
    }

    private static int getIndex(String[] args, String arg) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                return i;
            }
        }
        MagicMenu.getLogger().warning("Could not find default argument: " + arg + " in dropdown/stepslider placeholders: " + Arrays.toString(args));
        return 0;
    }
}