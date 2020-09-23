package me.PSK1103.VillagerNerfer.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Grindstone;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.util.Vector;

public class VillagerStorage {
    private Map<String, Villager> villagers;

    private VillagerNerfer plugin;

    private Map<String, Integer> restockCycle;

    private Map<String, Boolean> restockInitiated;

    private List<String> exemptVillagers;

    private int maxDailyRestocks;

    private int cyclesTillNextRestock;

    private long checkInterval;

    private boolean skipNametaggedVillagers;

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
            if(m.name().contains("_BED"))
                BEDS.add(m);
        }
    }

    public VillagerStorage(VillagerNerfer plugin) {
        this.plugin = plugin;
        this.villagers = new HashMap<>();
        this.restockCycle = new HashMap<>();
        this.restockInitiated = new HashMap<>();
        this.exemptVillagers = new ArrayList<>();
        this.checkInterval = plugin.getCustomConfig().getLong("check-interval",200L);
        this.maxDailyRestocks = plugin.getCustomConfig().getInt("max-daily-restocks",2);
        this.cyclesTillNextRestock = plugin.getCustomConfig().getInt("cycles-till-next-restock",1);
        this.skipNametaggedVillagers = plugin.getCustomConfig().getBoolean("skip-nametagged-villagers",true);
        Bukkit.getScheduler().runTaskTimer(plugin, new FreezeTask(), this.checkInterval, this.checkInterval);
    }

    public final class FreezeTask implements Runnable {
        public void run() {
            List<String> ids = new ArrayList<>();
            for (Villager v : VillagerStorage.this.villagers.values()) {
                if (!v.isValid() || v.isDead() || !v.getWorld().isChunkLoaded(v.getLocation().getBlockX() / 16, v.getLocation().getBlockZ() / 16)) {
                    ids.add(v.getUniqueId().toString());
                    continue;
                }


                if (VillagerStorage.canMove(v.getLocation()) && v.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    if(!v.isSleeping()) {
                        ids.add(v.getUniqueId().toString());
                        continue;
                    }
                }
                if(canMove(v.getLocation()) && v.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    v.setAI(true);
                    v.setAware(true);
                    continue;
                }
                if (VillagerStorage.this.exemptVillagers.contains(v.getUniqueId().toString()))
                    continue;

                if(v.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    if(!(v.getCustomName()!=null && v.getCustomName().length()!=0 && skipNametaggedVillagers))
                        ids.add(v.getUniqueId().toString());
                    continue;
                }

                if(v.getCustomName()!=null && v.getCustomName().length()!=0 && skipNametaggedVillagers)
                {
                    ids.add(v.getUniqueId().toString());
                    continue;
                }
                else {
                    v.setAI(false);
                    v.setAware(false);
                }


                if (v.getLocation().getBlock().isPassable()) {
                    v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY(), 0.0D));
                    if (VillagerStorage.SPECIAL_IMPASSABLES.contains(v.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getType())) {
                        v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() - 0.9375D, 0.0D));
                    } else if (VillagerStorage.TRAPDOORS.contains(v.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getType())) {
                        v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() - 0.875D, 0.0D));
                    } else if (VillagerStorage.SLABS.contains(v.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getType())) {
                        v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() - 0.5D, 0.0D));
                    } else if (v.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().isPassable()) {
                        v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() - 1.0D, 0.0D));
                    }
                } else if (VillagerStorage.SPECIAL_IMPASSABLES.contains(v.getLocation().getBlock().getType())) {
                    v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() + 0.0625D, 0.0D));
                } else if (VillagerStorage.TRAPDOORS.contains(v.getLocation().getBlock().getType())) {
                    v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() + 0.125D, 0.0D));
                } else if (VillagerStorage.SLABS.contains(v.getLocation().getBlock().getType())) {
                    v.teleport(v.getLocation().add(0.0D, v.getLocation().getBlockY() - v.getLocation().getY() + 0.5D, 0.0D));
                }

                if(Bukkit.getServer().getWorlds().get(0).getTime() >= 12000L && !v.isSleeping()) {
                    sleepInBed(v);
                    continue;
                }
                else {
                    if(Bukkit.getServer().getWorlds().get(0).getTime() <= 12000L && v.isSleeping()) {
                        try {
                            v.wakeup();
                        }
                        catch (IllegalStateException ignored) {}
                    }
                }

                /*if(isZombieNear(v.getLocation())) {
                    v.setAI(true);
                    v.setAware(true);
                }*/

                if (v.getProfession() == Villager.Profession.NONE) {
                    v.setMemory(MemoryKey.JOB_SITE, null);
                    if(Bukkit.getServer().getWorlds().get(0).getTime() >= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() <= 10000)
                        VillagerStorage.this.setProfession(v);
                    continue;
                }
                if (v.getProfession() != Villager.Profession.NITWIT) {
                    if (!VillagerStorage.this.checkForOwnJobBlock(v)) {
                        if (v.getVillagerExperience() == 0) {
                            v.setMemory(MemoryKey.JOB_SITE, null);
                            VillagerStorage.this.setProfession(v);
                        }
                        return;
                    }
                    List<MerchantRecipe> recipes = v.getRecipes();
                    if (Bukkit.getServer().getWorlds().get(0).getTime() <= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() > 2000L - VillagerStorage.this.checkInterval) {
                        if(v.isSleeping()) {
                            try {
                                v.wakeup();
                            }
                            catch (IllegalStateException ignored) {}
                        }

                        boolean[] resetNeeded = { false };
                        recipes.forEach(merchantRecipe -> {
                            if (merchantRecipe.getUses() > 0 && checkForOwnJobBlock(v)) {
                                merchantRecipe.setUses(0);
                                resetNeeded[0] = true;
                            }
                        });
                        if (resetNeeded[0] && checkForOwnJobBlock(v)) {
                            v.setRestocksToday(1);
                        } else {
                            v.setRestocksToday(0);
                        }
                        VillagerStorage.this.restockCycle.put(v.getUniqueId().toString(), 1);
                        continue;
                    }

                    if(Bukkit.getServer().getWorlds().get(0).getTime() >= 2000L && Bukkit.getServer().getWorlds().get(0).getTime() <= 10000) {

                        if (v.getRestocksToday() < VillagerStorage.this.maxDailyRestocks) {
                            if (v.isTrading()) {
                                VillagerStorage.this.restockCycle.put(v.getUniqueId().toString(), 1);
                                return;
                            }
                            if (VillagerStorage.this.restockCycle.get(v.getUniqueId().toString()) == VillagerStorage.this.cyclesTillNextRestock) {
                                boolean[] restockNeeded = {false};
                                recipes.forEach(merchantRecipe -> {
                                    if (merchantRecipe.getUses() > 0 && checkForOwnJobBlock(v)) {
                                        merchantRecipe.setUses(0);
                                        restockNeeded[0] = true;
                                    }
                                });
                                if (restockNeeded[0] && checkForOwnJobBlock(v))
                                    v.setRestocksToday(v.getRestocksToday() + 1);
                            }
                            VillagerStorage.this.cycleRestock(v.getUniqueId().toString());
                        }
                    }
                }
            }
            ids.forEach(id -> {
                Villager v = villagers.get(id);
                VillagerStorage.this.villagers.remove(id);
                if(v.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    spawnRegularVillager(v);
                }
                else {
                    spawnNerfedVillager(v);
                }
            });
        }
    }

    public void showTradeParticleEffects(String uid) {
        Villager v = this.villagers.get(uid);
        if (v == null)
            return;
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            v.setAI(true);
            v.setAware(true);
        },1L);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            v.setAI(false);
            v.setAware(false);
        },2L);
    }

    public void checkForLevelUpgrade(String uid) {
        Villager v = this.villagers.get(uid);
        if (v == null || this.restockInitiated.get(v.getUniqueId().toString()))
            return;
        if (v.getVillagerLevel() < setLevel(v.getVillagerExperience())) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                v.setAI(true);
                v.setAware(true);
                this.exemptVillagers.add(v.getUniqueId().toString());
                this.restockInitiated.put(v.getUniqueId().toString(), Boolean.TRUE);
            },1L);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                v.setAI(false);
                v.setAware(false);
                this.exemptVillagers.remove(v.getUniqueId().toString());
                this.restockInitiated.put(v.getUniqueId().toString(), Boolean.FALSE);
            },60L);
        }
    }

    public void disableVillagerAIAfterUpgrade(String uid) {
        if (!this.villagers.containsKey(uid))
            return;
        Villager v = this.villagers.get(uid);
        v.setAI(false);
        v.setAware(false);
    }

    private static boolean canMove(Location l) {
        World w = l.getWorld();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (w.getBlockAt(x, y, z).isPassable() || w.getBlockAt(x, y, z).isLiquid() || DOORS.contains(w.getBlockAt(x, y, z).getType()) || SPECIAL_IMPASSABLES.contains(w.getBlockAt(x, y, z).getType())) {
            if ((w.getBlockAt(x + 1, y, z).isPassable() && w.getBlockAt(x + 1, y + 1, z).isPassable()) || (w.getBlockAt(x - 1, y, z).isPassable() && w.getBlockAt(x - 1, y + 1, z).isPassable()) || (w.getBlockAt(x, y, z + 1).isPassable() && w.getBlockAt(x, y + 1, z + 1).isPassable()) || (w.getBlockAt(x, y, z - 1).isPassable() && w.getBlockAt(x, y + 1, z - 1).isPassable()))
                return true;
            if (w.getBlockAt(x, y + 2, z).isPassable()) {
                if (TALL_IMPASSABLES.contains(w.getBlockAt(x + 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x - 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z + 1).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z - 1).getType()))
                    return false;
                if ((w.getBlockAt(x + 1, y + 2, z).isPassable() && w.getBlockAt(x + 1, y + 1, z).isPassable()) || (w.getBlockAt(x - 1, y + 2, z).isPassable() && w.getBlockAt(x - 1, y + 1, z).isPassable()) || (w.getBlockAt(x, y + 2, z + 1).isPassable() && w.getBlockAt(x, y + 1, z + 1).isPassable()) || (w.getBlockAt(x, y + 2, z - 1).isPassable() && w.getBlockAt(x, y + 1, z - 1).isPassable()))
                    return true;
            }
            return false;
        }
        if (w.getBlockAt(x, y + 2, z).isPassable()) {
            if (TALL_IMPASSABLES.contains(w.getBlockAt(x + 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x - 1, y, z).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z + 1).getType()) && TALL_IMPASSABLES.contains(w.getBlockAt(x, y, z - 1).getType()) && !SLABS.contains(w.getBlockAt(x, y, z).getType()))
                return false;
            if ((w.getBlockAt(x + 1, y + 2, z).isPassable() && w.getBlockAt(x + 1, y + 1, z).isPassable()) || (w.getBlockAt(x - 1, y + 2, z).isPassable() && w.getBlockAt(x - 1, y + 1, z).isPassable()) || (w.getBlockAt(x, y + 2, z + 1).isPassable() && w.getBlockAt(x, y + 1, z + 1).isPassable()) || (w.getBlockAt(x, y + 2, z - 1).isPassable() && w.getBlockAt(x, y + 1, z - 1).isPassable()))
                return true;
            return false;
        }
        return false;
    }

    public void addVillager(@Nonnull Villager v) {
        if (this.villagers.containsKey(v.getUniqueId().toString()))
            return;

        Villager v2 = null;

        if(v.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM && !canMove(v.getLocation()))
            v2 = v;
        else if(v.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM && canMove(v.getLocation()))
            v2 = spawnRegularVillager(v);
        else if(v.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && canMove(v.getLocation()))
            v2 = v;
        else if(v.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && !canMove(v.getLocation())) {
            if(v.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG && skipNametaggedVillagers ) {
                v2 = v;
                System.out.println("Nametagged");
            }
            else
                v2 = spawnNerfedVillager(v);
        }

        if (v2 == null)
            return;
        this.villagers.put(v2.getUniqueId().toString(), v2);
        this.restockCycle.put(v2.getUniqueId().toString(), 1);
        this.restockInitiated.put(v2.getUniqueId().toString(), Boolean.FALSE);
    }

    public void removeVillager(@Nonnull Villager v) {
        this.villagers.remove(v.getUniqueId().toString());
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
        this.villagers.values().forEach(villager -> {
            villager.setAware(true);
            villager.setAI(true);
        });
        this.villagers.clear();
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

    /*private boolean isZombieNear(Location l) {
        boolean[] isZombieNear = {false};
        l.getNearbyEntities(1,1,1).forEach(entity -> {
            if(entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.ZOMBIE_VILLAGER || entity.getType() == EntityType.HUSK || entity.getType() == EntityType.DROWNED) {
                isZombieNear[0] = true;
            }
        });
        return isZombieNear[0];
    }*/

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
            case 0:
                p = (checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : checkZ(v))));
                break;
            case 1:
                p = (checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : checkZ(v))));
                break;
            case 2:
                p = (checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : checkZ(v))));
                break;
            case 3:
                p = (checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : checkZ(v))));
                break;
            case 4:
                p = (checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : checkZ(v))));
                break;
            case 5:
                p = (checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : checkZ(v))));
                break;
            case 6:
                p = (checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : checkZ(v))));
                break;
            case 7:
                p = (checkNorth(v) != Villager.Profession.NONE) ? checkNorth(v) : ((checkWest(v) != Villager.Profession.NONE) ? checkWest(v) : ((checkEast(v) != Villager.Profession.NONE) ? checkEast(v) : ((checkSouth(v) != Villager.Profession.NONE) ? checkSouth(v) : checkZ(v))));
                break;
        }
        v.setProfession(p);
    }

    private Villager.Profession checkEast(Villager v) {
        World w = v.getWorld();
        int x = v.getLocation().getBlockX();
        int y = v.getLocation().getBlockY();
        int z = v.getLocation().getBlockZ();
        if (isOccupied(w.getBlockAt(x + 1, y, z).getLocation())) {
            return Villager.Profession.NONE;
        }
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
        if (isOccupied(w.getBlockAt(x - 1, y, z).getLocation())) {
            return Villager.Profession.NONE;
        }
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
        if (isOccupied(w.getBlockAt(x, y, z + 1).getLocation())) {
            return Villager.Profession.NONE;
        }
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
        if (isOccupied(w.getBlockAt(x, y, z - 1).getLocation())) {
            return Villager.Profession.NONE;
        }
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
        if (isOccupied(w.getBlockAt(x, y - 1, z).getLocation())) {
            return Villager.Profession.NONE;
        }
        if (getProfession(w.getBlockAt(x, y - 1, z).getType()) != Villager.Profession.NONE) {
            v.setMemory(MemoryKey.JOB_SITE, w.getBlockAt(x, y - 1, z).getLocation());
            return getProfession(w.getBlockAt(x, y - 1, z).getType());
        }
        return Villager.Profession.NONE;
    }

    private boolean checkForOwnJobBlock(Villager v) {
        Material jobBlock = getProfessionBlock(v.getProfession());
        if(v.getMemory(MemoryKey.JOB_SITE) == null)
            return false;
        else {
            return  (jobBlock == v.getWorld().getBlockAt(v.getMemory(MemoryKey.JOB_SITE)).getType());
        }
    }

    private Material getProfessionBlock(Villager.Profession p) {
        switch (p.getKey().getKey()) {
            case "armorer":
                return Material.BLAST_FURNACE;
            case "butcher":
                return Material.SMOKER;
            case "cartographer":
                return Material.CARTOGRAPHY_TABLE;
            case "cleric":
                return Material.BREWING_STAND;
            case "farmer":
                return Material.COMPOSTER;
            case "fisherman":
                return Material.BARREL;
            case "fletcher":
                return Material.FLETCHING_TABLE;
            case "leatherworker":
                return Material.CAULDRON;
            case "librarian":
                return Material.LECTERN;
            case "mason":
                return Material.STONECUTTER;
            case "shepherd":
                return Material.LOOM;
            case "toolsmith":
                return Material.SMITHING_TABLE;
            case "weaponsmith":
                return Material.GRINDSTONE;
        }
        return null;
    }

    private Villager.Profession getProfession(Material m) {
        switch (m.getKey().getKey()) {
            case "blast_furnace":
                return Villager.Profession.ARMORER;
            case "smoker":
                return Villager.Profession.BUTCHER;
            case "cartography_table":
                return Villager.Profession.CARTOGRAPHER;
            case "brewing_stand":
                return Villager.Profession.CLERIC;
            case "composter":
                return Villager.Profession.FARMER;
            case "barrel":
                return Villager.Profession.FISHERMAN;
            case "fletching_table":
                return Villager.Profession.FLETCHER;
            case "cauldron":
                return Villager.Profession.LEATHERWORKER;
            case "lectern":
                return Villager.Profession.LIBRARIAN;
            case "stonecutter":
                return Villager.Profession.MASON;
            case "loom":
                return Villager.Profession.SHEPHERD;
            case "smithing_table":
                return Villager.Profession.TOOLSMITH;
            case "grindstone":
                return Villager.Profession.WEAPONSMITH;
        }
        return Villager.Profession.NONE;
    }

    private boolean isOccupied(Location l) {
        boolean[] res = { false };
        this.villagers.values().forEach(villager -> {
            if ((villager.getProfession() != Villager.Profession.NONE && villager.getProfession() != Villager.Profession.NITWIT) && villager.getMemory(MemoryKey.JOB_SITE) != null && villager.getMemory(MemoryKey.JOB_SITE).getBlockX() == l.getBlockX() && villager.getMemory(MemoryKey.JOB_SITE).getBlockY() == l.getBlockY() && villager.getMemory(MemoryKey.JOB_SITE).getBlockZ() == l.getBlockZ())
                res[0] = true;
        });
        return res[0];
    }

    private Villager spawnNerfedVillager(Villager v1) {

        if(v1.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return v1;

        if (!v1.isValid() || v1.isDead() || !v1.getWorld().isChunkLoaded(v1.getLocation().getBlockX() / 16, v1.getLocation().getBlockZ() / 16))
            return null;

        if(v1.getCustomName()!=null && v1.getCustomName().length()!=0)
            return v1;

        Villager v2 = (Villager)v1.getWorld().spawnEntity(v1.getLocation(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM);
        v2.setAI(false);
        v2.setAware(false);
        v2.setAge(v1.getAge());
        v2.setProfession(v1.getProfession());
        v2.setRestocksToday(v1.getRestocksToday());
        v2.setReputations(v1.getReputations());
        v2.setVillagerExperience(v1.getVillagerExperience());
        v2.setVillagerLevel(v1.getVillagerLevel());
        v2.setVillagerType(v1.getVillagerType());
        v2.setBreed(v1.canBreed());
        try {
            v2.setMemory(MemoryKey.JOB_SITE, v1.getMemory(MemoryKey.JOB_SITE));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ADMIRING_DISABLED, v1.getMemory(MemoryKey.ADMIRING_DISABLED));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ADMIRING_ITEM, v1.getMemory(MemoryKey.ADMIRING_ITEM));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ANGRY_AT, v1.getMemory(MemoryKey.ANGRY_AT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.GOLEM_DETECTED_RECENTLY, v1.getMemory(MemoryKey.GOLEM_DETECTED_RECENTLY));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.HOME, v1.getMemory(MemoryKey.HOME));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.HUNTED_RECENTLY, v1.getMemory(MemoryKey.HUNTED_RECENTLY));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_SLEPT, v1.getMemory(MemoryKey.LAST_SLEPT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_WOKEN, v1.getMemory(MemoryKey.LAST_WOKEN));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_WORKED_AT_POI, v1.getMemory(MemoryKey.LAST_WORKED_AT_POI));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.MEETING_POINT, v1.getMemory(MemoryKey.MEETING_POINT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.POTENTIAL_JOB_SITE, v1.getMemory(MemoryKey.POTENTIAL_JOB_SITE));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.UNIVERSAL_ANGER, v1.getMemory(MemoryKey.UNIVERSAL_ANGER));
        } catch (NullPointerException ignored) {}
        v2.setRecipes(v1.getRecipes());
        v1.remove();
        return v2;
    }

    private Villager spawnRegularVillager(Villager v1) {

        System.out.println("Spawning regular villager");

        if(v1.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            return v1;

        if (!v1.isValid() || v1.isDead() || !v1.getWorld().isChunkLoaded(v1.getLocation().getBlockX() / 16, v1.getLocation().getBlockZ() / 16))
            return null;

        Villager v2;

        if(v1.getCustomName()!=null && v1.getCustomName().length()!=0) {
            v2 = (Villager) v1.getWorld().spawnEntity(v1.getLocation(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
            v2.setCustomName(v1.getCustomName());
        }
        else
            v2 = (Villager)v1.getWorld().spawnEntity(v1.getLocation(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.NATURAL);

        v2.setAI(true);
        v2.setAware(true);
        v2.setAge(v1.getAge());
        v2.setProfession(v1.getProfession());
        v2.setRestocksToday(v1.getRestocksToday());
        v2.setReputations(v1.getReputations());
        v2.setVillagerExperience(v1.getVillagerExperience());
        v2.setVillagerLevel(v1.getVillagerLevel());
        v2.setVillagerType(v1.getVillagerType());
        v2.setBreed(v1.canBreed());

        try {
            v2.setMemory(MemoryKey.JOB_SITE, v1.getMemory(MemoryKey.JOB_SITE));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ADMIRING_DISABLED, v1.getMemory(MemoryKey.ADMIRING_DISABLED));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ADMIRING_ITEM, v1.getMemory(MemoryKey.ADMIRING_ITEM));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.ANGRY_AT, v1.getMemory(MemoryKey.ANGRY_AT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.GOLEM_DETECTED_RECENTLY, v1.getMemory(MemoryKey.GOLEM_DETECTED_RECENTLY));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.HOME, v1.getMemory(MemoryKey.HOME));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.HUNTED_RECENTLY, v1.getMemory(MemoryKey.HUNTED_RECENTLY));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_SLEPT, v1.getMemory(MemoryKey.LAST_SLEPT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_WOKEN, v1.getMemory(MemoryKey.LAST_WOKEN));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.LAST_WORKED_AT_POI, v1.getMemory(MemoryKey.LAST_WORKED_AT_POI));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.MEETING_POINT, v1.getMemory(MemoryKey.MEETING_POINT));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.POTENTIAL_JOB_SITE, v1.getMemory(MemoryKey.POTENTIAL_JOB_SITE));
        } catch (NullPointerException ignored) {}
        try {
            v2.setMemory(MemoryKey.UNIVERSAL_ANGER, v1.getMemory(MemoryKey.UNIVERSAL_ANGER));
        } catch (NullPointerException ignored) {}
        v2.setRecipes(v1.getRecipes());
        try {
            v1.wakeup();
        }
        catch (IllegalStateException ignored) {}
        v1.remove();
        return v2;
    }

    public void sleepInBed(Villager v) {
        Location l = v.getLocation();
        World w = v.getWorld();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();

        if(w.getBlockAt(x-1,y,z).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x - 1, y, z).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x-1,y,z).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x-1,y,z).getLocation());
            return;
        }
        if(w.getBlockAt(x-1,y,z-1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x - 1, y, z-1).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x-1,y,z-1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x-1,y,z-1).getLocation());
            return;
        }
        if(w.getBlockAt(x,y,z-1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x,y,z-1).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x,y,z-1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x,y,z-1).getLocation());
            return;
        }
        if(w.getBlockAt(x+1,y,z-1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x+1,y,z-1).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x+1,y,z-1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x+1,y,z-1).getLocation());
            return;
        }
        if(w.getBlockAt(x+1,y,z).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x + 1, y, z).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x+1,y,z).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x+1,y,z).getLocation());
            return;
        }
        if(w.getBlockAt(x+1,y,z+1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x+1,y,z+1).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x+1,y,z+1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x+1,y,z+1).getLocation());
            return;
        }
        if(w.getBlockAt(x,y,z+1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x,y,z+1).getState().getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x,y,z+1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x,y,z+1).getLocation());
            return;
        }
        if(w.getBlockAt(x-1,y,z+1).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x-1,y,z+1).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x-1,y,z+1).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x-1,y,z+1).getLocation());
            return;
        }
        if(w.getBlockAt(x,y-1,z).getBlockData() instanceof Bed && ((Bed) w.getBlockAt(x,y-1,z).getBlockData()).getPart() == Bed.Part.HEAD && !((Bed)w.getBlockAt(x,y-1,z).getBlockData()).isOccupied()) {
            v.sleep(w.getBlockAt(x,y-1,z).getLocation());
        }
    }
}
