package net.onebeastchris.extension.emotecommandmenu;

import net.onebeastchris.extension.config.Config;
import net.onebeastchris.extension.util.CommandsHolder;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.HashMap;
import java.util.Map;

public class Form {
    public static void sendForm(GeyserSession session, Config config) {
        Map<String, CommandsHolder> temp = new HashMap<>();
        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(config.title())
                .content(config.description());

        for (CommandsHolder commandsHolder : config.commandsHolders()) {
            simpleForm.button(commandsHolder.name(), FormImage.Type.URL, commandsHolder.imageUrl());
            temp.put(commandsHolder.name(), commandsHolder);
        }

        simpleForm.validResultHandler((form, response) -> {
            CommandsHolder commandsHolder = temp.get(response.getClickedButton().getText());
            showCommandHolderForm(session, commandsHolder);
            temp.clear();
        });

        simpleForm.invalidResultHandler((form, response) -> {
            temp.clear();
        });

    }

    private static void showCommandHolderForm(GeyserSession session, CommandsHolder commandsHolder) {
        Map<String, String> temp = new HashMap<>();
        SimpleForm.Builder simpleForm = SimpleForm.builder()
                .title(commandsHolder.name())
                .content(commandsHolder.description());

        for (CommandsHolder.Command command : commandsHolder.commands()) {
            simpleForm.button(command.name(), FormImage.Type.URL, command.imageUrl());
            temp.put(command.name(), command.command());
        }

        simpleForm.validResultHandler((form, response) -> {
            String command = temp.get(response.getClickedButton().getText());
            session.sendCommand(command);
            temp.clear();
        });

        simpleForm.invalidResultHandler((form, response) -> {
            temp.clear();
        });
    }
}
