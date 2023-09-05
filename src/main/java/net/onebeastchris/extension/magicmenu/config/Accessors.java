package net.onebeastchris.extension.magicmenu.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.List;

public record Accessors(
        @JsonProperty("command-accessors")
        @Nullable List<CommandAccessor> commandAccessors,

        @JsonProperty("emote-accessors")
        @Nullable List<EmoteAccessor> emoteAccessors
) {
    public record CommandAccessor(
        @JsonProperty("command")
        @NonNull String command,

        @JsonProperty("command-name")
        @Nullable String commandName,

        @JsonProperty("permission")
        @Nullable String permission,

        @JsonProperty("description")
        @Nullable String description,

        @JsonProperty("aliases")
        @Nullable List<String> aliases,

        @JsonProperty("executes-command")
        List<Config.CommandExecutor> commandExecutors,

        @JsonProperty("executes-form")
        List<Config.FormExecutor> formExecutors
    ) implements Accessor {
        @Override
        public AccessorType type() {
            return AccessorType.COMMAND;
        }

        @Override
        public boolean hasCommands() {
            return commandExecutors != null && !commandExecutors.isEmpty();
        }

        @Override
        public boolean hasForms() {
            return formExecutors != null && !formExecutors.isEmpty();
        }
    }

    public record EmoteAccessor(
        @JsonProperty("emote-uuid")
        @NonNull String emoteID,

        @JsonProperty("allowed-users")
        @Nullable List<String> allowedUsers,

        @JsonProperty("executes-command")
        List<Config.CommandExecutor> commandExecutors,

        @JsonProperty("executes-form")
        List<Config.FormExecutor> formExecutors

    ) implements Accessor, Config.Restrictable {
        @Override
        public AccessorType type() {
            return AccessorType.EMOTE;
        }

        @Override
        public boolean hasCommands() {
            return commandExecutors != null && !commandExecutors.isEmpty();
        }

        @Override
        public boolean hasForms() {
            return formExecutors != null && !formExecutors.isEmpty();
        }
    }

    public interface Accessor {
        AccessorType type();

        boolean hasCommands();

        boolean hasForms();

        List<Config.FormExecutor> formExecutors();

        List<Config.CommandExecutor> commandExecutors();

        default Config.Executor getExecutorForUsername(String username) {
            if (hasCommands()) {
                for (Config.CommandExecutor executor : commandExecutors()) {
                    if (executor.isAllowed(username)) {
                        return executor;
                    }
                }
            }
            if (hasForms()) {
                for (Config.FormExecutor executor : formExecutors()) {
                    if (executor.isAllowed(username)) {
                        return executor;
                    }
                }
            }
            return null;
        }
    }

    public enum AccessorType {
        COMMAND,
        EMOTE,

        BUTTON
    }
}
