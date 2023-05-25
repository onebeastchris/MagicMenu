package net.onebeastchris.extension.emotecommandmenu.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public record Config(
        @JsonProperty("config-version") String version,
        @JsonProperty("menus") List<FormDefinition> forms,
        @JsonProperty("show-emotes") boolean showEmotes,
        @JsonProperty("go-back")boolean goBackOnClosed
    ) {
        public record FormDefinition(
            @JsonProperty("emote-uuid")
            @NonNull String emoteID,
            @JsonProperty("form-title")
            @NonNull String title,

            @JsonProperty("form-description")
            String description,
            @JsonProperty("buttons")
            List<Button> buttons,
            // emote -> command
            @JsonProperty("command")
            CommandHolder command
        ) {

        }
        public record Button(
            @JsonProperty("name")
            @NonNull String name,

            @JsonProperty("description")
            String description,
            @JsonProperty("image-url")
            String imageUrl,

            // emote -> form -> commands form -> command
            @JsonProperty("commands")
            List<CommandHolder> commandHolders,

            // emote -> form -> command
            // in case of a direct command.
            @JsonProperty("command")
            CommandHolder command
        ) {
        }

        public record CommandHolder(
            @JsonProperty("command-name")
            String name,

            @JsonProperty("image-url")
            String imageUrl,

            @JsonProperty("run")
            @NonNull String command
        ) {
        }

}