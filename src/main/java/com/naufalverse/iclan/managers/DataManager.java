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

    private void loadConfig() {
        if (!dataFile.exists()) {
            plugin.saveResource("clans.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            dataConfig.set("clans", null);

            for (Clan clan : plugin.getClanManager().getAllClans()) {
                String path = "clans." + clan.getName();

                dataConfig.set(path + ".name", clan.getName());
                dataConfig.set(path + ".owner", clan.getOwner().toString());
                dataConfig.set(path + ".creation-time", clan.getCreationTime());

                List<String> memberStrings = new ArrayList<>();
                for (UUID member : clan.getMembers()) {
                    memberStrings.add(member.toString());
                }
                dataConfig.set(path + ".members", memberStrings);
            }

            dataConfig.save(dataFile);

        } catch (IOException e) {
            plugin.getLogger().severe("Could not save clan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadData() {
        try {
            plugin.getClanManager().clearData();

            if (!dataConfig.contains("clans")) {
                plugin.getLogger().info("No clan data found to load.");
                return;
            }

            for (String clanName : dataConfig.getConfigurationSection("clans").getKeys(false)) {
                String path = "clans." + clanName;

                try {
                    String name = dataConfig.getString(path + ".name");
                    String ownerString = dataConfig.getString(path + ".owner");
                    long creationTime = dataConfig.getLong(path + ".creation-time", System.currentTimeMillis());

                    if (name == null || ownerString == null) {
                        plugin.getLogger().warning("Invalid clan data for: " + clanName);
                        continue;
                    }

                    UUID owner = UUID.fromString(ownerString);

                    Clan clan = new Clan(name, owner);
                    clan.setCreationTime(creationTime);

                    List<String> memberStrings = dataConfig.getStringList(path + ".members");
                    clan.getMembers().clear();

                    for (String memberString : memberStrings) {
                        try {
                            UUID member = UUID.fromString(memberString);
                            clan.addMember(member);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid member UUID in clan " + name + ": " + memberString);
                        }
                    }

                    if (!clan.isMember(owner)) {
                        clan.addMember(owner);
                    }

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

    public File getDataFile() {
        return dataFile;
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public void reloadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean dataFileExists() {
        return dataFile.exists();
    }

    public long getDataFileSize() {
        return dataFile.exists() ? dataFile.length() : 0;
    }

    public boolean deleteDataFile() {
        if (dataFile.exists()) {
            return dataFile.delete();
        }
        return false;
    }

    public void createDataDirectory() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
}