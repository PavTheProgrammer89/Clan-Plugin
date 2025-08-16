package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;

import java.util.*;

public class ClanManager {

    private final IClanPlugin plugin;
    private final Map<String, Clan> clans;
    private final Map<UUID, String> playerClans;

    public ClanManager(IClanPlugin plugin) {
        this.plugin = plugin;
        this.clans = new HashMap<>();
        this.playerClans = new HashMap<>();
    }

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

            plugin.getDataManager().saveData();
        }
    }

    public Clan getClan(String clanName) {
        return clans.get(clanName.toLowerCase());
    }

    public Clan getPlayerClan(UUID playerUUID) {
        String clanName = playerClans.get(playerUUID);
        if (clanName != null) {
            return getClan(clanName);
        }
        return null;
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
        if (clan != null) {
            clan.addMember(playerUUID);
            playerClans.put(playerUUID, clanName.toLowerCase());
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

    public void loadClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);

        for (UUID member : clan.getMembers()) {
            playerClans.put(member, clan.getName().toLowerCase());
        }
    }

    public void clearData() {
        clans.clear();
        playerClans.clear();
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

        int totalMembers = 0;
        for (Clan clan : clans.values()) {
            totalMembers += clan.getMemberCount();
        }
        stats.put("average_members_per_clan", clans.isEmpty() ? 0 : totalMembers / clans.size());

        return stats;
    }
}