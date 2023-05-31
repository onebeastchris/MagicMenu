package net.onebeastchris.extension.magicmenu.util;

import net.onebeastchris.extension.magicmenu.MagicMenu;
import net.onebeastchris.extension.magicmenu.config.Config;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.List;
import java.util.Stack;


public class PlayerMenuHandler {

    private final String username;

    // main emote definition to base on
    private final Config.EmoteDefinition emoteDefinition;

    private Stack<Object> stack = new Stack<>();

    public PlayerMenuHandler(GeyserConnection connection, Config.EmoteDefinition emoteDefinition) {
        this.emoteDefinition = emoteDefinition;
        this.username = connection.bedrockUsername();
        handle(emoteDefinition, connection);
    }

    private void handle(Object object, GeyserConnection connection) {
        if (object instanceof Config.EmoteDefinition emoteDefinition) {
            // stack.add(emoteDefinition); likely not needed.
            if (checkForCommand(connection, emoteDefinition)) {
                return;
            } else {
                Config.Form form = null;
                for (Config.Form form1 : emoteDefinition.forms()) {
                    if (form1.allowedUsers() != null) {
                        if (form1.allowedUsers().contains(username)) {
                            form = form1;
                            break;
                        }
                    } else {
                        form = form1;
                    }
                }
                if (form == null) {
                    MagicMenu.getLogger().error("No form found for emote definition: "
                            + emoteDefinition.emoteID()
                            + " and player: " + username + "."
                            + " This is a configuration issue: This emote id is accessible to this player, but has no form, or command to execute.");
                    return;
                }
                handle(form, connection);
                return;
            }
        }

        if (object instanceof Config.Form form) {
            stack.add(form);
            // checking for buttons, or back.
            MenuHandler.sendForm(connection, form).whenCompleteAsync((button, throwable) -> {
                if (throwable != null) {
                    // who tf knows, better be safer than sorry
                    throwable.printStackTrace();
                    return;
                }

                if (button == null) {
                    goBack(connection);
                    return;
                }
                handle(button, connection);
            });
            return;
        }
        if (object instanceof Config.Button button) {
            stack.add(button);
            if (checkForCommand(connection, button)) {
                return;
            }
            if (button.forms() != null) {
                Config.Form form = null;
                for (Config.Form potentialForm : button.forms()) {
                    if (potentialForm.allowedUsers() != null) {
                        if (potentialForm.allowedUsers().contains(username)) {
                            form = potentialForm;
                            break;
                        }
                    } else {
                        form = potentialForm;
                    }
                }
                if (form == null) {
                    MagicMenu.getLogger().error("No form found for button: "
                            + button.name()
                            + " and player: " + username + "."
                            + " This is a configuration issue: This button is accessible to this player, but has no form, or command to execute.");
                    return;
                }
                handle(form, connection);
                return;
            }
            MagicMenu.debug("No command or form found for button: " + button.name() + " and player: " + username + ".");
        }
        if (object instanceof Config.CommandHolder commandHolder) {
            stack.add(commandHolder);
            MenuHandler.executeCommand(connection, commandHolder).whenCompleteAsync((resultType, throwable) -> {
                if (throwable != null) {
                    // who tf knows, better be safer than sorry
                    throwable.printStackTrace();
                    return;
                }

                MagicMenu.debug("result type is " + resultType);
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
            });
            return;
        }
        MagicMenu.getLogger().error("Unknown object type in handle(): " + object.getClass().getName());
    }

    private boolean checkForCommand(GeyserConnection connection, Object object) {
        if (object instanceof Config.holder holder) {
            // specifically, emote definition, and button
            if (holder.hasCommands()) {
                Config.CommandHolder command = null;
                for (Config.CommandHolder commandHolder : holder.commands()) {
                    if (commandHolder.allowedUsers() != null) {
                        if (commandHolder.allowedUsers().contains(connection.bedrockUsername())) {
                            command = commandHolder;
                            break;
                        }
                    } else {
                        command = commandHolder;
                    }
                }
                stack.add(command);
                handle(command, connection);
                return true;
            }
            return false;
        }
        return false;
    }

    public void goBack(GeyserConnection connection) {
        if (MagicMenu.getConfig().goBackOnClosed() && stack.size() > 0) {
            handle(stack.pop(), connection);
        }
    }

    public static boolean hasPerms(Object list, String userName) {
        if (list instanceof List<?> playerList) {
            return playerList.contains(userName);
        }
        return true;
    }
}
