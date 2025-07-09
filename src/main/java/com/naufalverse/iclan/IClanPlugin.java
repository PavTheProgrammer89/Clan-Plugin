package com.naufalverse.iclan;

import org.bukkit.plugin.java.JavaPlugin;
import com.naufalverse.iclan.commands.ClanCommand;
import com.naufalverse.iclan.managers.ClanManager;
import com.naufalverse.iclan.managers.DataManager;
import com.naufalverse.iclan.managers.InvitationManager;
import com.naufalverse.iclan.managers.ConfigManager;

public class IClanPlugin extends JavaPlugin {

    private ClanManager clanManager;
    private DataManager dataManager;
    private InvitationManager invitationManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize config manager first
        this.configManager = new ConfigManager(this);

        // Initialize other managers
        this.dataManager = new DataManager(this);
        this.clanManager = new ClanManager(this);
        this.invitationManager = new InvitationManager(this);

        // Load data
        dataManager.loadData();

        // Register commands
        getCommand("clan").setExecutor(new ClanCommand(this));

        getLogger().info("iClan plugin has been enabled!");
        getLogger().info("Loaded " + clanManager.getClanCount() + " clans.");
        getLogger().info("Config loaded with " + configManager.getAdmins().size() + " admins.");
    }

    @Override
    public void onDisable() {
        // Save data
        if (dataManager != null) {
            dataManager.saveData();
        }

        getLogger().info("iClan plugin has been disabled!");
    }

    // Getter methods
    public ClanManager getClanManager() {
        return clanManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public InvitationManager getInvitationManager() {
        return invitationManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}