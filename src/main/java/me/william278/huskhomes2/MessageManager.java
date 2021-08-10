package me.william278.huskhomes2;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.ChatMessageType;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

public class MessageManager {

    private static final Map<String, String> messages = new HashMap<>();

    private static final HuskHomes plugin = HuskHomes.getInstance();

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
                """
                         ------------------------------\s
                        |      HuskHomes Messages      |
                        |    Developed by William278   |
                         ------------------------------\s
                        If you'd like to use a different language, you can change it in the config.yml\s
                        Change the appearance/text of messages in the plugin using this config.\s
                        This config makes use of MineDown formatting, with extensive support for custom colors & formats.\s
                        For formatting help, see: https://github.com/Phoenix616/MineDown or check the HuskHomes Wiki.""");
        InputStream defaultMessageFile = plugin.getResource("languages/" + language + ".yml");
        if (defaultMessageFile != null) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultMessageFile, StandardCharsets.UTF_8));
            config.setDefaults(yamlConfiguration);
            config.options().copyHeader(true).copyDefaults(true);
        }
        saveFile(config, f);
        messages.clear();
        for (String message : config.getKeys(false)) {
            messages.put(message, StringEscapeUtils.unescapeJava(config.getString(message)));
        }
    }

    // Send a message with multiple placeholders
    public static void sendMessage(Player p, String messageID, String... placeholderReplacements) {
        sendMessage(p, ChatMessageType.CHAT, messageID, placeholderReplacements);
    }

    // Send a message with multiple placeholders
    public static void sendActionBar(Player p, String messageID, String... placeholderReplacements) {
        sendMessage(p, ChatMessageType.ACTION_BAR, messageID, placeholderReplacements);
    }

    // Send a message to the correct channel
    private static void sendMessage(Player p, ChatMessageType chatMessageType, String messageID, String... placeholderReplacements) {
        String message = getRawMessage(messageID);

        // Don't send empty messages
        if (StringUtils.isEmpty(message)) {
            return;
        }

        int replacementIndexer = 1;

        // Replace placeholders
        for (String replacement : placeholderReplacements) {
            String replacementString = "%" + replacementIndexer + "%";
            message = message.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }

        // Convert to baseComponents[] via MineDown formatting and send
        p.spigot().sendMessage(chatMessageType, new MineDown(message).replace().toComponent());
    }

    // Send a message with no placeholder parameters
    public static void sendMessage(Player p, String messageID) {
        String message = getRawMessage(messageID);

        // Don't send empty messages
        if (StringUtils.isEmpty(message)) {
            return;
        }

        // Convert to baseComponents[] via MineDown formatting and send
        p.spigot().sendMessage(new MineDown(message).replace().toComponent());
    }

    public static String getRawMessage(String messageID) {
        return messages.get(messageID);
    }

    public static String getRawMessage(String messageID, String... placeholderReplacements) {
        String message = messages.get(messageID);
        int replacementIndexer = 1;

        // Replace placeholders
        for (String replacement : placeholderReplacements) {
            String replacementString = "%" + replacementIndexer + "%";
            message = message.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }
        return message;
    }

}
