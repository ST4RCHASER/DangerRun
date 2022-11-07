package me.starchaser.dangerrun;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.XMLFormatter;

import static me.starchaser.dangerrun.starchaser.SearchMatch;
import static me.starchaser.dangerrun.starchaser.getDangerRunArena;

public class core extends JavaPlugin implements Listener {
    public static Plugin getDangerRun;
    public static ArrayList<DangerRunArena> arenas = new ArrayList<>();
    public static World game_world,main_world;
    public static Location game_loc,main_loc;
    public static Plugin wgd;
    public static String path;
    @Override
    public void onEnable() {
        path = this.getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath() + File.separator;
        wgd = getServer().getPluginManager().getPlugin("WorldGuard");
        getDangerRun = this;
        arenas = new ArrayList<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                game_world = Bukkit.getWorld("game_world");
                main_world = Bukkit.getWorld("main_world");
                if (game_world == null) {
                    getLogger().severe("=========================================");
                    getLogger().severe("Error! game_world not found please check!");
                    getLogger().severe("=========================================");
                    Bukkit.shutdown();
                    return;
                }
                if (main_world == null) {
                    getLogger().severe("=========================================");
                    getLogger().severe("Error! main_world not found please check!");
                    getLogger().severe("=========================================");
                    Bukkit.shutdown();
                    return;
                }
                game_loc = new Location(game_world, -10,21,68,180,0);
                main_loc = new Location(main_world, 43 ,71 ,34 , 180 , 0);
            }
        }.runTask(this);
        Bukkit.getPluginManager().registerEvents(new evt(), this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (starchaser.getDangerRunArena(p) == null) {
                        for (Player onp : Bukkit.getOnlinePlayers()){
                            p.showPlayer(onp);
                        }
                    }else {
                        for (Player onp : Bukkit.getOnlinePlayers()){
                            if (starchaser.getDangerRunArena(onp) == null) {
                                p.hidePlayer(onp);
                            }else {
                                if (getDangerRunArena(onp).getMatchID() == getDangerRunArena(p).getMatchID()) {
                                    p.showPlayer(onp);
                                }else {
                                    p.hidePlayer(onp);
                                }
                            }
                        }
                    }
                    if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                        if (p.getAllowFlight()) {
                            p.setAllowFlight(false);
                        }else {
                            p.setAllowFlight(true);
                        }
                    }
                }
            }
        }.runTaskTimer(this , 1L,1L);
        new scoreboard_mgr(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dangerrun") || command.getName().equalsIgnoreCase("dr")){
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("join")){
                    if (starchaser.getDangerRunArena((Player) sender) != null) {
                        sender.sendMessage("§7DangerRun: §cYou already in match!");
                        return true;
                    }else {
                        sender.sendMessage("§7DangerRun: §aSearching for match...");
                        SearchMatch( true , true, (Player) sender);
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("leave")){
                    if (starchaser.getDangerRunArena((Player) sender) != null) {
                        sender.sendMessage("§7DangerRun: §cLeaving the match...");
                        starchaser.getDangerRunArena((Player) sender).LeaveArena((Player) sender);
                        return true;
                    }else {
                        sender.sendMessage("§7DangerRun: §cYou are not in any match");
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("room")) {
                    if (args.length < 2) {
                        sender.sendMessage("§7DangerRun: §cวิธีการเข้าห้องให้พิมพ์:§f /dr room <หมายเลขห้อง>");
                        return true;
                    }else {
                        if (args[1].matches("[0-9]+")) {
                            for (DangerRunArena arena : arenas) {
                                if (arena.getMatchID() == Integer.parseInt(args[1])) {
                                    arena.JoinArena((Player) sender);
                                    return true;
                                }
                            }
                        }
                        sender.sendMessage("§7DangerRun: §cไม่พบหมายเลขห้องนี้: §f" + args[1]);
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("rooms")){
                    sender.sendMessage("§6====================§f ห้องทั้งหมดขณะนี้ §6====================");
                    sender.sendMessage("§r");
                    for (DangerRunArena dangerRunArena : arenas) {
                        if (dangerRunArena.getGameState() != DangerRunArena.GameState.END) {
                            String game_state_str = "§cไม่ได้ระบุ";
                            if (dangerRunArena.getGameState() == DangerRunArena.GameState.WAITING) {
                                game_state_str = "§aกำลังรอ";
                            }
                            if (dangerRunArena.getGameState() == DangerRunArena.GameState.COUNTING) {
                                game_state_str = "§eกำลังจะเริ่ม";
                            }
                            if (dangerRunArena.getGameState() == DangerRunArena.GameState.RUNNING) {
                                game_state_str = "§cกำลังเล่น";
                            }
                            sender.sendMessage("§rหมายเลขห้อง: §d" + dangerRunArena.getMatchID() + " §rสถานะ: " + game_state_str + " §fผู้เล่น: §b" + dangerRunArena.getPlayersAlive().size() + "/" + dangerRunArena.getPlayers().size());
                        }
                    }
                    sender.sendMessage("§r");
                    sender.sendMessage("§eสามารถเข้าร่วมห้องได้โดยการพิมพ์ §f/dr room <หมายเลขห้อง> §eได้ทันที!");
                    sender.sendMessage("§r");
                    sender.sendMessage("§dหรือกระโดดลงไปเพื่อหาห้องแบบอัตโนมัติ");
                }
                if (args[0].equalsIgnoreCase("top")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (args[1].equalsIgnoreCase("solo")) {
                                String s_10 = " ",s_9 = " ",s_8 = " ",s_7 = " " ,s_6 = " ",s_5 = " ",s_4 = " ",s_3 = " ",s_2 = " ",s_1 = "NuMaySiamz";
                                YamlReader reader = new YamlReader(core.path+".mc-deluxe/dangerrun.yml");
                                ConfigurationSection selection =  reader.getConfigSelection("players");
                                for (String str : selection.getKeys(false)){
                                    if (reader.getInt("players." + str + ".solo_win") >= reader.getInt("players." + s_1 + ".solo_win")) {
                                        String c_2 = s_1, c_3 = s_2, c_4 = s_3, c_5 = s_4, c_6 = s_5, c_7 = s_6, c_8 = s_7, c_9 = s_8, c_10 = s_9;
                                        s_1 = str;
                                        s_2 = c_2;
                                        s_3 = c_3;
                                        s_4 = c_4;
                                        s_5 = c_5;
                                        s_6 = c_6;
                                        s_7 = c_7;
                                        s_8 = c_8;
                                        s_9 = c_9;
                                        s_10 = c_10;
                                    }
                                }
                                sender.sendMessage("1." + s_1 + " | " + reader.getInt("players." + s_1 + ".solo_win"));
                                sender.sendMessage("2." + s_2 + " | " + reader.getInt("players." + s_2 + ".solo_win"));
                                sender.sendMessage("3." + s_3 + " | " + reader.getInt("players." + s_3 + ".solo_win"));
                                sender.sendMessage("4." + s_4 + " | " + reader.getInt("players." + s_4 + ".solo_win"));
                                sender.sendMessage("5." + s_5 + " | " + reader.getInt("players." + s_5 + ".solo_win"));
                                sender.sendMessage("6." + s_6 + " | " + reader.getInt("players." + s_6 + ".solo_win"));
                                sender.sendMessage("7." + s_7 + " | " + reader.getInt("players." + s_7 + ".solo_win"));
                                sender.sendMessage("8." + s_8 + " | " + reader.getInt("players." + s_8 + ".solo_win"));
                                sender.sendMessage("9." +  s_9 + " | " + reader.getInt("players." + s_9 + ".solo_win"));
                                sender.sendMessage("10." + s_10 + " | " + reader.getInt("players." + s_10 + ".solo_win"));

                            }else if (args[1].equalsIgnoreCase("team")) {

                            }
                        }
                    }.runTaskAsynchronously(core.getDangerRun);
                }
                if (args[0].equalsIgnoreCase("runtest")) {
                    Location p1 = new Location(((Player) sender).getWorld(), -30, 17.00, 61.00);
                    Location p2 = new Location(((Player) sender).getWorld(), 8, 17.00, 20.00);

                    Selection sel = new Selection(p1, p2);
                    Material[] mat = {Material.GLASS, Material.AIR};

                    starchaser.setBlocks(sel, mat, new ArrayList<Player>(Arrays.asList(Bukkit.getPlayer(sender.getName()))) );

                    new BukkitRunnable() {
                        int counting = 11;
                        @Override
                        public void run() {
                            counting--;
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation() , Sound.CLICK , 1 , 1);
                            }
                        Bukkit.broadcastMessage("§7DangerRun: §aRun!!! §c" + counting);
                        if (counting < 1) {
                            Selection sel = new Selection(p1, p2);
                            Material[] mat = {Material.AIR};
                            starchaser.setBlocks(sel, mat, new ArrayList<Player>(Arrays.asList(Bukkit.getPlayer(sender.getName()))) );
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation() , Sound.ENDERMAN_HIT , 1 , 1);
                            }
                            this.cancel();
                        }
                        }
                    }.runTaskTimerAsynchronously(this,20L,20L);
                }
            }
            return true;
        }
        return false;
    }
}
