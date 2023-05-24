package net.onebeastchris.extension.emotecommandmenu;

import net.onebeastchris.extension.config.Config;
import net.onebeastchris.extension.config.ConfigLoader;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.session.GeyserSession;

public class EmoteCommandMenu implements Extension {

    private Config config;

    @Subscribe
    public void onGeyserPostInitializeEvent(GeyserPostInitializeEvent event) {
        logger().info("EmoteCommandMenu enabled!");
        config = ConfigLoader.load(this, getClass(), Config.class, logger());
        logger().info("EmoteCommandMenu config loaded!");
    }


    @Subscribe
    public void onCommandExecute(ClientEmoteEvent event) {
        logger().info(event.connection().bedrockUsername() + "sent emote with emote id: " + event.emoteId());
        GeyserSession session = (GeyserSession) event.connection();
        if (true || config.emoteList().contains(event.emoteId())) {
            if (config.showEmotes()) {
                event.setCancelled(true);
            }
            Form.sendForm(session, config);
        }
    }

}
