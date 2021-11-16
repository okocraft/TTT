package net.okocraft.ttt.module.anticlickbot;

import org.bukkit.Location;

public class KillLog {

    private int killCount;
    private Location location;
    private long timestamp;
    
    public KillLog(int killCount, Location location, long timestamp) {
        this.killCount = killCount;
        this.location = location;
        this.timestamp = timestamp;
    }

    public int killCount() {
        return killCount;
    }
    
    public void killCount(int killCount) {
        this.killCount = killCount;
    }

    public Location location() {
        return location;
    }
    
    public void location(Location location) {
        this.location = location;
    }

    public long timestamp() {
        return timestamp;
    }

    public void timestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
