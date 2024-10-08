package net.onebeastchris.extension.magicmenu;

import net.onebeastchris.extension.magicmenu.config.Config;
import net.onebeastchris.extension.magicmenu.config.ConfigLoader;
import net.onebeastchris.extension.magicmenu.util.PlayerMenuHandler;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.util.TriState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.onebeastchris.extension.magicmenu.util.PlayerMenuHandler.hasPerms;

public class MagicMenu implements Extension {

    public static ExtensionLogger getLogger() {
        return logger;
    }

    private static ExtensionLogger logger;
    private static Config config;

    public static Config getConfig() {
        return config;
    }

    boolean isAllEmotes = false;
    private Map<String, Config.Menu> menuMap;

    private Map<Command, Config.Menu> commandMap;

    @Subscribe
    public void onGeyserPreInitialize(GeyserPreInitializeEvent event) {
        logger = this.logger();
        menuMap = new HashMap<>();
        commandMap = new HashMap<>();

        try {
            config = ConfigLoader.load(this, MagicMenu.class, Config.class);
            assert config != null;

            if (!config.version().equals("1.0")) {
                logger().error("Config version is not up to date! Please update your config!");
                disable();
            }

            for (Config.Menu menu : config.menu()) {
                if (menu.emoteID() == null && menu.menuCommand() == null) {
                    logger().error("Emote definition has no emote id or command! This is a configuration issue.");
                } else if (menu.emoteID() != null) {
                    if (menu.emoteID().equals("all")) {
                        isAllEmotes = true;
                    }
                    //you cant have *multiple* "all" definitions. well, you can, but only the last will be used.
                    if (menuMap.containsKey(menu.emoteID())) {
                        logger().warning("Multiple emote definitions for " + menu.emoteID() + " found! Only the last will be used.");
                    }
                    debug("emote id is not null, adding " + menu.emoteID() + " to emote map");
                    menuMap.put(menu.emoteID(), menu);
                }
                if (menu.menuCommand() != null) {
                    debug("menu command is not null, adding " + menu.menuCommand() + " to command map");
                    commandMap.put(getCommand(menu.menuCommand()), menu);
                }
            }
        } catch (Exception e) {
            logger().error("Failed to load MagicMenu config!", e);
            e.printStackTrace();
            disable();
            return;
        }
        logger().info("MagicMenu loaded!");
    }

    @Subscribe
    public void onEmote(ClientEmoteEvent event) {
        debug(event.connection().bedrockUsername() + "sent emote with emote id: " + event.emoteId());

        if (menuMap.containsKey(event.emoteId())) {
            debug("emote id is in menu_map");
            run(menuMap.get(event.emoteId()), event);
        } else if (isAllEmotes) {
            debug("emote id is not in menu_map, but there is an all emote");
            run(menuMap.get("all"), event);
        }
    }

    private void run(Config.Menu menu, ClientEmoteEvent event) {
        if (!hasPerms(menu.allowedUsers(), event.connection().bedrockUsername())) {
            debug("player does not have perms for this emote");
            return;
        }
        event.setCancelled(!config.showEmotes());
        new PlayerMenuHandler(event.connection(), menu);
    }

    private Command getCommand(Config.Command command) {
        String desc = Optional.ofNullable(command.description()).orElse("Opens the " + command + " menu");
        String perm = Optional.ofNullable(command.permission()).orElse("");

        return Command.builder(this)
                .name(command.commandName())
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .aliases(Optional.ofNullable(command.aliases()).orElse(List.of()))
                .description(desc)
                .playerOnly(true)
                .permission(perm, TriState.TRUE)
                .executor((source, cmd, args) -> {
                    debug("Running command " + cmd);
                    if (commandMap.containsKey(cmd)) {
                        debug("command is in command_map");
                        Config.Menu menu = commandMap.get(cmd);
                        GeyserConnection connection = (GeyserConnection) source;
                        if (!hasPerms(menu.allowedUsers(), connection.bedrockUsername())) {
                            debug("player does not have perms for this emote");
                            return;
                        }
                        new PlayerMenuHandler(connection, menu);
                        return;
                    }
                    debug("command " + cmd + "is not in command_map");
                    source.sendMessage("The command " + cmd + "does not exist!");
                })
                .build();
    }

    @Subscribe
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        debug("Registering commands");
        debug("command map size: " + commandMap.size());
        for (Command command : commandMap.keySet()) {
            commandsEvent.register(command);
        }
    }

    public static void debug(String message) {
        if (config.debug()) {
            getLogger().info(message);
        }
    }
}

