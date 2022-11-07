package me.starchaser.dangerrun;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static me.starchaser.dangerrun.core.getDangerRun;

public class DangerRunArena {
    enum GameState {
        RUNNING,
        END,
        WAITING,
        COUNTING
    }

    private GameState gameState = GameState.WAITING;
    private int CountDownTimer = 60;
    private ArrayList<Player> players = new ArrayList<>();
    private int MatchID = 0;
    private int min_players = 3;
    public int max_players = 32;
    private int stage_count = 0;
    private int cooldown_wait = 5;
    private int countdown_run = 15;
    private boolean next_stage = false;
    public String current_side = "A";
    public Player first_runner = null;
    Location p1 = new Location(Bukkit.getWorld("game_world"), -30, 17.00, 61.00);
    Location p2 = new Location(Bukkit.getWorld("game_world"), 8, 17.00, 20.00);
    Selection sel = new Selection(p1, p2);
    ArrayList<BlockStageRemember> Arena_Block_Packet = new ArrayList<>();
    private ArrayList<Material[]> stage = new ArrayList<>();
    private ArrayList<PlayerDataBaseXP> players_xp = new ArrayList<>();

    public DangerRunArena() {
        MakeStage();
        Random rand = new Random();
        MatchID = rand.nextInt(100000);
        //ReloadPlayerPacket(true);
        for (Player ppp : players) {
            ppp.updateInventory();
        }
        starchaser.setBlocks(sel, new Material[]{Material.AIR, Material.AIR} , players);
        //PacketBlockMaker(sel, new Material[]{Material.AIR, Material.AIR});
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState == GameState.WAITING) {
                    if (players.size() >= min_players) {
                        gameState = GameState.COUNTING;
                        CountDownTimer = 40;
                        BCMessage("§7DangerRun: §aMatch has start in " + CountDownTimer + " secs...");
                    }
                } else if (gameState == GameState.COUNTING) {
                    if (players.size() < min_players) {
                        BCMessage("§7DangerRun: §cCountdown has stoped! §7§o(Not enough players)");
                        gameState = GameState.WAITING;
                    } else {
                        CountDownTimer--;
                        if (CountDownTimer == 30) {
                            BCMessage("§7DangerRun: §aMatch has start in 30 secs...");
                            BCSound(Sound.CLICK);
                        }
                        if (CountDownTimer == 20) {
                            BCMessage("§7DangerRun: §aMatch has start in 20 secs...");
                            BCSound(Sound.CLICK);
                        }
                        if (CountDownTimer < 11 && CountDownTimer > 0) {
                            BCMessage("§7DangerRun: §aMatch has start in " + CountDownTimer + " secs...");
                            BCSound(Sound.CLICK);
                        }
                        if (CountDownTimer <= 0) {
                            BCMessage("§7DangerRun: §eStarting game, Pleasewait!");
                            for (Player a : players) {
                                a.getInventory().clear();
                                TeleportPlayer(a , core.game_loc);
                            }
                            setGameState(GameState.RUNNING);
                            starchaser.setBlocks(sel, new Material[]{Material.AIR , Material.AIR} , players);
                            cooldown_wait = 3;
                            stage_count = -1;
                            next_stage = true;
                            BCMessage("§7DangerRun: §d§lWarmup!!");
                        }
                    }
                } else if (gameState == GameState.RUNNING) {
                    for (Player ppp : getPlayersAlive()) {
                        getDBXP(ppp).put_new("Survival Time", 1);
                    }
                    if (cooldown_wait > 1) {
                        cooldown_wait--;
                        return;
                    }
                    if (countdown_run < 1) {
                        BCSound(Sound.ENDERDRAGON_HIT);
                        starchaser.setBlocks(sel, new Material[]{Material.AIR , Material.AIR} , players);
                        //PacketBlockMaker(sel, new Material[]{Material.AIR, Material.AIR});
                        for (Player pp : getPlayersAlive()) {
                            pp.getInventory().clear();
                            WorldGuardPlugin wg = (WorldGuardPlugin) core.wgd;
                            RegionContainer container = wg.getRegionContainer();
                            RegionQuery query = container.createQuery();
                            ApplicableRegionSet set = query.getApplicableRegions(pp.getLocation());
                            for (ProtectedRegion r : set) {
                                if (!r.getId().equalsIgnoreCase("runner_" + current_side) && !r.getId().equalsIgnoreCase("game_arena") && !r.getId().equalsIgnoreCase("run_arena")) {
                                    pp.teleport(new Location(core.game_world, -10, 10, 40));
                                }
                            }
                        }
                        cooldown_wait = 4;
                        next_stage = true;
                        if (stage_count < 1) {
                            countdown_run = 20;
                        } else {
                            if (stage_count > 10) {
                                countdown_run = 10;
                            } else if (stage_count > 5) {
                                countdown_run = 13;
                            } else {
                                countdown_run = 15;
                            }
                        }

                    } else {
                        if (next_stage) {
                            next_stage = false;
                            stage_count++;
                            countdown_run--;
                            if (stage_count >= stage.size()) {
                                BCSound(Sound.LEVEL_UP);
                                setGameState(GameState.END);
                                BCMessage("§2====================================================");
                                BCMessage("§r");
                                BCMessage("§6 Game Result");
                                BCMessage("§r");
                                String winner_players = "§b§l";
                                for (Player winner : getPlayersAlive()) {
                                    winner_players = winner.getName() + " ";
                                    YamlReader reader = new YamlReader(core.path + ".mc-deluxe/dangerrun.yml");
                                    reader.set("players." + winner.getName() + ".solo_win", reader.getInt("players." + winner.getName() + ".solo_win") + 1);
                                    getDBXP(winner).put_new("Win", 300);
                                }
                                BCMessage(winner_players + "§a is a winner!");
                                BCMessage("§r");
                                BCMessage("§2====================================================");
                                setGameState(GameState.END);
                                cooldown_wait = 6;
                                return;
                            }
                            if (getPlayersAlive().size() == 1) {
                                BCSound(Sound.LEVEL_UP);
                                setGameState(GameState.END);
                                BCMessage("§2====================================================");
                                BCMessage("§r");
                                BCMessage("§6 Game Result");
                                BCMessage("§r");
                                String winner_players = "§b§l" + getPlayersAlive().get(0).getName();
                                BCMessage(winner_players + "§a is a winner!");
                                getDBXP(getPlayersAlive().get(0)).put_new("Win", 300);
                                YamlReader reader = new YamlReader(core.path + ".mc-deluxe/dangerrun.yml");
                                reader.set("players." + getPlayersAlive().get(0).getName() + ".solo_win", reader.getInt("players." + getPlayersAlive().get(0).getName() + ".solo_win") + 1);
                                BCMessage("§r");
                                BCMessage("§2====================================================");
                                setGameState(GameState.END);
                                cooldown_wait = 6;
                                return;
                            }
                            if (getPlayersAlive().size() < 1) {
                                BCSound(Sound.LEVEL_UP);
                                setGameState(GameState.END);
                                BCMessage("§2====================================================");
                                BCMessage("§r");
                                BCMessage("§6 Game Result");
                                BCMessage("§r");
                                BCMessage("§c§lALL FAIL!");
                                BCMessage("§r");
                                BCMessage("§2====================================================");
                                setGameState(GameState.END);
                                cooldown_wait = 6;
                                return;
                            }
                            BCSound(Sound.NOTE_PLING);
//                            ItemStack is = new ItemStack(Material.STONE_BUTTON);
//                            ItemMeta im = is.getItemMeta();
//                            im.setDisplayName("§a§lRun!!");
//                            is.setItemMeta(im);
//                            for (Player pp : getPlayersAlive()) {
//                                for (int i = 0; i < 9; i++) {
//                                    pp.getInventory().setItem(i, is);
//                                }
//                            }
                            starchaser.setBlocks(sel, stage.get(stage_count), players);
                            //PacketBlockMaker(sel, stage.get(stage_count));
                            if (current_side.equalsIgnoreCase("a")) {
                                current_side = "B";
                            } else {
                                current_side = "A";
                            }
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    first_runner = null;
                                }
                            }.runTaskLaterAsynchronously(getDangerRun, 20L);
                        } else {
                            BCSound(Sound.NOTE_PLING);
                            countdown_run--;
                        }
                    }
                } else if (gameState == GameState.END) {
                    if (cooldown_wait > 0) {
                        cooldown_wait--;
                        return;
                    } else {
                        ArrayList<Player> cache_room_players = new ArrayList<>();
                        for (Player pp : players) {
                            cache_room_players.add(pp);
                        }
                        for (Player p : cache_room_players) {
                            LeaveArena(p);
                        }
                    }
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(getDangerRun, 20L, 20L);
    }

    public void PacketBlockMaker(Selection sel, Material[] materials) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Arena_Block_Packet.clear();
                Random rng = new Random();

                for (int a = sel.minX.intValue(); a <= sel.maxX; a++) {
                    for (int b = sel.minZ.intValue(); b <= sel.maxZ; b++) {
                        Arena_Block_Packet.add(new BlockStageRemember(new Location(Bukkit.getWorld("game_world"), a, sel.maxY, b), materials[rng.nextInt(materials.length)]));
                    }
                }
                ReloadPlayerPacket(false);
            }
        }.runTaskAsynchronously(getDangerRun);
    }

    public void ReloadPlayerPacket(boolean near) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (near) {
                    for (Player p : players) {
                        p.updateInventory();
                        for (Location loc : starchaser.getNearbyLocation(p.getLocation(), 5)) {
                            for (BlockStageRemember blockStageRemember : Arena_Block_Packet) {
                                if (loc.getY() == blockStageRemember.getLocation().getY() && loc.getX() == blockStageRemember.getLocation().getX() && loc.getZ() == blockStageRemember.getLocation().getZ()) {
                                    p.sendBlockChange(blockStageRemember.getLocation(), blockStageRemember.getMaterial(), (byte) 0);
                                }
                            }
                        }
                    }

                } else {
                    for (BlockStageRemember blockStageRemember : Arena_Block_Packet) {
                        ArrayList<Player> arr = new ArrayList(players);
                        for (Player p : arr) {
                            p.updateInventory();
                            p.sendBlockChange(blockStageRemember.getLocation(), blockStageRemember.getMaterial(), (byte) 0);
                        }
                    }
                }
            }
        }.runTaskAsynchronously(getDangerRun);
    }

    public class BlockStageRemember {
        final Location loc;
        final Material mat;

        BlockStageRemember(Location location, Material material) {
            this.loc = location;
            this.mat = material;
        }

        public Location getLocation() {
            return loc;
        }

        public Material getMaterial() {
            return mat;
        }
    }

    public int getStage_count() {
        return stage_count;
    }

    public ArrayList<Player> getPlayersAlive() {
        ArrayList alive = new ArrayList();
        for (Player target : getPlayers()) {
            WorldGuardPlugin wg = (WorldGuardPlugin) core.wgd;
            RegionContainer container = wg.getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(target.getLocation());
            for (ProtectedRegion r : set) {
                if (r.getId().equalsIgnoreCase("game_arena")) {
                    alive.add(target);
                }
            }
        }
        return alive;
    }

    public void BCMessage(String messages) {
        for (Player p : players) {
            p.sendMessage(messages);
        }
    }

    public void BCSound(Sound sound) {
        for (Player p : players) {
            p.playSound(p.getLocation(), sound, 1, 1);
        }
    }

    private void MakeStage() {
        //STAGE: 0
        Material[] mat = {Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.BARRIER};
        stage.add(mat);

        //STAGE: 1
        mat = new Material[]{Material.GRASS, Material.DIRT, Material.AIR};
        stage.add(mat);

        //STAGE: 3
        mat = new Material[]{Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 4
        mat = new Material[]{Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 5
        mat = new Material[]{Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.WOOD, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 6
        mat = new Material[]{Material.GRASS, Material.DIRT, Material.LEAVES, Material.WOOD, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 7
        mat = new Material[]{Material.ICE, Material.SNOW_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 8
        mat = new Material[]{Material.ICE, Material.SNOW_BLOCK, Material.PACKED_ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 9
        mat = new Material[]{Material.ICE, Material.PACKED_ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 10
        mat = new Material[]{Material.ICE, Material.PACKED_ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 11
        mat = new Material[]{Material.NETHERRACK, Material.NETHERRACK, Material.GLOWSTONE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 12
        mat = new Material[]{Material.SOUL_SAND, Material.NETHERRACK, Material.GLOWSTONE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 13
        mat = new Material[]{Material.SOUL_SAND, Material.SOUL_SAND, Material.GLOWSTONE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 14
        mat = new Material[]{Material.SLIME_BLOCK, Material.SOUL_SAND, Material.ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 15
        mat = new Material[]{Material.SLIME_BLOCK, Material.SOUL_SAND, Material.ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 16
        mat = new Material[]{Material.SLIME_BLOCK, Material.SOUL_SAND, Material.SLIME_BLOCK, Material.PACKED_ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 17
        mat = new Material[]{Material.REDSTONE_BLOCK, Material.PISTON_MOVING_PIECE, Material.WOOD_STAIRS, Material.PACKED_ICE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

        //STAGE: 17
        mat = new Material[]{Material.REDSTONE_BLOCK, Material.PISTON_MOVING_PIECE, Material.WOOD_STAIRS, Material.DARK_OAK_FENCE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR};
        stage.add(mat);

    }

    public int getCountdown_run() {
        return countdown_run;
    }

    public Player getPlayerInArena(String name) {
        for (Player pl : players) {
            if (pl.getName().equalsIgnoreCase(name)) {
                return pl;
            }
        }
        return null;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void RemovePlayer(String name) {
        Player p = getPlayerInArena(name);
        if (p != null && players.contains(p)) {
            players.remove(p);
        }
    }

    public void RemovePlayer(Player p) {
        RemovePlayer(p.getName());
    }

    public void AddPlayer(Player p) {
        if (getPlayerInArena(p.getName()) == null) {
            RemovePlayer(p);
            players.add(p);
        }
    }
    boolean cooldown_map = true;
    public void JoinArena(Player p) {
        if (gameState == GameState.END) {
        p.sendMessage("§7DangerRun: §cไม่สามารถเข้าร่วมห้องนี้ได้เนื่องจากห้องนี้ได้เล่นจนจบไปแล้ว!");
        return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setGameMode(GameMode.ADVENTURE);
            }
        }.runTask(getDangerRun);
        AddPlayer(p);
        AddnewDBXP(p);
        p.sendMessage("§7DangerRun: §aYou joined match id: " + getMatchID());
        if (gameState == GameState.COUNTING || gameState == GameState.WAITING) {
            TeleportPlayer(p , core.game_loc);
            for (Player pa : players) {
                pa.sendMessage("§7Join: §a" + p .getName());
                if (min_players - players.size() > 1 && gameState == GameState.WAITING) {
                    pa.sendMessage("§7DangerRun: §aต้องการอีก §f" + (min_players - players.size()) + " §aผู้เล่นเพื่อที่จะเริ่มเกม");
                }
            }
            if (cooldown_map) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        starchaser.setBlocks(sel , new Material[]{Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER, Material.BARRIER} , players);
                    }
                }.runTaskLaterAsynchronously(getDangerRun , 5L);
                cooldown_map = false;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        cooldown_map = true;
                    }
                }.runTaskLater(getDangerRun , 100);
            }
        }else {
            p.teleport(new Location(core.game_world ,-28 ,30 ,40 , -90 ,0));
        }
        //ReloadPlayerPacket(false);
        ClearINV(p);
        giveExitItem(p);
    }
    public void giveExitItem(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack exit_item = new ItemStack(Material.SLIME_BALL);
                ItemMeta meta = exit_item.getItemMeta();
                meta.setDisplayName("§cLeave to lobby");
                exit_item.setItemMeta(meta);
                p.getInventory().setItem(8 , exit_item);
            }
        }.runTaskLater(getDangerRun , 10L);
    }
    public void ClearINV(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.getInventory().clear();
            }
        }.runTask(getDangerRun);
    }
    public void LeaveArena(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setGameMode(GameMode.ADVENTURE);
            }
        }.runTask(getDangerRun);
        RemovePlayer(p);
        RemoveDBXP(p);
        TeleportPlayer(p , core.main_loc);
        if (gameState == GameState.COUNTING || gameState == GameState.WAITING) {
            for (Player pa : players) {
                pa.sendMessage("§7Leave: §c" + p .getName());
            }
        }

        ClearINV(p);
    }
    public void AddnewDBXP(Player p){
        if (getDBXP(p) == null) {
            RemoveDBXP(p);
            players_xp.add(new PlayerDataBaseXP(p));
        }
    }
    public void RemoveDBXP(Player p) {
        PlayerDataBaseXP pxp = getDBXP(p);
        if (pxp != null && players_xp.contains(p)) {
            players_xp.remove(pxp);
        }
    }
    public PlayerDataBaseXP getDBXP(Player p) {
        for (PlayerDataBaseXP pxp : players_xp) {
            if (pxp.getPlayer().getName().equalsIgnoreCase(p.getName())) {
                return pxp;
            }
        }
        return null;
    }
    public void TeleportPlayer(Player p, Location loc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.teleport(loc);
            }
        }.runTask(getDangerRun);
    }
    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        Bukkit.getConsoleSender().sendMessage("§bGame id:" + getMatchID() + " has state change to §e" + gameState.toString());
        this.gameState = gameState;
    }

    public int getCountDownTimer() {
        return CountDownTimer;
    }

    public void setCountDownTimer(int countDownTimer) {
        CountDownTimer = countDownTimer;
    }

    public int getMatchID() {
        return MatchID;
    }
    public class PlayerDataBaseXP {
        public HashMap<String, Integer> xp_table = new HashMap<>();
        final Player player;
        PlayerDataBaseXP(Player p){
            this.player = p;
        }

        public Player getPlayer() {
            return player;
        }
        public void clean() {
            xp_table.clear();
        }
        public void put_new(String data, int xp) {
            if (xp_table.containsKey(data)) {
                xp_table.replace(data , xp_table.get(data) + xp);
            }else {
                xp_table.put(data , xp);
            }
        }
    }
}
