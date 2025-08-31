package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;

import java.util.*;

public class ClanManager {

    private final IClanPlugin plugin;
    private final Map<String, Clan> clans;
    private final Map<UUID, String> playerClans;
    private final Map<String, Set<UUID>> bannedPlayers;

    public ClanManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.clans = new HashMap<>();
        this.playerClans = new HashMap<>();
        this.bannedPlayers = new HashMap<>();
    }

    // --- Clan CRUD ---
    public void createClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
        playerClans.put(clan.getOwner(), clan.getName().toLowerCase());
        plugin.getDataManager().saveData();
    }

    public void deleteClan(String clanName) {
        Clan clan = getClan(clanName);
        if (clan != null) {
            for (UUID member : clan.getMembers()) {
                playerClans.remove(member);
            }
            clans.remove(clanName.toLowerCase());
            bannedPlayers.remove(clanName.toLowerCase());
            plugin.getDataManager().saveData();
        }
    }

    public Clan getClan(String clanName) {
        return clans.get(clanName.toLowerCase());
    }

    public Clan getPlayerClan(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        return clanName != null ? getClan(clanName) : null;
    }

    public boolean clanExists(String clanName) {
        return clans.containsKey(clanName.toLowerCase());
    }

    public List<Clan> getAllClans() {
        return new ArrayList<>(clans.values());
    }

    public Set<String> getAllClanNames() {
        return new HashSet<>(clans.keySet());
    }

    public void addMemberToClan(UUID playerUUID, String clanName) {
        Clan clan = getClan(clanName);
        if (clan != null && !isBanned(playerUUID, clanName)) {
            clan.addMember(playerUUID);
            playerClans.put(playerUUID, clanName.toLowerCase());
            plugin.getDataManager().saveData();
        }
    }

    public void removeMemberFromClan(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName != null) {
            Clan clan = getClan(clanName);
            if (clan != null) {
                clan.removeMember(playerUUID);
                playerClans.remove(playerUUID);
                plugin.getDataManager().saveData();
            }
        }
    }

    public boolean isBanned(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName == null) return false;
        return isBanned(playerUUID, clanName);
    }

    public boolean isBanned(UUID playerUUID, String clanName) {
        Set<UUID> banned = bannedPlayers.get(clanName.toLowerCase());
        return banned != null && banned.contains(playerUUID);
    }

    public void banPlayer(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName != null) {
            bannedPlayers.computeIfAbsent(clanName.toLowerCase(), k -> new HashSet<>())
                    .add(playerUUID);
            removeMemberFromClan(playerUUID);
            plugin.getDataManager().saveData();
        }
    }

    public void unbanPlayer(UUID playerUUID, String clanName) {
        Set<UUID> banned = bannedPlayers.get(clanName.toLowerCase());
        if (banned != null) {
            banned.remove(playerUUID);
            plugin.getDataManager().saveData();
        }
    }

    public void loadClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
        for (UUID member : clan.getMembers()) {
            playerClans.put(member, clan.getName().toLowerCase());
        }
    }

    public void loadBanned(String clanName, Set<UUID> banned) {
        bannedPlayers.put(clanName.toLowerCase(), banned);
    }

    public void clearData() {
        clans.clear();
        playerClans.clear();
        bannedPlayers.clear();
    }

    public int getClanCount() {
        return clans.size();
    }

    public List<Clan> getClansByMemberCount() {
        List<Clan> sortedClans = new ArrayList<>(clans.values());
        sortedClans.sort((c1, c2) -> Integer.compare(c2.getMemberCount(), c1.getMemberCount()));
        return sortedClans;
    }

    public Map<String, Integer> getClanStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_clans", clans.size());
        stats.put("total_members", playerClans.size());

        int totalMembers = clans.values().stream().mapToInt(Clan::getMemberCount).sum();
        stats.put("average_members_per_clan", clans.isEmpty() ? 0 : totalMembers / clans.size());

        return stats;
    }
}
