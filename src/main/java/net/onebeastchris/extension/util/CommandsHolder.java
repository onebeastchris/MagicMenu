package net.onebeastchris.extension.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record CommandsHolder(
        String name,
        String description,
        @JsonProperty("image-url") String imageUrl,
        @JsonProperty("commands") List<Command> commands
) {
    public record Command (
            String name,
            String command,
            @JsonProperty("image-url") String imageUrl
    ) {}
}
