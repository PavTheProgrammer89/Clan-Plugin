package com.naufalverse.iclan.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Clan {
    
    private String name;
    private UUID owner;
    private List<UUID> members;
    private long creationTime;
    
    public Clan(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner); // Owner is also a member
        this.creationTime = System.currentTimeMillis();
    }
    
    // Getters
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
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    // Member management
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
                '}';
    }
}