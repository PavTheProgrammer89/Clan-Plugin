package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final IClanPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "clans.yml");
        loadConfig();
    }

    /**
     * Load configuration file
     */
    private void loadConfig() {
        if (!dataFile.exists()) {
            plugin.saveResource("clans.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Save data to file
     */
    public void saveData() {
        try {
            // Clear existing data
            dataConfig.set("clans", null);

            // Save all clans
            for (Clan clan : plugin.getClanManager().getAllClans()) {
                String path = "clans." + clan.getName();

                dataConfig.set(path + ".name", clan.getName());
                dataConfig.set(path + ".owner", clan.getOwner().toString());
                dataConfig.set(path + ".creation-time", clan.getCreationTime());

                // Save members
                List<String> memberStrings = new ArrayList<>();
                for (UUID member : clan.getMembers()) {
                    memberStrings.add(member.toString());
                }
                dataConfig.set(path + ".members", memberStrings);
            }

            // Save to file
            dataConfig.save(dataFile);

        } catch (IOException e) {
            plugin.getLogger().severe("Could not save clan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load data from file
     */
    public void loadData() {
        try {
            // Clear existing data
            plugin.getClanManager().clearData();

            // Check if clans section exists
            if (!dataConfig.contains("clans")) {
                plugin.getLogger().info("No clan data found to load.");
                return;
            }

            // Load all clans
            for (String clanName : dataConfig.getConfigurationSection("clans").getKeys(false)) {
                String path = "clans." + clanName;

                try {
                    // Load basic clan info
                    String name = dataConfig.getString(path + ".name");
                    String ownerString = dataConfig.getString(path + ".owner");
                    long creationTime = dataConfig.getLong(path + ".creation-time", System.currentTimeMillis());

                    if (name == null || ownerString == null) {
                        plugin.getLogger().warning("Invalid clan data for: " + clanName);
                        continue;
                    }

                    UUID owner = UUID.fromString(ownerString);

                    // Create clan object
                    Clan clan = new Clan(name, owner);
                    clan.setCreationTime(creationTime);

                    // Load members
                    List<String> memberStrings = dataConfig.getStringList(path + ".members");
                    clan.getMembers().clear(); // Clear the default owner member

                    for (String memberString : memberStrings) {
                        try {
                            UUID member = UUID.fromString(memberString);
                            clan.addMember(member);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid member UUID in clan " + name + ": " + memberString);
                        }
                    }

                    // Ensure owner is in members list
                    if (!clan.isMember(owner)) {
                        clan.addMember(owner);
                    }

                    // Load clan into manager
                    plugin.getClanManager().loadClan(clan);

                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load clan: " + clanName + " - " + e.getMessage());
                }
            }

            plugin.getLogger().info("Loaded " + plugin.getClanManager().getClanCount() + " clans.");

        } catch (Exception e) {
            plugin.getLogger().severe("Could not load clan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Backup current data
     */
    public void backupData() {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupFile = new File(backupDir, "clans_backup_" + timestamp + ".yml");

            if (dataFile.exists()) {
                dataConfig.save(backupFile);
                plugin.getLogger().info("Data backed up to: " + backupFile.getName());
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Could not backup clan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get data file
     */
    public File getDataFile() {
        return dataFile;
    }

    /**
     * Get data configuration
     */
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    /**
     * Reload configuration
     */
    public void reloadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Check if data file exists
     */
    public boolean dataFileExists() {
        return dataFile.exists();
    }

    /**
     * Get file size in bytes
     */
    public long getDataFileSize() {
        return dataFile.exists() ? dataFile.length() : 0;
    }

    /**
     * Delete data file (use with caution)
     */
    public boolean deleteDataFile() {
        if (dataFile.exists()) {
            return dataFile.delete();
        }
        return false;
    }

    /**
     * Create data directory if it doesn't exist
     */
    public void createDataDirectory() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
}