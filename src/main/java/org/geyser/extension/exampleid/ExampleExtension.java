package org.geyser.extension.exampleid;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
public class ExampleExtension implements Extension {

    // The main class of your extension - must implement extension, and be in the extension.yml file.
    // see https://github.com/GeyserMC/Geyser/blob/master/api/src/main/java/org/geysermc/geyser/api/extension/Extension.java for more info on available methods

    // example: accessing extension datafolder
    // Path thisDataFolder = this.dataFolder();

    // Registering custom items/blocks, or adding resource packs (and basically all other events that are fired before Geyser initializes fully)
    // are done in the PreInitializeEvent. Yeet this if you don't need it.
    @Subscribe
    public void onGeyserPreInitializeEvent(GeyserLoadResourcePacksEvent event) {
        logger().info("Loading: " + event.resourcePacks().size() + " resource packs.");
    }

    // You can use this to run anything after Geyser fully initialized and is ready to accept bedrock player connections.
    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        this.logger().info("Loading example extension...");
    }
}
