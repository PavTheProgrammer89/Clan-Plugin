package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;

import java.util.*;

public class ClanManager {
    
    private final IClanPlugin plugin;
    private final Map<String, Clan> clans; // clan name -> clan object
    private final Map<UUID, String> playerClans; // player UUID -> clan name
    
    public ClanManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.clans = new HashMap<>();
        this.playerClans = new HashMap<>();
    }
    
    /**
     * Create a new clan
     */
    public void createClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
        playerClans.put(clan.getOwner(), clan.getName().toLowerCase());
        
        // Save data immediately
        plugin.getDataManager().saveData();
    }
    
    /**
     * Delete a clan
     */
    public void deleteClan(String clanName) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            // Remove all members from playerClans map
            for (UUID member : clan.getMembers()) {
                playerClans.remove(member);
            }
            
            // Remove clan from clans map
            clans.remove(clanName.toLowerCase());
            
            // Save data immediately
            plugin.getDataManager().saveData();
        }
    }
    
    /**
     * Get clan by name
     */
    public Clan getClan(String clanName) {
        return clans.get(clanName.toLowerCase());
    }
    
    /**
     * Get clan that a player belongs to
     */
    public Clan getPlayerClan(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName != null) {
            return getClan(clanName);
        }
        return null;
    }
    
    /**
     * Check if a clan exists
     */
    public boolean clanExists(String clanName) {
        return clans.containsKey(clanName.toLowerCase());
    }
    
    /**
     * Get all clans
     */
    public List<Clan> getAllClans() {
        return new ArrayList<>(clans.values());
    }
    
    /**
     * Get all clan names
     */
    public Set<String> getAllClanNames() {
        return new HashSet<>(clans.keySet());
    }
    
    /**
     * Add a member to a clan (used when loading data)
     */
    public void addMemberToClan(UUID playerUUID, String clanName) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            clan.addMember(playerUUID);
            playerClans.put(playerUUID, clanName.toLowerCase());
        }
    }
    
    /**
     * Remove a member from a clan
     */
    public void removeMemberFromClan(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName != null) {
            Clan clan = getClan(clanName);
            if (clan != null) {
                clan.removeMember(playerUUID);
                playerClans.remove(playerUUID);
                
                // Save data immediately
                plugin.getDataManager().saveData();
            }
        }
    }
    
    /**
     * Load clan data (called by DataManager)
     */
    public void loadClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
        
        // Update playerClans map
        for (UUID member : clan.getMembers()) {
            playerClans.put(member, clan.getName().toLowerCase());
        }
    }
    
    /**
     * Clear all data (used when reloading)
     */
    public void clearData() {
        clans.clear();
        playerClans.clear();
    }
    
    /**
     * Get number of clans
     */
    public int getClanCount() {
        return clans.size();
    }
    
    /**
     * Get clans sorted by member count
     */
    public List<Clan> getClansByMemberCount() {
        List<Clan> sortedClans = new ArrayList<>(clans.values());
        sortedClans.sort((c1, c2) -> Integer.compare(c2.getMemberCount(), c1.getMemberCount()));
        return sortedClans;
    }
    
    /**
     * Get clan statistics
     */
    public Map<String, Integer> getClanStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_clans", clans.size());
        stats.put("total_members", playerClans.size());
        
        int totalMembers = 0;
        for (Clan clan : clans.values()) {
            totalMembers += clan.getMemberCount();
        }
        stats.put("average_members_per_clan", clans.isEmpty() ? 0 : totalMembers / clans.size());
        
        return stats;
    }
}