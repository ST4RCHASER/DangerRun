package me.starchaser.dangerrun;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.starchaser.nginxmc.bukkit.NginxPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static me.starchaser.dangerrun.starchaser.SearchMatch;
import static me.starchaser.dangerrun.starchaser.getDangerRunArena;

public class evt implements Listener {
    @EventHandler
    public void onDisconnect(PlayerQuitEvent evt){
        if (starchaser.getDangerRunArena(evt.getPlayer()) != null) {
            starchaser.getDangerRunArena(evt.getPlayer()).LeaveArena(evt.getPlayer());
        }
    }
    @EventHandler
    public void PlayerInteract(PlayerInteractEvent evt) {
            Player p = evt.getPlayer();
            if (starchaser.getDangerRunArena(p) != null) {
                if (p.getInventory().getItemInHand() != null && p.getInventory().getItemInHand().getType().equals(Material.SLIME_BALL)) {
                    starchaser.getDangerRunArena(p).LeaveArena(p);
                    return;
                }
//                new BukkitRunnable() {
//                    @Override
//                    public void run() {
//                        for (Location loc : starchaser.getNearbyLocation(p.getLocation(), 5)) {
//                            ArrayList<DangerRunArena.BlockStageRemember> arrs = new ArrayList<>(getDangerRunArena(p).Arena_Block_Packet);
//                            for (DangerRunArena.BlockStageRemember blockStageRemember : arrs) {
//                                if (loc.getY() == blockStageRemember.getLocation().getY() && loc.getX() == blockStageRemember.getLocation().getX() && loc.getZ() == blockStageRemember.getLocation().getZ()) {
//                                    p.sendBlockChange(blockStageRemember.getLocation(), blockStageRemember.getMaterial(), (byte) 0);
//                                }
//                            }
//                        }
//                    }
//                }.runTaskAsynchronously(core.getDangerRun);
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        event.setCancelled(event.getPlayer().getGameMode() != GameMode.CREATIVE);
    }
    @EventHandler
    public void PlayerJoin (PlayerJoinEvent e) {
        e.setJoinMessage(null);
        e.getPlayer().teleport(core.main_loc);
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getPlayer().getInventory().clear();
        e.getPlayer().setMaxHealth(2);
        e.getPlayer().setHealth(2);
    }
    @EventHandler
    public void PlayerDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getWorld().equals(core.game_world)) {
            if (starchaser.getDangerRunArena(p) != null) {
                DangerRunArena dangerRunArena = starchaser.getDangerRunArena(p);
                if (dangerRunArena.getGameState() == DangerRunArena.GameState.WAITING || dangerRunArena.getGameState() == DangerRunArena.GameState.COUNTING) {
                    if (p.getLocation().getY() < 5) {
                        p.teleport(core.game_loc);
                    }
                }else if (dangerRunArena.getGameState() == DangerRunArena.GameState.RUNNING) {
                    if (dangerRunArena.first_runner == null) {
                        WorldGuardPlugin wg = (WorldGuardPlugin) core.wgd;
                        for(ProtectedRegion r : WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld())).getApplicableRegions(BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))) {
                                String swith_side;
                                if (dangerRunArena.current_side.equalsIgnoreCase("A")) {
                                    swith_side = "B";
                                }else {
                                    swith_side = "A";
                                }
                                if (r.getId().equalsIgnoreCase("runner_" + dangerRunArena.current_side) && dangerRunArena.getStage_count() > -1) {
                                    dangerRunArena.BCMessage("§7DangerRun: §aผู้เล่น §f" + p.getName() + "§a ถึงอีกฝั่งเป็นคนแรก!");
                                    dangerRunArena.BCSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                                    dangerRunArena.getDBXP(p).put_new("Fastest Player" , 50);
                                    dangerRunArena.first_runner = p;
                                }
                        }
                    }
                    if (p.getLocation().getY() < 6) {
                        if (dangerRunArena.getPlayersAlive().contains(p)) {
                            p.teleport(new Location(core.game_world ,-28 ,30 ,40 , -90 ,0));
                            dangerRunArena.BCMessage("§7OUT: §c" + p.getName());
                            p.setGameMode(GameMode.ADVENTURE);
                            p.getInventory().clear();
                            p.updateInventory();
                            getDangerRunArena(p).getDBXP(p).TaskGive();
                            dangerRunArena.giveExitItem(p);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    p.sendMessage("§7DangerRun: §eเสียใจด้วยคุณตกรอบซะแล้ว หลังจากนี้คุณสามารถออกจากห้องนี้และหาต้องใหม่ได้โดยทันที โดยการพิมพ์ §f/dr leave §eขอให้โชคดีในรอบถัดไปนะ :)");
                                }
                            }.runTaskLaterAsynchronously(core.getDangerRun , 60L);
                        }else {
                            p.teleport(new Location(core.game_world ,-28 ,30 ,40 , -90 ,0));
                        }
                    }
                }
            }
        }else if (p.getWorld().equals(core.main_world)) {
            if (p.getLocation().getBlock() != null && p.getLocation().getBlock().getType().equals(Material.END_PORTAL)) {
                if (starchaser.getDangerRunArena(p) != null) {
                    p.sendMessage("§7DangerRun: §cYou already in match!");
                }else {
                    p.sendMessage("§7DangerRun: §aSearching for match...");
                    SearchMatch( true , true, p);
                }
            }
            if (p.getLocation().getY() < 15) {
                p.teleport(core.main_loc);
            }
        }
    }
    @EventHandler
    public void PlayerChat(PlayerChatEvent e){
        e.setCancelled(true);
            NginxPlayer nginxPlayer = core.nginxAPI.getNginxPlayer(e.getPlayer());
            if (starchaser.getDangerRunArena(e.getPlayer()) != null) {
                Bukkit.getConsoleSender().sendMessage("§b§l" + starchaser.getDangerRunArena(e.getPlayer()).getMatchID() + " §8- §f"+nginxPlayer.getLevel().getStr()+" §8- §r"+nginxPlayer.getTitle().getStr()+nginxPlayer.getPlayerClass().getStr()+"§7"+nginxPlayer.getName()+":§b "+e.getMessage()+"");
                for (Player abc : starchaser.getDangerRunArena(e.getPlayer()).getPlayers()) {
                    abc.sendMessage("§8। §f"+nginxPlayer.getLevel().getStr()+" §8। §r"+nginxPlayer.getTitle().getStr()+nginxPlayer.getPlayerClass().getStr()+"§7"+nginxPlayer.getName()+":§b "+e.getMessage()+"");
                }
            }else {
                Bukkit.getConsoleSender().sendMessage("§b§lLOBBY §8। §f"+nginxPlayer.getLevel().getStr()+" §8। §r"+nginxPlayer.getTitle().getStr()+nginxPlayer.getPlayerClass().getStr()+"§7"+nginxPlayer.getName()+":§b "+e.getMessage()+"");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(core.main_world)) {
                        p.sendMessage("§8। §f"+nginxPlayer.getLevel().getStr()+" §8। §r"+nginxPlayer.getTitle().getStr()+nginxPlayer.getPlayerClass().getStr()+"§7"+nginxPlayer.getName()+":§b "+e.getMessage()+"");
                    }
                }
            }

    }
}
