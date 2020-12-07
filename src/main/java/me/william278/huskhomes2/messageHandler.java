package me.william278.huskhomes2;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public class messageHandler {

    private static HashMap<String, String> messages = new HashMap<>();

    private static String translateHexColorCodes(String message)
    {
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    // Send a blank message
    public static void sendMessage(Player p, String messageID, String... placeholderReplacements) {
        String message = messages.get(messageID);
        int replacementIndexer = 1;

        // Replace placeholders
        for (String replacement : placeholderReplacements) {
            String replacementString = "%" + replacementIndexer + "%";
            message = message.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }

        // Convert to text component and send
        p.spigot().sendMessage(TextComponent.fromLegacyText(translateHexColorCodes(message)));
    }

    // Send a message with no placeholder parameters
    public static void sendMessage(Player p, String messageID) {
        String message = messages.get(messageID);

        // Convert to text component and send
        p.spigot().sendMessage(TextComponent.fromLegacyText(translateHexColorCodes(message)));
    }

}
