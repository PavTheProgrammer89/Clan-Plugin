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

import java.util.List;

public class IClanPlugin extends JavaPlugin implements Listener {

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

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

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
        String message = event.getMessage();

        // Check for bad words first
        checkBadWords(player, message, event);

        // If message was cancelled, don't continue
        if (event.isCancelled()) {
            return;
        }

        // Get player's clan correctly
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());

        String clanPrefix = (clan != null) ?
                ChatColor.BLACK + "[" + ChatColor.AQUA + clan.getName() + ChatColor.BLACK + "] " :
                ChatColor.GRAY + "[NoClan] ";

        String formatted = clanPrefix + player.getName() + ChatColor.WHITE + ": " + message;
        event.setFormat(formatted);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerTabName(player);
    }

    // Check for bad words in chat
    private void checkBadWords(Player player, String message, AsyncPlayerChatEvent event) {
        List<String> badWords = configManager.getBadWords();

        // Loop through each bad word
        for (String badWord : badWords) {
            if (message.toLowerCase().contains(badWord.toLowerCase())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Watch your language!");
                // No return here - keep checking other words
            }
        }
    }

    // Update player's name in tab list with clan prefix
    private void updatePlayerTabName(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        String clanPrefix = (clan != null) ?
                ChatColor.AQUA + "[" + clan.getName() + "] " : "";

        try {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                scoreboard = getServer().getScoreboardManager().getNewScoreboard();
                player.setScoreboard(scoreboard);
            }

            // Create unique team name using player UUID to avoid conflicts
            String teamName = "clan_" + player.getUniqueId().toString().substring(0, 8);

            // Remove old team if it exists
            Team oldTeam = scoreboard.getTeam(teamName);
            if (oldTeam != null) {
                oldTeam.unregister();
            }

            // Create new team with unique name
            Team team = scoreboard.registerNewTeam(teamName);
            team.setPrefix(clanPrefix);
            team.addEntry(player.getName());

            // Set in tab list
            player.setPlayerListName(team.getPrefix() + player.getName());

        } catch (Exception e) {
            // If scoreboard fails, just set player list name directly
            player.setPlayerListName(clanPrefix + player.getName());
        }
    }

    // Public method to update tab name when clan membership changes
    public void updatePlayerTab(Player player) {
        updatePlayerTabName(player);
    }

    // Helper method to check if player is admin
    private boolean isPlayerAdmin(Player player) {
        return configManager.isAdmin(player.getName()) || player.isOp();
    }
}