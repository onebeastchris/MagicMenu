package net.onebeastchris.extension.magicmenu.util;

import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

public class PlaceHolder {
    public static String parsePlaceHolders(GeyserConnection connection, String message) {
        GeyserSession session = (GeyserSession) connection;

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
                .replace("%x%", String.valueOf(session.getPlayerEntity().getPosition().getX()))
                .replace("%y%", String.valueOf(session.getPlayerEntity().getPosition().getY()))
                .replace("%z%", String.valueOf(session.getPlayerEntity().getPosition().getZ()))
                .replace("%position%", Math.floor(session.getPlayerEntity().getPosition().getX())
                        + " " + Math.floor(session.getPlayerEntity().getPosition().getY())
                        + " " + Math.floor(session.getPlayerEntity().getPosition().getZ()));
    }
}
