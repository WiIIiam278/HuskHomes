package me.william278.huskhomes2;

import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;
import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

public class messageManager {

    private static final int languageFileVersion = 1;

    private static HashMap<String, String> messages = new HashMap<>();

    private static final Main plugin = Main.getInstance();

    // Delete the file at the pointer specified
    private static void deleteFile(File f) {
        try {
            if (!f.delete()) {
                Bukkit.getLogger().severe("Failed to delete messages.yml file!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("An error occurred while deleting the messages.yml file!");
            e.printStackTrace();
        }
    }

    // Create a new file, at the pointer specified
    private static void createFile(File f) {
        try {
            if (!f.createNewFile()) {
                Bukkit.getLogger().severe("Failed to create messages.yml file!");
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("An error occurred while creating the messages.yml file!");
            e.printStackTrace();
        }
    }

    // Save the config file specified to the file at the pointer specified
    private static void saveFile(FileConfiguration config, File f) {
        try {
            config.save(f);
        } catch (IOException e) {
            Bukkit.getLogger().severe("An error occurred while saving the messages.yml file!");
        }
    }

    // (Re-)Load the messages file with the correct language
    public static void loadMessages(String language) {
        File f = new File(plugin.getDataFolder() + File.separator + "messages_" + language + ".yml");

        if (!f.exists()) {
            createFile(f);
        }
        FileConfiguration config = loadConfiguration(f);
        config.options().header(
                " ------------------------------ \n" +
                        "|      HuskHomes Messages      |\n" +
                        "|    Developed by William278   |\n" +
                        " ------------------------------ \n" +
                        "If you'd like to use a different language, you can change it in the config.yml \n" +
                        "Change the appearance/text of messages in the plugin using this config: \n" +
                        "1. Use the selection symbol (§) followed by a Minecraft color code to add color to the messages.\n" +
                        "2. Use an ampersand symbol and hashtag (&#) followed by a 6 digit hex code to add custom colors.\n" +
                        "3. Example 1: \"§cHello!\" will display \"Hello!\" in light red.\n" +
                        "4. Example 2: \"&#32CD32Hello\" will display \"Hello!\" in a bright green color (hex: #32CD32)");
        InputStream defaultMessageFile = plugin.getResource("Languages/" + language);
        if (defaultMessageFile != null) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultMessageFile, StandardCharsets.UTF_8));
            config.setDefaults(yamlConfiguration);
            config.options().copyHeader(true).copyDefaults(true);
        }
        saveFile(config, f);
        messages.clear();
        for (String message : config.getConfigurationSection("").getKeys(false)) {
            messages.put(message, StringEscapeUtils.unescapeJava(config.getString(message)));
        }
    }

    // Translate hexadecimal custom color codes
    private static String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    // Send a message with multiple placeholders
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
