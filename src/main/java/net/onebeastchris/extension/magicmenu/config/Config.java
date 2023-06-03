package net.onebeastchris.extension.magicmenu.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public record Config(
        @JsonProperty("config-version") String version,
        @JsonProperty("menus") List<Menu> menu,
        @JsonProperty("show-emotes") boolean showEmotes,
        @JsonProperty("go-back")boolean goBackOnClosed,
        @JsonProperty("debug") boolean debug
    ) {
        public record Menu(
            @JsonProperty("emote-uuid")
            // nullable since there can be menu only accessible via command.
            @Nullable String emoteID,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

            @JsonProperty("menu-command")
            @Nullable Command menuCommand,
            @JsonProperty("forms")
            List<Form> forms,

            @JsonProperty("commands")
            List<CommandHolder> commands
        ) implements holder {

        }

        public record Form (
            @JsonProperty("form-title")
            @NonNull String title,

            @JsonProperty("form-description")
            String description,

            @JsonProperty("buttons")
            List<Button> buttons,

            @JsonProperty("allowed-users")
            List<String> allowedUsers
        ) {

        }
        public record Button(
            @JsonProperty("name")
            @NonNull String name,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

            @JsonProperty("image-url")
            String imageUrl,

            // emote -> form -> command
            // in case of a direct command.
            // multiple commands, in the edge case, that someone has multiple commands with different permissions.
            @JsonProperty("commands")
            List<CommandHolder> commands,
            @JsonProperty("forms")
            List<Form> forms
        ) implements holder {

        }

        public record CommandHolder(

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers,

            @JsonProperty("command-name")
            String name,

            @JsonProperty("run")
            @NonNull String command
        ) {
        }

        public interface holder {
            List<CommandHolder> commands();

            default boolean hasCommands() {
                return commands() != null && !commands().isEmpty();
            }
        }

        public record Command(
            @JsonProperty("command-name")
            String commandName,

            @JsonProperty("description")
            @Nullable String description,

            @JsonProperty("aliases")
            @Nullable List<String> aliases,

            @JsonProperty("permission")
            @Nullable String permission
        ) {
        }
}