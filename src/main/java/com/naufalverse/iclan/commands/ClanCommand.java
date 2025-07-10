package com.naufalverse.iclan.commands;

import java.awt.*;
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
            case "list":
                handleList(player);
                break;
            case "chat":
            case "c":
                handleChat(player, args);
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

        // Check permissions
        if (!player.hasPermission("iclan.create")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to create clans!");
            return;
        }

        // Check if player is already in a clan
        if (plugin.getClanManager().getPlayerClan(playerUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are already in a clan!");
            return;
        }

        // Check if clan name already exists
        if (plugin.getClanManager().getClan(clanName) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "A clan with that name already exists!");
            return;
        }

        // Use config values for validation
        int minLength = plugin.getConfigManager().getMinNameLength();
        int maxLength = plugin.getConfigManager().getMaxNameLength();
        String pattern = "^[a-zA-Z0-9]{" + minLength + "," + maxLength + "}$";

        if (!clanName.matches(pattern)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Clan name must be " + minLength + "-" + maxLength + " characters long and contain only letters and numbers!");
            return;
        }

        // Create the clan
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

        // Check permissions
        if (!player.hasPermission("iclan.join")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to join clans!");
            return;
        }

        // Check if player is already in a clan
        if (plugin.getClanManager().getPlayerClan(playerUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You are already in a clan!");
            return;
        }

        // Check if clan exists
        Clan clan = plugin.getClanManager().getClan(clanName);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Clan '" + clanName + "' does not exist!");
            return;
        }

        // Check if player already has a pending invitation
        if (plugin.getInvitationManager().hasInvitation(playerUUID, clanName)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You already have a pending invitation to this clan!");
            return;
        }

        // Send invitation request
        plugin.getInvitationManager().addInvitation(playerUUID, clanName);

        // Notify ALL clan members (not just owner)
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

        // Check permissions
        if (!player.hasPermission("iclan.accept")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to accept clan members!");
            return;
        }

        // Check if player is in a clan (any member can accept)
        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You must be in a clan to accept members!");
            return;
        }

        // Get target player
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online!");
            return;
        }

        UUID targetUUID = targetPlayer.getUniqueId();

        // Check if target has invitation
        if (!plugin.getInvitationManager().hasInvitation(targetUUID, clan.getName())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' has not requested to join your clan!");
            return;
        }

        // Check if target is already in a clan
        if (plugin.getClanManager().getPlayerClan(targetUUID) != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is already in a clan!");
            plugin.getInvitationManager().removeInvitation(targetUUID, clan.getName());
            return;
        }

        // Add player to clan
        clan.addMember(targetUUID);
        plugin.getClanManager().addMemberToClan(targetUUID, clan.getName());
        plugin.getInvitationManager().removeInvitation(targetUUID, clan.getName());

        // Notify both players
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "You accepted " + ChatColor.YELLOW + targetName + ChatColor.GREEN + " into your clan!");

        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        targetPlayer.sendMessage(ChatColor.GREEN + "You have been accepted into clan: " + ChatColor.YELLOW + clan.getName());
        targetPlayer.sendMessage(ChatColor.GRAY + "Accepted by: " + ChatColor.WHITE + player.getName());
// Get the welcome message from config and replace %clan% with actual clan name
        String welcomeMessage = plugin.getConfigManager().getWelcomeMessage();
        welcomeMessage = welcomeMessage.replace("%clan%", clan.getName());
        welcomeMessage = ChatColor.translateAlternateColorCodes('&', welcomeMessage);
        targetPlayer.sendMessage(welcomeMessage);

        // Notify other clan members
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

        // Check permissions
        if (!player.hasPermission("iclan.info")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to view clan info!");
            return;
        }

        Clan clan;
        if (args.length == 1) {
            // Show own clan info
            clan = plugin.getClanManager().getPlayerClan(playerUUID);
            if (clan == null) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                player.sendMessage(ChatColor.RED + "You are not in a clan!");
                return;
            }
        } else {
            // Show specific clan info
            String clanName = args[1];
            clan = plugin.getClanManager().getClan(clanName);
            if (clan == null) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                player.sendMessage(ChatColor.RED + "Clan '" + clanName + "' does not exist!");
                return;
            }
        }

        // Display clan information
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GOLD + "=== Clan Information ===");
        player.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + clan.getName());

        Player owner = Bukkit.getPlayer(clan.getOwner());
        String ownerName = owner != null ? owner.getName() : "Unknown";
        player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + ownerName);

        player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + clan.getMemberCount());

        // List online members
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

        // Check permissions
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

        // Notify other clan members
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

        // Check permissions
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

        // Notify all members
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

        // Check permissions
        if (!player.hasPermission("iclan.kick")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to kick clan members!");
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

        // Notify other clan members
        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.playSound(member.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 0.8f);
                member.sendMessage(ChatColor.YELLOW + targetName + ChatColor.GRAY + " has been kicked from the clan.");
            }
        }
    }

    private void handleChat(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        // Check permissions
        if (!player.hasPermission("iclan.chat")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You don't have permission to use clan chat!");
            return;
        }

        // Check if player is in a clan
        Clan clan = plugin.getClanManager().getPlayerClan(playerUUID);
        if (clan == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "You must be in a clan to use clan chat!");
            return;
        }

        // Check if message provided
        if (args.length < 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Usage: /clan chat <message>");
            player.sendMessage(ChatColor.GRAY + "Or use: /clan c <message>");
            return;
        }

        // Build the message from all arguments after "chat"
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();

        // Format the clan chat message with dynamic prefix (clan name)
        String formattedMessage = ChatColor.BLACK + "[" + ChatColor.AQUA + clan.getName() + ChatColor.BLACK + "] "
                + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ": "
                + ChatColor.WHITE + message;

        // Send to all clan members
        int recipientCount = 0;
        for (UUID memberUUID : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.2f);
                member.sendMessage(formattedMessage);
                recipientCount++;
            }
        }

        // Silent monitoring for admins only (no indication to clan members)
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Only send to admins who aren't in the clan
            if (!clan.isMember(onlinePlayer.getUniqueId()) && isPlayerAdmin(onlinePlayer)) {
                String adminMessage = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "[" + ChatColor.AQUA + clan.getName() + ChatColor.LIGHT_PURPLE + "] "
                        + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ": "
                        + ChatColor.BLUE + message;
                onlinePlayer.sendMessage(adminMessage);
            }
        }
    }

    // Helper method to check if player is admin
    private boolean isPlayerAdmin(Player player) {
        return plugin.getConfigManager().isAdmin(player.getName()) || player.isOp();
    }

    private void handleList(Player player) {
        // Check permissions
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

        // Loop through each bad word
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
        player.sendMessage(ChatColor.YELLOW + "/clan accept <username>" + ChatColor.GRAY + " - Accept a player into your clan (any member)");
        player.sendMessage(ChatColor.YELLOW + "/clan info [name]" + ChatColor.GRAY + " - Show clan information");
        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.GRAY + " - Leave your current clan");
        player.sendMessage(ChatColor.YELLOW + "/clan kick <username>" + ChatColor.GRAY + " - Kick a member from your clan (owner only)");
        player.sendMessage(ChatColor.YELLOW + "/clan disband" + ChatColor.GRAY + " - Disband your clan (owner only)");
        player.sendMessage(ChatColor.YELLOW + "/clan chat <message>" + ChatColor.GRAY + " - Send a message to your clan");
        player.sendMessage(ChatColor.YELLOW + "/clan list" + ChatColor.GRAY + " - List all clans");
        player.sendMessage(ChatColor.GOLD + "=====================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("create", "join", "accept", "info", "leave", "kick", "disband", "list", "chat");
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "join":
                case "info":
                    // Add clan names
                    for (Clan clan : plugin.getClanManager().getAllClans()) {
                        if (clan.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(clan.getName());
                        }
                    }
                    break;
                case "accept":
                case "kick":
                    // Add online player names
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                    break;
                case "chat":
                case "c":
                case "chat hey there clan, how yall doing?":
                    break;
            }
        }

        return completions;
    }
}