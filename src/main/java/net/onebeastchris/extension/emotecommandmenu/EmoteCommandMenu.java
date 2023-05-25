package net.onebeastchris.extension.emotecommandmenu;

import net.onebeastchris.extension.emotecommandmenu.config.Config;
import net.onebeastchris.extension.emotecommandmenu.config.ConfigLoader;
import net.onebeastchris.extension.emotecommandmenu.util.MenuHandler;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.session.GeyserSession;

import java.util.HashMap;
import java.util.Map;

public class EmoteCommandMenu implements Extension {

    public static ExtensionLogger getLogger() {
        return logger;
    }
    private static ExtensionLogger logger;
    private static Config config;

    public static Config getConfig() {
        return config;
    }

    boolean isAllEmotes = false;
    private Map<String, Config.FormDefinition> commandsHolderMap;

    @Subscribe
    public void onGeyserPostInitializeEvent(GeyserPostInitializeEvent event) {
        logger = this.logger();
        commandsHolderMap = new HashMap<>();
        try {
            config = ConfigLoader.load(this, EmoteCommandMenu.class, Config.class, logger());
            assert config != null;
            for (Config.FormDefinition formDefinition : config.forms()) {
                if (formDefinition.emoteID().equals("all")) {
                    isAllEmotes = true;
                }
                //you cant have *multiple* "all" forms.
                commandsHolderMap.put(formDefinition.emoteID(), formDefinition);
            }
        } catch (Exception e) {
            logger().error("Failed to load EmoteCommandMenu config!", e);
            e.printStackTrace();
            return;
        }
        logger().info("EmoteCommandMenu config loaded!");
    }


    @Subscribe
    public void onEmote(ClientEmoteEvent event) {
        logger().info(event.connection().bedrockUsername() + "sent emote with emote id: " + event.emoteId());
        GeyserSession session = (GeyserSession) event.connection();
        if (commandsHolderMap.containsKey(event.emoteId())) {
            event.setCancelled(!config.showEmotes());
            MenuHandler.sendForm(session, commandsHolderMap.get(event.emoteId()));
        } else if (isAllEmotes) {
            event.setCancelled(!config.showEmotes());
            MenuHandler.sendForm(session, commandsHolderMap.get("all"));
        }
    }

}
