package net.onebeastchris.extension.magicmenu;

import net.onebeastchris.extension.magicmenu.config.Config;
import net.onebeastchris.extension.magicmenu.config.ConfigLoader;
import net.onebeastchris.extension.magicmenu.util.PlayerMenuHandler;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Config.EmoteDefinition> menu_map;

    @Subscribe
    public void onGeyserPostInitializeEvent(GeyserPostInitializeEvent event) {
        logger = this.logger();
        menu_map = new HashMap<>();

        try {
            config = ConfigLoader.load(this, MagicMenu.class, Config.class);
            assert config != null;

            for (Config.EmoteDefinition formDefinition : config.emoteDefinitions()) {
                if (formDefinition.emoteID().equals("all")) {
                    isAllEmotes = true;
                }
                //you cant have *multiple* "all" definitions. well, you can, but only the last will be used.
                if (menu_map.containsKey(formDefinition.emoteID())) {
                    logger().warning("Multiple emote definitions for " + formDefinition.emoteID() + " found! Only the last will be used.");
                }
                menu_map.put(formDefinition.emoteID(), formDefinition);
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

        if (menu_map.containsKey(event.emoteId())) {
            debug("emote id is in menu_map");
            run(menu_map.get(event.emoteId()), event);
        } else if (isAllEmotes) {
            debug("emote id is not in menu_map, but there is an all emote");
            run(menu_map.get("all"), event);
        }
    }

    private void run(Config.EmoteDefinition emoteDefinition, ClientEmoteEvent event) {
        if (!hasPerms(emoteDefinition.allowedUsers(), event.connection().bedrockUsername())) {
            debug("player does not have perms for this emote");
            return;
        }
        event.setCancelled(!config.showEmotes());
        new PlayerMenuHandler(event.connection(), emoteDefinition);
    }

    public static void debug(String message) {
        if (config.debug()) {
            getLogger().info(message);
        }
    }
}

