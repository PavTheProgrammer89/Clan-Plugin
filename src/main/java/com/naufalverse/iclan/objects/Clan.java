package com.naufalverse.iclan.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Clan {

    private String name;
    private UUID owner;
    private List<UUID> members;
    private long creationTime;
    private Set<UUID> bannedMembers;
    private Map<UUID, Boolean> bannedMembersAppeals;

    public Clan(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner);
        this.creationTime = System.currentTimeMillis();
        this.bannedMembers = new HashSet<>();
        this.bannedMembersAppeals = new HashMap<>();
    }

    // --- Basic getters ---
    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public long getCreationTime() {
        return creationTime;
    }

    // --- Banned members management ---
    public void addBannedMember(UUID uuid) {
        bannedMembers.add(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return bannedMembers.contains(uuid);
    }

    public void banMember(UUID member, boolean allowAppeal) {
        bannedMembers.add(member);
        bannedMembersAppeals.put(member, allowAppeal);
    }

    public void pardonMember(UUID member) {
        bannedMembers.remove(member);
        bannedMembersAppeals.remove(member);
    }

    public boolean canAppeal(UUID member) {
        return bannedMembersAppeals.getOrDefault(member, false);
    }

    // --- Clan management ---
    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setBannedMembers(Set<UUID> bannedMembers) {
        this.bannedMembers = bannedMembers != null ? bannedMembers : new HashSet<>();
    }

    public void setBannedMembersAppeals(Map<UUID, Boolean> bannedMembersAppeals) {
        this.bannedMembersAppeals = bannedMembersAppeals != null ? bannedMembersAppeals : new HashMap<>();
    }

    public void addMember(UUID member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public int getMemberCount() {
        return members.size();
    }

    @Override
    public String toString() {
        return "Clan{" +
                "name='" + name + '\'' +
                ", owner=" + owner +
                ", members=" + members.size() +
                ", creationTime=" + creationTime +
                ", bannedMembers=" + bannedMembers.size() +
                '}';
    }
}
