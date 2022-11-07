package me.starchaser.dangerrun;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class scoreboard_mgr {
    scoreboard_mgr(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()){
                        ScoreboardManager manager = Bukkit.getScoreboardManager();
                        Scoreboard board = manager.getNewScoreboard();
                        Objective objective = board.registerNewObjective("dr_board", "dummy");
                        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                        objective.setDisplayName("§e§lDANGER RUN");
                        if (starchaser.getDangerRunArena(p) != null) {
                            DangerRunArena dangerRunArena = starchaser.getDangerRunArena(p);
                            if (dangerRunArena.getGameState() == DangerRunArena.GameState.WAITING || dangerRunArena.getGameState() == DangerRunArena.GameState.COUNTING) {
                                objective.getScore("§r§a§r").setScore(11);
                                objective.getScore("Map: §aRainbows").setScore(10);
                                objective.getScore("Players: §a" + dangerRunArena.getPlayers().size() + "/" + dangerRunArena.max_players).setScore(8);
                                objective.getScore("§r§r").setScore(9);
                                if (dangerRunArena.getGameState() == DangerRunArena.GameState.COUNTING) {
                                    objective.getScore("Status: §aStarting in " + dangerRunArena.getCountDownTimer()).setScore(8);
                                }else {
                                    objective.getScore("Status: §aWaiting...").setScore(8);
                                }
                                objective.getScore("§r§r§r§r").setScore(7);
                                objective.getScore("Mode: §6Solo").setScore(6);
                                objective.getScore("Version: §7BETA b0.2").setScore(5);
                                objective.getScore("§r").setScore(4);
                                objective.getScore("RoomID: §d" + dangerRunArena.getMatchID()).setScore(3);
                                objective.getScore("§r§r§r").setScore(2);
                                objective.getScore("§b✎ §emc.starchaser.me").setScore(1);
                            }
                            if (dangerRunArena.getGameState() == DangerRunArena.GameState.RUNNING) {
                                objective.getScore("§r§a§r").setScore(11);
                                objective.getScore("Map: §aRainbows").setScore(10);
                                objective.getScore("Players Alive: §a" + dangerRunArena.getPlayersAlive().size() + "/" + dangerRunArena.getPlayers().size()).setScore(9);
                                objective.getScore("§r§r").setScore(8);
                                objective.getScore("Stages: §c" + dangerRunArena.getStage_count() + "✮").setScore(7);
                                objective.getScore("§r§r§r§r").setScore(6);
                                objective.getScore("Time left:§a " + dangerRunArena.getCountdown_run()).setScore(5);
                                objective.getScore("§r").setScore(4);
                                objective.getScore("RoomID: §d" + dangerRunArena.getMatchID()).setScore(3);
                                objective.getScore("§r§r§r").setScore(2);
                                objective.getScore("§b✎ §emc.starchaser.me").setScore(1);
                            }
                        }else {
                            objective.getScore("§r").setScore(10);
                            objective.getScore("Player: §a" + p.getName()).setScore(9);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    YamlReader reader = new YamlReader(core.path+".mc-deluxe/dangerrun.yml");
                                    objective.getScore("Solo Wins: §a"  + reader.getInt("players." + p.getName() + ".solo_win")).setScore(8);
                                }
                            }.runTaskAsynchronously(core.getDangerRun);
                            objective.getScore("Team Wins: §a0").setScore(7);
                            objective.getScore("§r§r").setScore(6);
                            objective.getScore("§7Auto join:").setScore(5);
                            objective.getScore("§b/dr join").setScore(4);
                            objective.getScore("§7Join with room id:").setScore(3);
                            objective.getScore("§b/dr join <id>").setScore(2);
                            objective.getScore("§r§r§r").setScore(1);
                            objective.getScore("§b✎ §emc.starchaser.me").setScore(1);
                            objective.getScore("§b✎ §emc.starchaser.me").setScore(0);
                        }
                        p.setScoreboard(board);
                }
            }
        }.runTaskTimer(plugin , 5 , 5);
    }
}
