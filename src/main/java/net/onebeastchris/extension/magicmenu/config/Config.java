package net.onebeastchris.extension.magicmenu.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public record Config(
        @JsonProperty("config-version") String version,
        @JsonProperty("show-emotes") boolean showEmotes,
        @JsonProperty("go-back")boolean goBackOnClosed,
        @JsonProperty("debug") boolean debug
    ) {
        public record Form (
            @JsonProperty("form-title")
            @NonNull String title,

            @JsonProperty("form-description")
            String description,

            @JsonProperty("buttons")
            List<Button> buttons,
            @JsonProperty("allowed-users")
            List<String> allowedUsers

        ) implements Restrictable { }

        public record Button (
            @JsonProperty("name")
            @NonNull String name,

            @JsonProperty("image-url")
            String imageUrl,

            // multiple commands, in the edge case, that someone has multiple commands with different permissions.
            @JsonProperty("commands")
            List<CommandExecutor> commands,

            @JsonProperty("forms")
            List<FormExecutor> forms,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers
        ) implements Restrictable, Accessors.Accessor {
            @Override
            public Accessors.AccessorType type() {
                return Accessors.AccessorType.BUTTON;
            }

            @Override
            public boolean hasCommands() {
                return commands != null && !commands.isEmpty();
            }

            @Override
            public boolean hasForms() {
                return forms != null && !forms.isEmpty();
            }

            @Override
            public List<FormExecutor> formExecutors() {
                return forms;
            }

            @Override
            public List<CommandExecutor> commandExecutors() {
                return commands;
            }

            @Override
            public Executor getExecutorForUsername(String username) {
                return Accessors.Accessor.super.getExecutorForUsername(username);
            }
        }

        public record CommandExecutor (
            @JsonProperty("command-name")
            @Nullable String name,

            @JsonProperty("run")
            @NonNull String command,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers
        ) implements Restrictable, Executor {
            @Override
            public ExecutorType type() {
                return ExecutorType.COMMAND;
            }
        }

        public record FormExecutor (
            @JsonProperty("form-id")
            @NonNull String id,

            @JsonProperty("allowed-users")
            @Nullable List<String> allowedUsers

        ) implements Restrictable, Executor {
            @Override
            public ExecutorType type() {
                return ExecutorType.FORM;
            }
        }

        public interface Restrictable {
            List<String> allowedUsers();

            default boolean hasAllowedUsers() {
                return allowedUsers() != null && !allowedUsers().isEmpty();
            }

            default boolean isAllowed(String user) {
                return !hasAllowedUsers() || allowedUsers().contains(user);
            }
        }

        public interface Executor {
            ExecutorType type();
        }

        public enum ExecutorType {
            COMMAND,
            FORM
        }
}