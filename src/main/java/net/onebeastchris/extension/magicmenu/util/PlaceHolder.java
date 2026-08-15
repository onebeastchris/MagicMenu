package net.onebeastchris.extension.magicmenu.util;

import org.geysermc.geyser.api.connection.GeyserConnection;
import org.cloudburstmc.math.vector.Vector3f;

public class PlaceHolder {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String parsePlaceHolders(GeyserConnection connection, String message) {
        Vector3f position = connection.playerEntity().position();

        // safety check
        if (message.startsWith("/")) {
            message.replaceFirst("/", "");
        }

        return message
                .replace("%username%", connection.javaUsername())
                .replace("%xuid%", connection.xuid())
                .replace("%uuid%", connection.javaUuid().toString())
                .replace("%bedrockusername%", connection.bedrockUsername())
                .replace("%version%", connection.version())
                .replace("%device%", connection.inputMode().name())
                .replace("%lang%", connection.languageCode())
                .replace("%x%", String.valueOf(position.getX()))
                .replace("%y%", String.valueOf(position.getY()))
                .replace("%z%", String.valueOf(position.getZ()))
                .replace("%position%", Math.floor(position.getX())
                        + " " + Math.floor(position.getY())
                        + " " + Math.floor(position.getZ()));
    }
}
