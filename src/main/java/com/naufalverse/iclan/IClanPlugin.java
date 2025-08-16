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
        this.configManager = new ConfigManager(this);

        this.dataManager = new DataManager(this);
        this.clanManager = new ClanManager(this);
        this.invitationManager = new InvitationManager(this);

        dataManager.loadData();

        getCommand("clan").setExecutor(new ClanCommand(this));

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("iClan plugin has been enabled!");
        getLogger().info("Loaded " + clanManager.getClanCount() + " clans.");
        getLogger().info("Config loaded with " + configManager.getAdmins().size() + " admins.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }

        getLogger().info("iClan plugin has been disabled!");
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
        String message = event.getMessage();

        checkBadWords(player, message, event);

        if (event.isCancelled()) {
            return;
        }

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

    private void checkBadWords(Player player, String message, AsyncPlayerChatEvent event) {
        List<String> badWords = configManager.getBadWords();

        for (String badWord : badWords) {
            if (message.toLowerCase().contains(badWord.toLowerCase())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Watch your language!");
            }
        }
    }

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

            String teamName = "clan_" + player.getUniqueId().toString().substring(0, 8);

            Team oldTeam = scoreboard.getTeam(teamName);
            if (oldTeam != null) {
                oldTeam.unregister();
            }

            Team team = scoreboard.registerNewTeam(teamName);
            team.setPrefix(clanPrefix);
            team.addEntry(player.getName());

            player.setPlayerListName(team.getPrefix() + player.getName());

        } catch (Exception e) {
            player.setPlayerListName(clanPrefix + player.getName());
        }
    }

    public void updatePlayerTab(Player player) {
        updatePlayerTabName(player);
    }

    private boolean isPlayerAdmin(Player player) {
        return configManager.isAdmin(player.getName()) || player.isOp();
    }
}