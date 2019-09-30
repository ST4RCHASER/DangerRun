package me.starchaser.dangerrun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class starchaser {
    public static int SearchMatch(boolean join_if_found , boolean create_new_if_not_found, Player p){
        for (DangerRunArena dangerRunArena : core.arenas) {
            if (dangerRunArena.getGameState() == DangerRunArena.GameState.WAITING || dangerRunArena.getGameState() == DangerRunArena.GameState.COUNTING) {
                if (join_if_found) {
                    dangerRunArena.JoinArena(p);
                }
                return dangerRunArena.getMatchID();
            }
        }
        if (create_new_if_not_found) {
            core.arenas.add(new DangerRunArena());
            SearchMatch(join_if_found , create_new_if_not_found , p);
        }
        return -1;
    }
    public static DangerRunArena getDangerRunArena(Player p) {
        for (DangerRunArena arena : core.arenas) {
            if (arena.getPlayers().contains(p)) return arena;
        }
        return null;
    }
    static public void setBlocks(Selection sel, Material[] materials, ArrayList<Player> players) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Random rng = new Random();
                    for (int b = sel.minZ.intValue(); b <= sel.maxZ; b++) {
                        for (int a = sel.minX.intValue(); a <= sel.maxX; a++) {

                            for (Player p : players) {
                            p.sendBlockChange(new Location(Bukkit.getWorld("game_world"), a , sel.maxY , b) , materials[rng.nextInt(materials.length)] ,(byte) 0);
                        }
                    }
                }
            }
        }.runTaskLaterAsynchronously(core.getDangerRun , 1L);
    }
    public static ArrayList<Location> getNearbyLocation(Location location, int radius) {
        Location loc = location;
        ArrayList<Location> locations = new ArrayList<>();
        World world = loc.getWorld();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    locations.add(new Location(world , loc.getBlockX()+x, loc.getBlockY()+y, loc.getBlockZ()+z));
                }
            }
        }
        return locations;
    }
}
