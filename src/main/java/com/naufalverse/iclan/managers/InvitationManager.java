package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationManager {
    
    private final IClanPlugin plugin;
    private final Map<UUID, Set<String>> invitations; // player UUID -> set of clan names
    private final Map<String, Long> invitationTime; // invitation key -> timestamp
    
    private static final long INVITATION_TIMEOUT = 300000; // 5 minutes in milliseconds
    
    public InvitationManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.invitations = new ConcurrentHashMap<>();
        this.invitationTime = new ConcurrentHashMap<>();
        
        // Start cleanup task
        startCleanupTask();
    }
    
    /**
     * Add an invitation
     */
    public void addInvitation(UUID playerUUID, String clanName) {
        invitations.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(clanName.toLowerCase());
        invitationTime.put(getInvitationKey(playerUUID, clanName), System.currentTimeMillis());
    }
    
    /**
     * Remove an invitation
     */
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
    
    /**
     * Check if a player has an invitation to a specific clan
     */
    public boolean hasInvitation(UUID playerUUID, String clanName) {
        Set<String> playerInvitations = invitations.get(playerUUID);
        if (playerInvitations != null) {
            return playerInvitations.contains(clanName.toLowerCase());
        }
        return false;
    }
    
    /**
     * Get all invitations for a player
     */
    public Set<String> getPlayerInvitations(UUID playerUUID) {
        return invitations.getOrDefault(playerUUID, new HashSet<>());
    }
    
    /**
     * Get all players with invitations to a specific clan
     */
    public Set<UUID> getClanInvitations(String clanName) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, Set<String>> entry : invitations.entrySet()) {
            if (entry.getValue().contains(clanName.toLowerCase())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    /**
     * Remove all invitations for a player
     */
    public void removeAllPlayerInvitations(UUID playerUUID) {
        Set<String> playerInvitations = invitations.remove(playerUUID);
        if (playerInvitations != null) {
            for (String clanName : playerInvitations) {
                invitationTime.remove(getInvitationKey(playerUUID, clanName));
            }
        }
    }
    
    /**
     * Remove all invitations for a clan
     */
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
    
    /**
     * Get invitation count for a player
     */
    public int getInvitationCount(UUID playerUUID) {
        Set<String> playerInvitations = invitations.get(playerUUID);
        return playerInvitations != null ? playerInvitations.size() : 0;
    }
    
    /**
     * Get total invitation count
     */
    public int getTotalInvitationCount() {
        return invitations.values().stream().mapToInt(Set::size).sum();
    }
    
    /**
     * Check if invitation has expired
     */
    public boolean isInvitationExpired(UUID playerUUID, String clanName) {
        Long timestamp = invitationTime.get(getInvitationKey(playerUUID, clanName));
        if (timestamp == null) {
            return true;
        }
        return (System.currentTimeMillis() - timestamp) > INVITATION_TIMEOUT;
    }
    
    /**
     * Get invitation key for internal use
     */
    private String getInvitationKey(UUID playerUUID, String clanName) {
        return playerUUID.toString() + ":" + clanName.toLowerCase();
    }
    
    /**
     * Start cleanup task to remove expired invitations
     */
    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredInvitations, 6000L, 6000L); // Run every 5 minutes
    }
    
    /**
     * Cleanup expired invitations
     */
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
                
                // Notify player if online
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.YELLOW + "Your invitation to clan '" + clanName + "' has expired.");
                }
            }
        }
    }
    
    /**
     * Get remaining time for an invitation
     */
    public long getRemainingTime(UUID playerUUID, String clanName) {
        Long timestamp = invitationTime.get(getInvitationKey(playerUUID, clanName));
        if (timestamp == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - timestamp;
        return Math.max(0, INVITATION_TIMEOUT - elapsed);
    }
    
    /**
     * Format remaining time as string
     */
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
    
    /**
     * Clear all invitations
     */
    public void clearAllInvitations() {
        invitations.clear();
        invitationTime.clear();
    }
}