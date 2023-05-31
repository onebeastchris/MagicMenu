package net.onebeastchris.extension.magicmenu.util;

import net.onebeastchris.extension.magicmenu.MagicMenu;
import net.onebeastchris.extension.magicmenu.config.Config;
import org.geysermc.api.Geyser;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;


public class PlayerMenuHandler {

    private final String username;

    // main emote definition to base on
    private final Config.EmoteDefinition emoteDefinition;

    // storing the form we are currently on
    private Config.Form formDefinition;

    // storing the button we are currently on
    private Config.Button button;

    // storing the command we are currently on
    private Config.CommandHolder commandHolder;

    // storing the stage to go back to
    // just the form/button/commandHolder isnt enough, since commands can be in both a form and a button.
    enum Stage {
        MAIN,
        BUTTON,
        NONE
    }
    private Stage stage = Stage.NONE;

    public PlayerMenuHandler(GeyserConnection connection, Config.EmoteDefinition emoteDefinition) {
        this.emoteDefinition = emoteDefinition;
        this.username = connection.bedrockUsername();
        defineMain(connection);
    }

    private void defineMain(GeyserConnection connection) {
        for (Config.Form form : emoteDefinition.forms()) {
            if (form.allowedUsers() != null) {
                if (form.allowedUsers().contains(username)) {
                    this.formDefinition = form;
                    break;
                }
            } else {
                this.formDefinition = form;
                break;
            }
            MagicMenu.getLogger().warning("No form found for " + username + " in " + emoteDefinition.emoteID());
        }

        // now that the main form for the player is found, send it
        executeMain(connection);
    }

    public void goBack(GeyserConnection connection) {
        if (MagicMenu.getConfig().goBackOnClosed()) {
            switch (stage) {
                case MAIN -> executeMain(connection);
                case BUTTON -> executeButton(connection);
            }
        }
    }

    private void executeMain(GeyserConnection connection) {
        stage = Stage.NONE;
        if (formDefinition.command() != null && hasPerms(formDefinition.allowedUsers(), username)) {
            commandHolder = formDefinition.command();
            executeCommand(connection);
            // not able to go back, hence no stage setting.
            return;
        }

        // checking for buttons, or back.
        MenuHandler.mainForm(connection, formDefinition).whenCompleteAsync((button, throwable) -> {
            if (throwable != null) {
                // who tf knows, better be safer than sorry
                throwable.printStackTrace();
                return;
            }

            if (button == null) {
                goBack(connection);
                return;
            }
            this.button = button;
            executeButton(connection);
        });
    }

    private void executeButton(GeyserConnection connection){
        MagicMenu.getLogger().info("executing button");
        if (button == null) {
            MagicMenu.getLogger().error("An error occurred while parsing button, is null when it shouldnt be.");
            executeMain(connection);
            return;
        }

        stage = Stage.MAIN;
        if (button.command() != null) {
            commandHolder = button.command();
            executeCommand(connection);
            return;
        }
        MenuHandler.showButtonForm(connection, button).whenCompleteAsync((commandHolder, throwable) -> {
            if (throwable != null) {
                // who tf knows, better be safer than sorry
                throwable.printStackTrace();
                return;
            }

            if (commandHolder == null) {
                goBack(connection);
                return;
            }
            this.commandHolder = commandHolder;
            stage = Stage.BUTTON;
            executeCommand(connection);
        });
    }

    public void executeCommand(GeyserConnection connection) {
        MenuHandler.RestultType resultType = MenuHandler.executeCommand(connection, commandHolder);
        MagicMenu.getLogger().info("result type is " + resultType);
        switch (resultType) {
            case SUCCESS:
                break;
            case FAILURE:
                connection.sendMessage("§cAn error occurred while parsing this command!");
                break;
            case CANCELLED:
                goBack(connection);
                break;
        }
    }

    public static boolean hasPerms(Object list, String userName) {
        if (list instanceof List<?> playerList) {
            return playerList.contains(userName);
        }
        return true;
    }
}
