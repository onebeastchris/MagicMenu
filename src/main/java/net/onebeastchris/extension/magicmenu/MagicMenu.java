package net.onebeastchris.extension.magicmenu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.onebeastchris.extension.magicmenu.config.Accessors;
import net.onebeastchris.extension.magicmenu.config.Config;
import net.onebeastchris.extension.magicmenu.config.ConfigLoader;
import net.onebeastchris.extension.magicmenu.util.PlayerFormHandler;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.util.PlatformType;
import org.geysermc.geyser.command.GeyserCommandSource;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MagicMenu implements Extension {
    private Path forms_dir;
    private Path accessors;
    public static PlatformType platformType;
    public static ExtensionLogger logger;
    private static Config config;
    boolean isAllEmotes = false;
    private final Map<String, Accessors.EmoteAccessor> emotes = new HashMap<>();
    private final Map<Command, Accessors.CommandAccessor> commands = new HashMap<>();
    private static final Map<String, Config.Form> forms = new HashMap<>();

    @Subscribe
    public void onGeyserPreInitialize(GeyserPreInitializeEvent event) {
        logger = this.logger();
        platformType = this.geyserApi().platformType();

        forms_dir = this.dataFolder().resolve("forms");
        accessors = this.dataFolder().resolve("accessors.yml");

        try {
            config = ConfigLoader.load(this, MagicMenu.class, Config.class);
            assert config != null;
            parseConfig(config);
        } catch (Exception e) {
            logger().error("Failed to load MagicMenu config!", e);
            e.printStackTrace();
            this.disable();
            return;
        }
        logger().info("MagicMenu loaded!");
    }

    @Subscribe
    public void onEmote(ClientEmoteEvent event) {
        debug(event.connection().bedrockUsername() + "sent emote with emote id: " + event.emoteId());
        Accessors.EmoteAccessor emoteAccessor = emotes.getOrDefault(event.emoteId(), isAllEmotes ? emotes.get("all") : null);
        if (emoteAccessor != null) {
            if (!emoteAccessor.isAllowed(event.connection().bedrockUsername())) {
                return;
            }
            event.setCancelled(!config.showEmotes());
            new PlayerFormHandler(event.connection(), emoteAccessor);
        }
    }

    private Command getCommand(Accessors.CommandAccessor commandAccessor) {
        String desc = Optional.ofNullable(commandAccessor.description()).orElse("Opens the " + commandAccessor.command() + " menu");
        String name = commandAccessor.command().strip().toLowerCase();

        // replace only leading slashes, e.g. for "/magicmenu test" -> "test"
        name = name.replaceAll("^/?(magicmenu)?\\s*", "");

        return Command.builder(this)
                .name(commandAccessor.command())
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .aliases(Optional.ofNullable(commandAccessor.aliases()).orElse(List.of()))
                .description(desc)
                .executableOnConsole(false)
                .suggestedOpOnly(false)
                .permission(Optional.ofNullable(commandAccessor.permission()).orElse(""))
                .executor((source, cmd, args) -> {
                    if (commands.containsKey(cmd)) {
                        GeyserConnection connection = (GeyserConnection) source;
                        new PlayerFormHandler(connection, commands.get(cmd));
                        return;
                    }
                    debug("command " + cmd + "is not in command_map");
                    source.sendMessage("The command " + cmd + "does not exist!");
                })
                .build();
    }

    @Subscribe
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        debug("Registering " + commands.size() +" commands");
        commands.forEach((key, value) -> commandsEvent.register(key));

        commandsEvent.register(Command.builder(this)
                .suggestedOpOnly(true)
                .name("show")
                .source(GeyserCommandSource.class)
                .description("Shows the specified form to the specified player")
                .permission("magicmenu.command.show")
                .executableOnConsole(true)
                .executor((source, cmd, args) -> {
                    debug("show command called: " + Arrays.toString(args));
                    if (args.length < 2) {
                        source.sendMessage("Usage: /magicmenu show <player> <form>");
                        return;
                    }
                    debug(args[0] + "|" + args[1]);
                    if (forms.containsKey(args[1])) {
                        // now: finding session
                        if (args[0].contains("@a")) {
                            for (GeyserConnection connection : this.geyserApi().onlineConnections()) {
                                new PlayerFormHandler(connection, forms.get(args[1]));
                            }
                            source.sendMessage("Showing form " + args[1] + " to all players!");
                            return;
                        }

                        GeyserConnection connection = null;
                        for (GeyserConnection c : this.geyserApi().onlineConnections()) {
                            if (c.javaUsername().equals(args[0])) {
                                connection = c;
                                break;
                            }
                        }

                        if (connection == null) {
                            source.sendMessage("The player " + args[0] + " is not online!");
                            return;
                        }

                        new PlayerFormHandler(connection, forms.get(args[1]));
                        return;
                    }
                    source.sendMessage("The form " + args[1] + " does not exist!");
                })
                .build());
    }

    public static void debug(String message) {
        if (config.debug()) logger.info("[MagicMenu DEBUG] " + message);
    }

    private void parseConfig(Config config) {
        // TODO:  Check & transform old config

        if (!config.version().equals("2.0")) {
            logger().error("Outdated config version. Please re-generate the config.");
            return;
        }

        try {
            if (!Files.exists(forms_dir)) {
                Files.createDirectory(forms_dir);

                FileSystem fileSystem = FileSystems.newFileSystem(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap());
                Files.copy(fileSystem.getPath("forms/example-form.yml"), forms_dir.resolve("example-form.yml"));
                Files.copy(fileSystem.getPath("forms/geyser-form.yml"), forms_dir.resolve("geyser-form.yml"));
            }

            // Load forms
            List<Path> files = Files.walk(forms_dir).toList();
            logger.info("Found " + files.size() + " forms");

            for (Path formPath : files) {
                if (formPath.toFile().isDirectory()) continue;
                Config.Form form = new ObjectMapper(new YAMLFactory())
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                        .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                        .readValue(formPath.toFile(), Config.Form.class);

                forms.put(formPath.getFileName().toString().replace(".yml", ""), form);
            }
        } catch (Exception e) {
            logger().error("Failed to load/read forms because " + e.getMessage() + " !", e);
            this.disable();
        }

        try {
            if (!Files.exists(accessors)) {
                FileSystem fileSystem = FileSystems.newFileSystem(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap());
                Files.copy(fileSystem.getPath("accessors.yml"), accessors);
            }

            Accessors acc = new ObjectMapper(new YAMLFactory())
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                    .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                    .readValue(accessors.toFile(), Accessors.class);

            if (acc.commandAccessors() != null && !acc.commandAccessors().isEmpty()) {
                // reg commands
                acc.commandAccessors().forEach(accessor -> commands.put(getCommand(accessor), accessor));
            }

            if (acc.emoteAccessors() != null && !acc.emoteAccessors().isEmpty()) {
                acc.emoteAccessors().forEach(emoteAccessor -> {
                    if (emoteAccessor.emoteID().equals("all")) {
                        isAllEmotes = true;
                    }
                    emotes.put(emoteAccessor.emoteID(), emoteAccessor);
                });
            }
        } catch (Exception e) {
            logger.error("Failed to load config!", e);
            this.disable();
        }
    }
    public static Config getConfig() {
        return config;
    }

    public static Config.Form getForm(String name) {
        return forms.getOrDefault(name, null);
    }
}


