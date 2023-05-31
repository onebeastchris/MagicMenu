package net.onebeastchris.extension.magicmenu.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.Normalizer;
import java.util.List;

public record Config(
        @JsonProperty("config-version") String version,
        @JsonProperty("menus") List<EmoteDefinition> emoteDefinitions,
        @JsonProperty("show-emotes") boolean showEmotes,
        @JsonProperty("go-back")boolean goBackOnClosed,

        @JsonProperty("debug") boolean debug
    ) {
        public record EmoteDefinition(
            @JsonProperty("emote-uuid")
            @NonNull String emoteID,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

            @JsonProperty("forms")
            List<Form> forms
        ) {

        }

        public record Form (
            @JsonProperty("form-title")
            @NonNull String title,

            @JsonProperty("form-description")
            String description,

            @JsonProperty("buttons")
            List<Button> buttons,
            // emote -> command
            @JsonProperty("command")
            CommandHolder command,

            @JsonProperty("allowed-users")
            List<String> allowedUsers
        ) {

        }
        public record Button(
            @JsonProperty("name")
            @NonNull String name,

            @JsonProperty("description")
            String description,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

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

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

            @JsonProperty("command-name")
            String name,

                @JsonProperty("image-url")
            String imageUrl,

                @JsonProperty("run")
            @NonNull String command
        ) {
        }

}