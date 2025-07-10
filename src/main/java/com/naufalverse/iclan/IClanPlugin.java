package com.naufalverse.iclan;

import com.naufalverse.iclan.objects.Clan;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.naufalverse.iclan.commands.ClanCommand;
import com.naufalverse.iclan.managers.ClanManager;
import com.naufalverse.iclan.managers.DataManager;
import com.naufalverse.iclan.managers.InvitationManager;
import com.naufalverse.iclan.managers.ConfigManager;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Async;
import org.w3c.dom.events.Event;

import java.util.logging.Level;

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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();

        Clan clan = getClanManager().getClan(String.valueOf(player));

        String clanPrefix = (clan != null) ? ChatColor.BLACK + "[" + ChatColor.AQUA + clan.getName() + ChatColor.BLACK + "]" : "[NoClan]";
        String formatted = clanPrefix + " <" + playerName + ">: " + message;

        event.setFormat(formatted);
    }

    public class IClanPlugin1 extends JavaPlugin implements Listener {

        @Override
        public void onEnable() {
            getServer().getPluginManager().registerEvents((Listener) this, this);
        }

        public Clan getPlayerClan(Player player) {
            return getClanManager().getClan(String.valueOf(player));
        }

        public void updatePlayerTabName(Player player) {
            Clan clan = getPlayerClan(player);
            String clanPrefix = (clan != null) ? ChatColor.GRAY + "[" + clan.getName() + "] " : "";

            Scoreboard scoreboard = player.getScoreboard();

            // Remove old team if it exists
            Team oldTeam = scoreboard.getTeam(player.getName());
            if (oldTeam != null) oldTeam.unregister();

            // Create new team with player name as ID (unique per player)
            Team team = scoreboard.registerNewTeam(player.getName());
            team.setPrefix(clanPrefix);
            team.addEntry(player.getName());

            // Set in tab list
            player.setPlayerListName(team.getPrefix() + player.getName());
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            updatePlayerTabName(player);
        }
    }

}