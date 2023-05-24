package net.onebeastchris.extension.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.onebeastchris.extension.util.CommandsHolder;

import java.util.List;

public record Config(
        String title,
        String description,
        @JsonProperty("main-page-buttons") List<CommandsHolder> commandsHolders,
        @JsonProperty("show-emotes") boolean showEmotes,
        @JsonProperty("emote-list") List<String> emoteList
) { }