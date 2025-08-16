package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationManager {

    private final IClanPlugin plugin;
    private final Map<UUID, Set<String>> invitations;
    private final Map<String, Long> invitationTime;

    private static final long INVITATION_TIMEOUT = 300000;

    public InvitationManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.invitations = new ConcurrentHashMap<>();
        this.invitationTime = new ConcurrentHashMap<>();

        startCleanupTask();
    }

    public void addInvitation(UUID playerUUID, String clanName) {
        invitations.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(clanName.toLowerCase());
        invitationTime.put(getInvitationKey(playerUUID, clanName), System.currentTimeMillis());
    }

    public void removeInvitation(UUID playerUUID, String clanName) {
        Set<String> playerInvitations = invitations.get(playerUUID);
        if (playerInvitations != null) {
            playerInvitations.remove(clanName.toLowerCase());
            if (playerInvitations.isEmpty()) {
                invitations.remove(playerUUID);
            }
        }
        invitationTime.remove(getInvitationKey(playerUUID, clanName));
    }

    public boolean hasInvitation(UUID playerUUID, String clanName) {
        Set<String> playerInvitations = invitations.get(playerUUID);
        if (playerInvitations != null) {
            return playerInvitations.contains(clanName.toLowerCase());
        }
        return false;
    }

    public Set<String> getPlayerInvitations(UUID playerUUID) {
        return invitations.getOrDefault(playerUUID, new HashSet<>());
    }

    public Set<UUID> getClanInvitations(String clanName) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, Set<String>> entry : invitations.entrySet()) {
            if (entry.getValue().contains(clanName.toLowerCase())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void removeAllPlayerInvitations(UUID playerUUID) {
        Set<String> playerInvitations = invitations.remove(playerUUID);
        if (playerInvitations != null) {
            for (String clanName : playerInvitations) {
                invitationTime.remove(getInvitationKey(playerUUID, clanName));
            }
        }
    }

    public void removeAllClanInvitations(String clanName) {
        Iterator<Map.Entry<UUID, Set<String>>> iterator = invitations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Set<String>> entry = iterator.next();
            Set<String> playerInvitations = entry.getValue();

            if (playerInvitations.remove(clanName.toLowerCase())) {
                invitationTime.remove(getInvitationKey(entry.getKey(), clanName));
            }

            if (playerInvitations.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public int getInvitationCount(UUID playerUUID) {
        Set<String> playerInvitations = invitations.get(playerUUID);
        return playerInvitations != null ? playerInvitations.size() : 0;
    }

    public int getTotalInvitationCount() {
        return invitations.values().stream().mapToInt(Set::size).sum();
    }

    public boolean isInvitationExpired(UUID playerUUID, String clanName) {
        Long timestamp = invitationTime.get(getInvitationKey(playerUUID, clanName));
        if (timestamp == null) {
            return true;
        }
        return (System.currentTimeMillis() - timestamp) > INVITATION_TIMEOUT;
    }

    private String getInvitationKey(UUID playerUUID, String clanName) {
        return playerUUID.toString() + ":" + clanName.toLowerCase();
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredInvitations, 6000L, 6000L);
    }

    private void cleanupExpiredInvitations() {
        long currentTime = System.currentTimeMillis();
        List<String> expiredKeys = new ArrayList<>();

        for (Map.Entry<String, Long> entry : invitationTime.entrySet()) {
            if ((currentTime - entry.getValue()) > INVITATION_TIMEOUT) {
                expiredKeys.add(entry.getKey());
            }
        }

        for (String key : expiredKeys) {
            String[] parts = key.split(":");
            if (parts.length == 2) {
                UUID playerUUID = UUID.fromString(parts[0]);
                String clanName = parts[1];

                removeInvitation(playerUUID, clanName);

                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.YELLOW + "Your invitation to clan '" + clanName + "' has expired.");
                }
            }
        }
    }

    public long getRemainingTime(UUID playerUUID, String clanName) {
        Long timestamp = invitationTime.get(getInvitationKey(playerUUID, clanName));
        if (timestamp == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - timestamp;
        return Math.max(0, INVITATION_TIMEOUT - elapsed);
    }

    public String formatRemainingTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "Expired";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    public void clearAllInvitations() {
        invitations.clear();
        invitationTime.clear();
    }
}