package net.onebeastchris.extension.emotecommandmenu.util;

import net.onebeastchris.extension.emotecommandmenu.EmoteCommandMenu;
import net.onebeastchris.extension.emotecommandmenu.config.Config;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            // custom inputs pls

            String commandToSend = "test";
            CustomForm.Builder customForm = CustomForm.builder()
                    .title(commandHolder.name());

            // add options: input, toggle, dropdown, slider

            customForm.invalidResultHandler((form, response) -> {
                if (EmoteCommandMenu.getConfig().goBackOnClosed()) {
                    session.sendForm(customForm);
                }
            });

            customForm.validResultHandler((form, response) -> {
                session.sendCommand(commandToSend);
            });
            session.sendForm(customForm);
        } else {
            session.sendCommand(commandHolder.command());
        }
    }

}
