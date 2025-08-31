package com.naufalverse.iclan.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;
import org.bukkit.event.player.PlayerChatEvent;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final IClanPlugin plugin;

    public ClanCommand(IClanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use clan commands!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "accept":
                handleAccept(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "ban":
                handleBan(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "chat":
            case "c":
                handleChat(player, args);
                break;
            case "help":
                sendHelpMessage(player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length != 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan create <name>");
            return;
        }

        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.create")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to create clans!");
            return;
        }

        if (plugin.getClanManager().getPlayerClan(playerUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are already in a clan!");
            return;
        }

        if (plugin.getClanManager().getClan(clanName) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "A clan with that name already exists!");
            return;
        }

        int minLength = plugin.getConfigManager().getMinNameLength();
        int maxLength = plugin.getConfigManager().getMaxNameLength();
        String pattern = "^[a-zA-Z0-9]{" + minLength + "," + maxLength + "}$";

        if (!clanName.matches(pattern)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Clan name must be " + minLength + "-" + maxLength + " characters long and contain only letters and numbers!");
            return;
        }

        Clan clan = new Clan(clanName, playerUUID);
        plugin.getClanManager().createClan(clan);

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "Successfully created clan: " + ChatColor.YELLOW + clanName);
        player.sendMessage(ChatColor.GRAY + "You are now the owner of this clan!");
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length != 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan join <name>");
            return;
        }

        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.join")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to join clans!");
            return;
        }

        if (plugin.getClanManager().getPlayerClan(playerUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are already in a clan!");
            return;
        }

        Clan clan = plugin.getClanManager().getClan(clanName);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Clan '" + clanName + "' does not exist!");
            return;
        }

        if (plugin.getInvitationManager().hasInvitation(playerUUID, clanName)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You already have a pending invitation to this clan!");
            return;
        }

        plugin.getInvitationManager().addInvitation(playerUUID, clanName);

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                member.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " wants to join your clan!");
                member.sendMessage(ChatColor.GRAY + "Use " + ChatColor.WHITE + "/clan accept " + player.getName() + ChatColor.GRAY + " to accept them.");
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Join request sent to clan: " + ChatColor.YELLOW + clanName);
        player.sendMessage(ChatColor.GRAY + "Wait for any clan member to accept your request.");
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length != 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan accept <username>");
            return;
        }

        String targetName = args[1];
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.accept")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to accept clan members!");
            return;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You must be in a clan to accept members!");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online!");
            return;
        }

        UUID targetUUID = targetPlayer.getUniqueId();

        // If your ClanManager exposes a global ban check, keep this. Otherwise, change to `clan.isBanned(targetUUID)`.
        if (plugin.getClanManager().isBanned(targetUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You cannot accept banned users!");
            return;
        }

        if (!plugin.getInvitationManager().hasInvitation(targetUUID, clan.getName())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' has not requested to join your clan!");
            return;
        }

        if (plugin.getClanManager().getPlayerClan(targetUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is already in a clan!");
            plugin.getInvitationManager().removeInvitation(targetUUID, clan.getName());
            return;
        }

        clan.addMember(targetUUID);
        plugin.getClanManager().addMemberToClan(targetUUID, clan.getName());
        plugin.getInvitationManager().removeInvitation(targetUUID, clan.getName());

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "You accepted " + ChatColor.YELLOW + targetName + ChatColor.GREEN + " into your clan!");

        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        targetPlayer.sendMessage(ChatColor.GREEN + "You have been accepted into clan: " + ChatColor.YELLOW + clan.getName());
        targetPlayer.sendMessage(ChatColor.GRAY + "Accepted by: " + ChatColor.WHITE + player.getName());

        String welcomeMessage = plugin.getConfigManager().getWelcomeMessage();
        welcomeMessage = welcomeMessage.replace("%clan%", clan.getName());
        welcomeMessage = ChatColor.translateAlternateColorCodes('&', welcomeMessage);
        targetPlayer.sendMessage(welcomeMessage);

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline() && !member.equals(player) && !member.equals(targetPlayer)) {
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
                member.sendMessage(ChatColor.YELLOW + targetName + ChatColor.GRAY + " has joined the clan!");
                member.sendMessage(ChatColor.GRAY + "Accepted by: " + ChatColor.WHITE + player.getName());
            }
        }
    }

    private void handleInfo(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.info")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to view clan info!");
            return;
        }

        Clan clan;
        if (args.length == 1) {
            clan = plugin.getClanManager().getPlayerClan(playerUUID);
            if (clan == null) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                player.sendMessage(ChatColor.RED + "You are not in a clan!");
                return;
            }
        } else {
            String clanName = args[1];
            clan = plugin.getClanManager().getClan(clanName);
            if (clan == null) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                player.sendMessage(ChatColor.RED + "Clan '" + clanName + "' does not exist!");
                return;
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GOLD + "=== Clan Information ===");
        player.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + clan.getName());

        Player owner = Bukkit.getPlayer(clan.getOwner());
        String ownerName = owner != null ? owner.getName() : "Unknown";
        player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + ownerName);

        player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + clan.getMemberCount());

        List<String> onlineMembers = new ArrayList<>();
        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                onlineMembers.add(member.getName());
            }
        }

        if (!onlineMembers.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Online: " + ChatColor.WHITE + String.join(", ", onlineMembers));
        }

        player.sendMessage(ChatColor.GOLD + "=======================");
    }

    private void handleLeave(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.leave")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to leave clans!");
            return;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are not in a clan!");
            return;
        }

        if (clan.isOwner(playerUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You cannot leave your own clan! Use /clan disband to delete it.");
            return;
        }

        clan.removeMember(playerUUID);
        plugin.getClanManager().removeMemberFromClan(playerUUID);

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        player.sendMessage(ChatColor.GREEN + "You have left the clan: " + ChatColor.YELLOW + clan.getName());

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
                member.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has left the clan.");
            }
        }
    }

    private void handleDisband(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.disband")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to disband clans!");
            return;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are not in a clan!");
            return;
        }

        if (!clan.isOwner(playerUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Only the clan owner can disband the clan!");
            return;
        }

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.playSound(member.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.8f);
                member.sendMessage(ChatColor.RED + "Your clan has been disbanded by the owner.");
            }
        }

        plugin.getClanManager().deleteClan(clan.getName());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "Successfully disbanded clan: " + ChatColor.YELLOW + clan.getName());
    }

    private void handleKick(Player player, String[] args) {
        if (args.length != 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan kick <username>");
            return;
        }

        String targetName = args[1];
        UUID playerUUID = player.getUniqueId();

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are not in a clan!");
            return;
        }

        if (!clan.isOwner(playerUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Only the clan owner can kick members!");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online!");
            return;
        }

        UUID targetUUID = targetPlayer.getUniqueId();

        if (!clan.isMember(targetUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not in your clan!");
            return;
        }

        Clan targetPlayerClan = plugin.getClanManager().getPlayerClan(targetUUID);
        if (targetPlayerClan == null || !targetPlayerClan.getName().equals(clan.getName())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Security check failed: Player is not in your clan!");
            return;
        }

        if (clan.isOwner(targetUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You cannot kick yourself!");
            return;
        }

        clan.removeMember(targetUUID);
        plugin.getClanManager().removeMemberFromClan(targetUUID);

        player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_CELEBRATE, 0.8f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "You kicked " + ChatColor.YELLOW + targetName + ChatColor.GREEN + " from the clan.");

        targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.8f);
        targetPlayer.sendMessage(ChatColor.RED + "You have been kicked from clan: " + ChatColor.YELLOW + clan.getName());

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.playSound(member.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 0.8f);
                member.sendMessage(ChatColor.YELLOW + targetName + ChatColor.GRAY + " has been kicked from the clan.");
            }
        }
    }

    private void handleBan(Player player, String[] args) {
        if (args.length != 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan ban <username>");
            return;
        }

        String targetName = args[1];
        UUID playerUUID = player.getUniqueId();

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are not in a clan!");
            return;
        }

        if (!clan.isOwner(playerUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Only the clan owner can ban members!");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online!");
            return;
        }

        UUID targetUUID = targetPlayer.getUniqueId();

        if (!clan.isMember(targetUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not in your clan!");
            return;
        }

        if (clan.isOwner(targetUUID)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You cannot ban yourself!");
            return;
        }

        clan.removeMember(targetUUID);
        plugin.getClanManager().removeMemberFromClan(targetUUID);
        plugin.getClanManager().banPlayer(targetUUID);

        player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_CELEBRATE, 0.8f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "You banned " + ChatColor.YELLOW + targetName + ChatColor.GREEN + " from the clan.");

        targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.8f);
        targetPlayer.sendMessage(ChatColor.RED + "You have been banned from clan: " + ChatColor.YELLOW + clan.getName());

        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.playSound(member.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 0.8f);
                member.sendMessage(ChatColor.YELLOW + targetName + ChatColor.GRAY + " has been banned from the clan.");
            }
        }
    }

    private void handleChat(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("iclan.chat")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to use clan chat!");
            return;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You must be in a clan to use clan chat!");
            return;
        }

        if (args.length < 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan chat <message>");
            player.sendMessage(ChatColor.GRAY + "Or use: /clan c <message>");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();

        String formattedMessage = ChatColor.BLACK + "[" + ChatColor.AQUA + clan.getName() + ChatColor.BLACK + "] "
                + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ": "
                + ChatColor.WHITE + message;

        int recipientCount = 0;
        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.2f);
                member.sendMessage(formattedMessage);
                recipientCount++;
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!clan.isMember(onlinePlayer.getUniqueId()) && isPlayerAdmin(onlinePlayer)) {
                String adminMessage = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "[" + ChatColor.AQUA + clan.getName() + ChatColor.LIGHT_PURPLE + "] "
                        + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ": "
                        + ChatColor.BLUE + message;
                onlinePlayer.sendMessage(adminMessage);
            }
        }
    }

    private boolean isPlayerAdmin(Player player) {
        return plugin.getConfigManager().isAdmin(player.getName()) || player.isOp();
    }

    private void handleList(Player player) {
        if (!player.hasPermission("iclan.list")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to list clans!");
            return;
        }

        List<Clan> clans = plugin.getClanManager().getAllClans();

        if (clans.isEmpty()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.YELLOW + "No clans exist yet!");
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GOLD + "=== Clan List ===");
        for (Clan clan : clans) {
            Player owner = Bukkit.getPlayer(clan.getOwner());
            String ownerName = owner != null ? owner.getName() : "Unknown";
            player.sendMessage(ChatColor.YELLOW + clan.getName() + ChatColor.GRAY + " - Owner: " + ChatColor.WHITE + ownerName + ChatColor.GRAY + " - Members: " + ChatColor.WHITE + clan.getMemberCount());
        }
        player.sendMessage(ChatColor.GOLD + "================");
    }

    private void checkBadWords(Player player, String message, PlayerChatEvent event) {
        List<String> badWords = plugin.getConfigManager().getBadWords();

        for (String badWord : badWords) {
            if (message.toLowerCase().contains(badWord.toLowerCase())) {
                event.setCancelled(true);
            }
        }
    }

    private void sendHelpMessage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GOLD + "=== iClan Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/clan create <name>" + ChatColor.GRAY + " - Create a new clan");
        player.sendMessage(ChatColor.YELLOW + "/clan join <name>" + ChatColor.GRAY + " - Request to join a clan");
        player.sendMessage(ChatColor.YELLOW + "/clan accept <username>" + ChatColor.GRAY + " - Accept a player into your clan (any member unless user is banned)");
        player.sendMessage(ChatColor.YELLOW + "/clan info [name]" + ChatColor.GRAY + " - Show clan information");
        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.GRAY + " - Leave your current clan");
        player.sendMessage(ChatColor.YELLOW + "/clan kick <username>" + ChatColor.GRAY + " - Kick a member from your clan (owner only)");
        player.sendMessage(ChatColor.YELLOW + "/clan ban <username>" + ChatColor.GRAY + " - Ban a member from your clan (owner only)");
        player.sendMessage(ChatColor.YELLOW + "/clan disband" + ChatColor.GRAY + " - Disband your clan (owner only)");
        player.sendMessage(ChatColor.YELLOW + "/clan chat <message>" + ChatColor.GRAY + " - Send a message to your clan");
        player.sendMessage(ChatColor.YELLOW + "/clan list" + ChatColor.GRAY + " - List all clans");
        player.sendMessage(ChatColor.GOLD + "=====================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("create", "join", "accept", "info", "leave", "kick", "ban", "disband", "list", "chat", "help");
            String prefix = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(prefix)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String prefix = args[1].toLowerCase();

            switch (sub) {
                case "join":
                case "info":
                    for (Clan clan : plugin.getClanManager().getAllClans()) {
                        if (clan.getName().toLowerCase().startsWith(prefix)) {
                            completions.add(clan.getName());
                        }
                    }
                    break;

                case "accept":
                case "kick":
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(prefix)) {
                            completions.add(p.getName());
                        }
                    }
                    break;

                case "ban":
                    if (sender instanceof Player) {
                        Player exec = (Player) sender;
                        Clan clan = plugin.getClanManager().getPlayerClan(exec.getUniqueId());
                        if (clan != null) {
                            for (UUID memberUUID : clan.getMembers()) {
                                if (memberUUID.equals(exec.getUniqueId())) continue;

                                Player member = Bukkit.getPlayer(memberUUID);
                                if (member != null && member.isOnline()) {
                                    String name = member.getName();
                                    if (name.toLowerCase().startsWith(prefix)) {
                                        completions.add(name);
                                    }
                                }
                            }
                        }
                    }
                    break;

                case "chat":
                case "c":
                    break;
            }
        }

        return completions;
    }
}
