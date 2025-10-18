package com.naufalverse.iclan;

import com.naufalverse.iclan.commands.ClanCommand;
import com.naufalverse.iclan.managers.ClanManager;
import com.naufalverse.iclan.managers.ConfigManager;
import com.naufalverse.iclan.managers.DataManager;
import com.naufalverse.iclan.managers.InvitationManager;
import com.naufalverse.iclan.objects.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class IClanPlugin extends JavaPlugin implements Listener {

    private ClanManager clanManager;
    private DataManager dataManager;
    private InvitationManager invitationManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.clanManager = new ClanManager(this);
        this.invitationManager = new InvitationManager(this);

        dataManager.loadData();

        if (getCommand("clan") != null) {
            getCommand("clan").setExecutor(new ClanCommand(this));
            getCommand("clan").setTabCompleter(new ClanCommand(this));
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("✅ iClan has been enabled successfully!");
        getLogger().info("Loaded " + clanManager.getClanCount() + " clans.");
        getLogger().info("Config loaded with " + configManager.getAdmins().size() + " admins.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("❌ iClan has been disabled!");
    }

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
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        String prefix = (clan != null)
                ? ChatColor.BLACK + "[" + ChatColor.AQUA + clan.getName() + ChatColor.BLACK + "] "
                : ChatColor.DARK_GRAY + "[NoClan] ";

        // Asynchronous-safe formatting: first %s = player name, second %s = message
        event.setFormat(prefix + ChatColor.WHITE + "%s: %s");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // delay a tick or two to avoid timing issues with tab/player state
        Bukkit.getScheduler().runTaskLater(this, () -> updatePlayerTab(event.getPlayer()), 10L);
    }

    public void updatePlayerTab(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        String prefix = (clan != null) ? ChatColor.AQUA + "[" + clan.getName() + "] " : "";

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String uuidPart = player.getUniqueId().toString().replace("-", "");
        String teamName = "clan_" + (uuidPart.length() > 12 ? uuidPart.substring(0, 12) : uuidPart);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        // Set prefix (Bukkit limits may apply)
        team.setPrefix(prefix);

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // Also set the player list name
        try {
            player.setPlayerListName(prefix + player.getName());
        } catch (Exception ignored) {
            // fallback if setting player list name fails
        }
    }

    public boolean isAdmin(Player player) {
        return configManager.isAdmin(player.getName()) || player.isOp();
    }
}
