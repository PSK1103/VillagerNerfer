package me.PSK1103.VillagerNerfer.utils;

import java.util.*;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import me.PSK1103.VillagerNerfer.depend.Inms;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VillagerStorage {
    private final Set<Villager> nerfedVillagers;

    private final Set<Villager> activeVillagers;

    private final VillagerNerfer plugin;

    private final Map<String, Integer> restockCycle;

    private final Map<String, Boolean> restockInitiated;

    private final List<String> exemptVillagers;

    private final Inms nmsDepend;

    private int maxDailyRestocks;
    private int cyclesTillNextRestock;
    private long activeCheckInterval;
    private long inactiveCheckInterval;
    private boolean skipNametaggedVillagers;
    private boolean showNerfedNametag;
    private List<String> nerfedNametags;

    private int dangerRadiusXZ;

    private int dangerRadiusY;

    private int checkingMethod;

    private static final EnumSet<Material> TALL_IMPASSABLES = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> SPECIAL_IMPASSABLES = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> TRAPDOORS = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> SLABS = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> DOORS = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> BEDS = EnumSet.noneOf(Material.class);

    private static final EnumSet<Material> JOB_BLOCKS = EnumSet.of(Material.BLAST_FURNACE, Material.SMOKER, Material.CARTOGRAPHY_TABLE, Material.BREWING_STAND, Material.COMPOSTER, Material.BARREL, Material.FLETCHING_TABLE, Material.CAULDRON, Material.LECTERN, Material.STONECUTTER, Material.LOOM,
            Material.SMITHING_TABLE, Material.GRINDSTONE);

    static {
        for (Material m : Material.values()) {
            if (m.name().contains("_CARPET"))
                SPECIAL_IMPASSABLES.add(m);
            if (m.name().contains("_WALL") || m.name().contains("_FENCE"))
                TALL_IMPASSABLES.add(m);
            if (m.name().contains("_TRAPDOOR"))
                TRAPDOORS.add(m);
            if (m.name().contains("_SLAB"))
                SLABS.add(m);
            if (m.name().contains("_DOOR"))
                DOORS.add(m);
            if (m.name().contains("_BED"))
                BEDS.add(m);
        }
    }

    public VillagerStorage(VillagerNerfer plugin) {
        this.plugin = plugin;
        this.activeVillagers = new HashSet<>();
        this.nerfedVillagers = new HashSet<>();
        this.restockCycle = new HashMap<>();
        this.restockInitiated = new HashMap<>();
        this.exemptVillagers = new ArrayList<>();
        this.activeCheckInterval = plugin.getCustomConfig().getActiveCheckInterval();
        this.inactiveCheckInterval = plugin.getCustomConfig().getInactiveCheckInterval();
        this.maxDailyRestocks = plugin.getCustomConfig().getMaxDailyRestocks();
        this.cyclesTillNextRestock = plugin.getCustomConfig().getCyclesTillNextInterval();
        this.skipNametaggedVillagers = plugin.getCustomConfig().skipNameTaggedVillagers();
        this.showNerfedNametag = plugin.getCustomConfig().showNerfedNametag();
        this.nerfedNametags = plugin.getCustomConfig().getNerfedNametags();
        this.dangerRadiusXZ = plugin.getCustomConfig().getDangerRadiusXZ();
        this.dangerRadiusY = plugin.getCustomConfig().getDangerRadiusY();
        this.checkingMethod = plugin.getCustomConfig().getCheckingMethod();
        this.nmsDepend = Inms.get(plugin);
        if(plugin.getCustomConfig().bstatsEnabled())
            addVillagerMetrics();
        Bukkit.getScheduler().runTaskTimer(plugin, new NerfedTask(), this.inactiveCheckInterval, this.inactiveCheckInterval);
        Bukkit.getScheduler().runTaskTimer(plugin, new ActiveTask(), this.activeCheckInterval, this.activeCheckInterval);
    }

    public final class NerfedTask implements Runnable {
        public void run() {
            nerfedVillagers.removeIf(v -> villagerCheck(v,true));
            for (Villager v : nerfedVillagers) {
                professionTask(v);
            }
        }
    }

    public final class ActiveTask implements Runnable {
        @Override
        public void run() {
            activeVillagers.removeIf(v -> villagerCheck(v, false));
        }
    }

    private void professionTask(Villager v) {
        if (v.getProfession() == Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, null);
            if (Bukkit.getServer().getWorlds().get(0).getTime() >= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() <= 10000L)
                VillagerStorage.this.setProfession(v);
            return;
        }
        if (v.getProfession() != Villager.Profession.NITWIT) {
            if (!VillagerStorage.this.checkForOwnJobBlock(v)) {
                if (v.getVillagerExperience() == 0) {
                    v.setMemory(MemoryKey.JOB_SITE, null);
                    VillagerStorage.this.setProfession(v);
                }
                return;
            }

            int currentLevel = v.getVillagerLevel();
            int newLevel = setLevel(v.getVillagerExperience());
            for (int i = 0;i<newLevel-currentLevel;i++)
                ((CraftVillager)v).getHandle().gq();

            List<MerchantRecipe> recipes = v.getRecipes();
            if (Bukkit.getServer().getWorlds().get(0).getTime() <= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() > 2000L - VillagerStorage.this.inactiveCheckInterval) {
                if (v.isSleeping())
                    try {
                        v.wakeup();
                    } catch (IllegalStateException ignored) {
                    }
                boolean[] resetNeeded = {false};
                recipes.forEach(merchantRecipe -> {
                    if (merchantRecipe.getUses() > 0 && VillagerStorage.this.checkForOwnJobBlock(v)) {
                        merchantRecipe.setUses(0);
                        resetNeeded[0] = true;
                    }
                });
                if (resetNeeded[0] && VillagerStorage.this.checkForOwnJobBlock(v)) {
                    v.setRestocksToday(1);
                } else {
                    v.setRestocksToday(0);
                }
                VillagerStorage.this.restockCycle.put(v.getUniqueId().toString(), 1);
                return;
            }
            if (Bukkit.getServer().getWorlds().get(0).getTime() >= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() <= 10000L)
                if (v.getRestocksToday() < VillagerStorage.this.maxDailyRestocks) {
                    if (v.isTrading()) {
                        VillagerStorage.this.restockCycle.put(v.getUniqueId().toString(), 1);
                        return;
                    }
                    if (VillagerStorage.this.restockCycle.get(v.getUniqueId().toString()) == VillagerStorage.this.cyclesTillNextRestock) {
                        boolean[] restockNeeded = {false};
                        recipes.forEach(merchantRecipe -> {
                            if (merchantRecipe.getUses() > 0 && VillagerStorage.this.checkForOwnJobBlock(v)) {
                                merchantRecipe.setUses(0);
                                restockNeeded[0] = true;
                            }
                        });
                        if (restockNeeded[0] && VillagerStorage.this.checkForOwnJobBlock(v))
                            v.setRestocksToday(v.getRestocksToday() + 1);
                    }
                    VillagerStorage.this.cycleRestock(v.getUniqueId().toString());
                }
        }
    }

    private boolean villagerCheck (Villager v, boolean nerfed) {
        if (!v.isValid() || v.isDead() || !v.getWorld().isChunkLoaded(v.getLocation().getBlockX() >> 4, v.getLocation().getBlockZ() >> 4)) {
            return true;
        }

        if(canMove(v.getLocation(), v.getCustomName()) || !v.getLocation().getNearbyLivingEntities(dangerRadiusXZ, dangerRadiusY, e -> List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED).contains(e.getType())).isEmpty()) {
            if(!nerfed)
                return false;
            v.setAware(true);
            nmsDepend.setActive(v);
            restockCycle.remove(v.getUniqueId().toString());
            restockInitiated.remove(v.getUniqueId().toString());
            activeVillagers.add(v);
            return true;
        }
        if(nerfed) {
            if(v.customName()!= null && !nerfedNametags.contains(v.getCustomName().toLowerCase(Locale.ROOT)) && skipNametaggedVillagers) {
                v.setAware(true);
                nmsDepend.setActive(v);
                restockCycle.remove(v.getUniqueId().toString());
                restockInitiated.remove(v.getUniqueId().toString());
                activeVillagers.add(v);
                return true;
            }
            return false;
        }
        if(v.customName()!= null && !nerfedNametags.contains(v.getCustomName().toLowerCase(Locale.ROOT)) && skipNametaggedVillagers) {
            return false;
        }
        v.setAware(false);
        if(!showNerfedNametag)
            v.customName(null);

        nmsDepend.setInactive(v);
        this.restockCycle.put(v.getUniqueId().toString(), 1);
        this.restockInitiated.put(v.getUniqueId().toString(), Boolean.FALSE);
        nerfedVillagers.add(v);
        return true;
    }

    private boolean canMove(Location l, String name) {
        if ((checkingMethod & 1) != 0 && in1x1(l))
                return false;
        if ((checkingMethod & 2) != 0 && standingOnForbiddenBlock(l))
                return false;
        if ((checkingMethod & 4) != 0)
            if ( name != null && nerfedNametags.contains(name.toLowerCase(Locale.ROOT)))
                return false;

        return true;
    }

    private boolean standingOnForbiddenBlock(Location l) {
        World w = l.getWorld();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();

        return w.getBlockAt(x,y-1,z).getType() == plugin.getCustomConfig().getBottomBlock();
    }

    private boolean in1x1(Location l) {
        World w = l.getWorld();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (w.getBlockAt(x, y, z).isPassable() || w.getBlockAt(x, y, z).isLiquid() || DOORS.contains(w.getBlockAt(x, y, z).getType()) || SPECIAL_IMPASSABLES.contains(w.getBlockAt(x, y, z).getType())) {
            if ((w.getBlockAt(x + 1, y, z).isPassable() && w.getBlockAt(x + 1, y + 1, z).isPassable()) || (w.getBlockAt(x - 1, y, z).isPassable() && w.getBlockAt(x - 1, y + 1, z).isPassable()) || (w.getBlockAt(x, y, z + 1).isPassable() && w.getBlockAt(x, y + 1, z + 1).isPassable()) || (w.getBlockAt(x, y, z - 1).isPassable() && w.getBlockAt(x, y + 1, z - 1).isPassable()))
                return false;
            if (w.getBlockAt(x, y + 2, z).isPassable()) {
                if (TALL_IMPASSABLES.contains(w.getBlockAt(x + 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x - 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z + 1).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z - 1).getType()))
                    return true;
                return (!w.getBlockAt(x + 1, y + 2, z).isPassable() || !w.getBlockAt(x + 1, y + 1, z).isPassable()) && (!w.getBlockAt(x - 1, y + 2, z).isPassable() || !w.getBlockAt(x - 1, y + 1, z).isPassable()) && (!w.getBlockAt(x, y + 2, z + 1).isPassable() || !w.getBlockAt(x, y + 1, z + 1).isPassable()) && (!w.getBlockAt(x, y + 2, z - 1).isPassable() || !w.getBlockAt(x, y + 1, z - 1).isPassable());
            }
            return true;
        }
        if (w.getBlockAt(x, y + 2, z).isPassable()) {
            if (TALL_IMPASSABLES.contains(w.getBlockAt(x + 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x - 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z + 1).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z - 1).getType()) && !SLABS.contains(w.getBlockAt(x, y, z).getType()))
                return false;
            return (!w.getBlockAt(x + 1, y + 2, z).isPassable() || !w.getBlockAt(x + 1, y + 1, z).isPassable()) && (!w.getBlockAt(x - 1, y + 2, z).isPassable() || !w.getBlockAt(x - 1, y + 1, z).isPassable()) && (!w.getBlockAt(x, y + 2, z + 1).isPassable() || !w.getBlockAt(x, y + 1, z + 1).isPassable()) && (!w.getBlockAt(x, y + 2, z - 1).isPassable() || !w.getBlockAt(x, y + 1, z - 1).isPassable());
        }
        return true;
    }

    public void addVillager(@NotNull Villager v) {
        this.activeVillagers.add(v);
    }

    public void removeVillager(@NotNull Villager v) {
        this.activeVillagers.remove(v);
        this.nerfedVillagers.remove(v);
        this.restockCycle.remove(v.getUniqueId().toString());
        this.restockInitiated.remove(v.getUniqueId().toString());
    }

    private void cycleRestock(String uid) {
        int i = this.restockCycle.get(uid);
        if (i <= this.cyclesTillNextRestock) {
            i++;
        } else {
            i = 1;
        }
        this.restockCycle.put(uid, i);
    }

    public void clearStorage() {
        this.activeVillagers.clear();
        this.nerfedVillagers.forEach(v -> {
            nmsDepend.setActive(v);
            v.setAware(true);
        });
        this.nerfedVillagers.clear();
        this.restockCycle.clear();
    }

    private int setLevel(int xp) {
        if (xp < 10)
            return 1;
        if (xp < 70)
            return 2;
        if (xp < 150)
            return 3;
        if (xp < 250)
            return 4;
        return 5;
    }

    private void setProfession(Villager v) {
        Villager.Profession p = Villager.Profession.NONE;
        Vector vX = new Vector(1, 0, 0);
        Vector vZ = new Vector(0, 0, 1);
        BitSet priorityOrder = new BitSet(3);
        Vector vV = v.getFacing().getDirection().multiply(-1);
        if (Math.abs(vV.dot(vX)) >= Math.abs(vV.dot(vZ))) {
            priorityOrder.clear(2);
        } else {
            priorityOrder.set(0);
        }
        if (vV.dot(vX) >= 0.0D) {
            priorityOrder.clear(0);
        } else {
            priorityOrder.set(0);
        }
        if (vV.dot(vZ) < 0.0D) {
            priorityOrder.clear(1);
        } else {
            priorityOrder.set(1);
        }
        int prNo = 0;
        for (int i = 0; i < priorityOrder.length(); i++)
            prNo += priorityOrder.get(i) ? (1 << i) : 0;
        switch (prNo) {
            case 0 ->
                    p = (checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : checkZ(v))));
            case 1 ->
                    p = (checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : checkZ(v))));
            case 2 ->
                    p = (checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : checkZ(v))));
            case 3 ->
                    p = (checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : checkZ(v))));
            case 4 ->
                    p = (checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : checkZ(v))));
            case 5 ->
                    p = (checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : checkZ(v))));
            case 6 ->
                    p = (checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : checkZ(v))));
            case 7 ->
                    p = (checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : checkZ(v))));
        }
        v.setProfession(p);
    }

    private Villager.Profession checkEast(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x + 1, y, z).getLocation()))
            return Villager.Profession.NONE;
        if (getProfession(w.getBlockAt(x + 1, y, z).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x + 1, y, z).getLocation());
            return getProfession(w.getBlockAt(x + 1, y, z).getType());
        }
        return Villager.Profession.NONE;
    }

    private Villager.Profession checkWest(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x - 1, y, z).getLocation()))
            return Villager.Profession.NONE;
        if (getProfession(w.getBlockAt(x - 1, y, z).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x - 1, y, z).getLocation());
            return getProfession(w.getBlockAt(x - 1, y, z).getType());
        }
        return Villager.Profession.NONE;
    }

    private Villager.Profession checkSouth(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x, y, z + 1).getLocation()))
            return Villager.Profession.NONE;
        if (getProfession(w.getBlockAt(x, y, z + 1).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x, y, z + 1).getLocation());
            return getProfession(w.getBlockAt(x, y, z + 1).getType());
        }
        return Villager.Profession.NONE;
    }

    private Villager.Profession checkNorth(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x, y, z - 1).getLocation()))
            return Villager.Profession.NONE;
        if (getProfession(w.getBlockAt(x, y, z - 1).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x, y, z - 1).getLocation());
            return getProfession(w.getBlockAt(x, y, z - 1).getType());
        }
        return Villager.Profession.NONE;
    }

    private Villager.Profession checkZ(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x, y - 1, z).getLocation()))
            return Villager.Profession.NONE;
        if (getProfession(w.getBlockAt(x, y - 1, z).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x, y - 1, z).getLocation());
            return getProfession(w.getBlockAt(x, y - 1, z).getType());
        }
        return Villager.Profession.NONE;
    }

    private boolean checkForOwnJobBlock(Villager v) {
        Material jobBlock = getProfessionBlock(v.getProfession());
        if (v.getMemory(MemoryKey.JOB_SITE) == null)
            return false;
        return (jobBlock == v.getWorld().getBlockAt(v.getMemory(MemoryKey.JOB_SITE)).getType());
    }

    private Material getProfessionBlock(Villager.Profession p) {
        return switch (p.getKey().getKey()) {
            case "armorer" -> Material.BLAST_FURNACE;
            case "butcher" -> Material.SMOKER;
            case "cartographer" -> Material.CARTOGRAPHY_TABLE;
            case "cleric" -> Material.BREWING_STAND;
            case "farmer" -> Material.COMPOSTER;
            case "fisherman" -> Material.BARREL;
            case "fletcher" -> Material.FLETCHING_TABLE;
            case "leatherworker" -> Material.CAULDRON;
            case "librarian" -> Material.LECTERN;
            case "mason" -> Material.STONECUTTER;
            case "shepherd" -> Material.LOOM;
            case "toolsmith" -> Material.SMITHING_TABLE;
            case "weaponsmith" -> Material.GRINDSTONE;
            default -> null;
        };
    }

    private Villager.Profession getProfession(Material m) {
        return switch (m.getKey().getKey()) {
            case "blast_furnace" -> Villager.Profession.ARMORER;
            case "smoker" -> Villager.Profession.BUTCHER;
            case "cartography_table" -> Villager.Profession.CARTOGRAPHER;
            case "brewing_stand" -> Villager.Profession.CLERIC;
            case "composter" -> Villager.Profession.FARMER;
            case "barrel" -> Villager.Profession.FISHERMAN;
            case "fletching_table" -> Villager.Profession.FLETCHER;
            case "cauldron" -> Villager.Profession.LEATHERWORKER;
            case "lectern" -> Villager.Profession.LIBRARIAN;
            case "stonecutter" -> Villager.Profession.MASON;
            case "loom" -> Villager.Profession.SHEPHERD;
            case "smithing_table" -> Villager.Profession.TOOLSMITH;
            case "grindstone" -> Villager.Profession.WEAPONSMITH;
            default -> Villager.Profession.NONE;
        };
    }

    private boolean isOccupied(Location l) {
        boolean[] res = { false };
        this.activeVillagers.forEach(villager -> {
            if (villager.getProfession() != Villager.Profession.NONE && villager.getProfession() != Villager.Profession.NITWIT && villager.getMemory(MemoryKey.JOB_SITE) != null && villager.getMemory(MemoryKey.JOB_SITE).getBlockX() == l.getBlockX() && villager.getMemory(MemoryKey.JOB_SITE).getBlockY() == l.getBlockY() && villager.getMemory(MemoryKey.JOB_SITE).getBlockZ() == l.getBlockZ())
                res[0] = true;
        });
        this.nerfedVillagers.forEach(villager -> {
            if (villager.getProfession() != Villager.Profession.NONE && villager.getProfession() != Villager.Profession.NITWIT && villager.getMemory(MemoryKey.JOB_SITE) != null && villager.getMemory(MemoryKey.JOB_SITE).getBlockX() == l.getBlockX() && villager.getMemory(MemoryKey.JOB_SITE).getBlockY() == l.getBlockY() && villager.getMemory(MemoryKey.JOB_SITE).getBlockZ() == l.getBlockZ())
                res[0] = true;
        });
        return res[0];
    }

    public void reloadCustomConfig() {
        Bukkit.getScheduler().cancelTasks(plugin);
        this.activeCheckInterval = plugin.getCustomConfig().getActiveCheckInterval();
        this.inactiveCheckInterval = plugin.getCustomConfig().getInactiveCheckInterval();
        this.maxDailyRestocks = plugin.getCustomConfig().getMaxDailyRestocks();
        this.cyclesTillNextRestock = plugin.getCustomConfig().getCyclesTillNextInterval();
        this.skipNametaggedVillagers = plugin.getCustomConfig().skipNameTaggedVillagers();
        this.showNerfedNametag = plugin.getCustomConfig().showNerfedNametag();
        this.nerfedNametags = plugin.getCustomConfig().getNerfedNametags();
        this.dangerRadiusXZ = plugin.getCustomConfig().getDangerRadiusXZ();
        this.dangerRadiusY = plugin.getCustomConfig().getDangerRadiusY();
        this.checkingMethod = plugin.getCustomConfig().getCheckingMethod();
        if(plugin.getCustomConfig().bstatsEnabled())
            addVillagerMetrics();
        Bukkit.getScheduler().runTaskTimer(plugin, new NerfedTask(), this.inactiveCheckInterval, this.inactiveCheckInterval);
        Bukkit.getScheduler().runTaskTimer(plugin, new ActiveTask(), this.activeCheckInterval, this.activeCheckInterval);
    }

    private void addVillagerMetrics() {
        plugin.getMetrics().addCustomChart(new Metrics.SingleLineChart("nerfed_villagers", nerfedVillagers::size));
    }
}
