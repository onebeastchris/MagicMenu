package net.onebeastchris.extension.magicmenu.util;

import net.onebeastchris.extension.magicmenu.MagicMenu;
import net.onebeastchris.extension.magicmenu.config.Accessors;
import net.onebeastchris.extension.magicmenu.config.Config;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.Stack;


public class PlayerFormHandler {

    private final String username;

    private final Stack<Object> stack = new Stack<>();

    public PlayerFormHandler(GeyserConnection connection, Accessors.Accessor ac) {
        // main emote definition to base on
        this.username = connection.bedrockUsername();
        handle(ac, connection);
    }

    public PlayerFormHandler(GeyserConnection connection, Config.Form form) {
        this.username = connection.bedrockUsername();
        handle(form, connection);
    }

    private void handle(Object object, GeyserConnection connection) {

        if (object instanceof Accessors.Accessor accessor) {
            Config.Executor executor = accessor.getExecutorForUsername(username);
            if (executor == null) {
                String type = accessor.type().toString().toLowerCase();
                MagicMenu.logger.error("No executor found for player: " + username + "." +
                        " This is a configuration issue: This player is allowed to use this " + type + ", but has no executor defined.");
                return;
            }

            handle(executor, connection);
            return;
        }

        if (object instanceof Config.FormExecutor formExecutor) {
            if (!formExecutor.isAllowed(username)) {
                MagicMenu.logger.error("Player: " + username + " is not allowed to view form: " + formExecutor.id() + ". This is usually a config issue!");
                goBack(connection);
                return;
            }
            Config.Form form = MagicMenu.getForm(formExecutor.id());
            if (form == null) {
                MagicMenu.logger.error("No form found with id: " + formExecutor.id());
                goBack(connection);
                return;
            }
            // TODO: add to back list?

            handle(form, connection);
            return;
        }

        if (object instanceof Config.CommandExecutor commandExecutor) {
            if (!commandExecutor.isAllowed(username)) {
                MagicMenu.logger.error("Player: " + username + " is not allowed to execute command: " + commandExecutor.command());
                goBack(connection);
                return;
            }
            MagicMenu.debug("Adding command: " + commandExecutor.command() + " to stack.");
            stack.add(commandExecutor);
            MenuHandler.executeCommand(connection, commandExecutor).whenCompleteAsync((resultType, throwable) -> {
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

        if (object instanceof Config.Form form) {
            if (!form.isAllowed(username)) {
                MagicMenu.logger.error("Player: " + username + " is not allowed to view form: " + form.title() + ". This is usually a config issue!");
                goBack(connection);
                return;
            }
            MagicMenu.debug("Adding form: " + form.title() + " to stack.");
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

        MagicMenu.logger.error("Unknown object type in handle(): " + object.getClass().getName());
    }

    public void goBack(GeyserConnection connection) {
        MagicMenu.debug("Going back? " + MagicMenu.getConfig().goBackOnClosed() + " stack size: " + stack.size());
        if (MagicMenu.getConfig().goBackOnClosed()) {
            var ignored = stack.pop(); // last element is what we were on.
            MagicMenu.debug("Current: " + ignored.getClass().getName());

            if (!stack.isEmpty()) {
                handle(stack.pop(), connection);
                MagicMenu.debug("NEW Stack size: " + stack.size());
            } else {
                MagicMenu.debug("Stack is empty.");
            }
        }
    }
}
