package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final IClanPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(IClanPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public int getMaxMembers() {
        return config.getInt("clan.max-members", 10);
    }

    public int getMinNameLength() {
        return config.getInt("clan.min-name-length", 3);
    }

    public int getMaxNameLength() {
        return config.getInt("clan.max-name-length", 16);
    }

    public boolean allowColorCodes() {
        return config.getBoolean("clan.allow-color-codes", false);
    }

    public boolean isAutoSaveEnabled() {
        return config.getBoolean("clan.auto-save", true);
    }

    public String getChatPrefix() {
        return config.getString("chat.prefix", "%clan%");
    }

    public String getChatPrefix(String clanName) {
        String prefix = getChatPrefix();
        return prefix.replace("%clan%", clanName);
    }

    public String getSpyPrefix() {
        return config.getString("chat.spy-prefix", "[SPY]");
    }

    public boolean isSpyModeEnabled() {
        return config.getBoolean("chat.enable-spy-mode", true);
    }

    public int getChatCooldown() {
        return config.getInt("chat.chat-cooldown", 0);
    }

    public boolean areSoundsEnabled() {
        return config.getBoolean("sounds.enable-sounds", true);
    }

    public String getSuccessSound() {
        return config.getString("sounds.success-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public String getErrorSound() {
        return config.getString("sounds.error-sound", "BLOCK_NOTE_BLOCK_BASS");
    }

    public String getNotificationSound() {
        return config.getString("sounds.notification-sound", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getChatSound() {
        return config.getString("sounds.chat-sound", "BLOCK_NOTE_BLOCK_CHIME");
    }

    public List<String> getAdmins() {
        return config.getStringList("admins");
    }

    public boolean isAdmin(String playerName) {
        return getAdmins().contains(playerName);
    }

    public void addAdmin(String playerName) {
        List<String> admins = getAdmins();
        if (!admins.contains(playerName)) {
            admins.add(playerName);
            config.set("admins", admins);
            saveConfig();
        }
    }

    public void removeAdmin(String playerName) {
        List<String> admins = getAdmins();
        if (admins.contains(playerName)) {
            admins.remove(playerName);
            config.set("admins", admins);
            saveConfig();
        }
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "&cMessage not found: " + key);
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "&6[iClan] &r");
    }

    public String getDatabaseType() {
        return config.getString("database.type", "YAML");
    }

    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return config.getString("database.database", "iclan");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public String getWelcomeMessage() {
        return config.getString("welcome-message", "&cWelcome to %clan%&c!");
    }

    public List<String> getBadWords() {
        return config.getStringList("badwords");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "");
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("plugin.debug", false);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}