package me.PSK1103.VillagerNerfer.utils;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;

public class VillagerStorage {

    private Map<String,Villager> villagers;
    private VillagerNerfer plugin;
    private Map<String,Integer> restockCycle;
    private Map<String,Boolean> restockInitiated;
    private List<String> exemptVillagers;

    private int maxDailyRestocks,cyclesTillNextRestock;
    private long checkInterval;
    private static final EnumSet<Material> TALL_IMPASSABLES = EnumSet.noneOf(Material.class);
    private static final EnumSet<Material> SPECIAL_IMPASSABLES = EnumSet.noneOf(Material.class);
    private static final EnumSet<Material> TRAPDOORS = EnumSet.noneOf(Material.class);
    private static final EnumSet<Material> SLABS = EnumSet.noneOf(Material.class);

    static {
        for (Material m : Material.values()) {
            if (m.name().contains("_CARPET"))
                SPECIAL_IMPASSABLES.add(m);
            if (m.name().contains("_WALL") || m.name().contains("_FENCE"))
                TALL_IMPASSABLES.add(m);
            if (m.name().contains("_TRAPDOOR"))
                TRAPDOORS.add(m);
            if(m.name().contains("_SLAB"))
                SLABS.add(m);
        }
    }

    public VillagerStorage( VillagerNerfer plugin) {
        this.plugin = plugin;
        villagers = new HashMap<>();
        restockCycle = new HashMap<>();
        restockInitiated = new HashMap<>();
        exemptVillagers = new ArrayList<>();
        checkInterval = plugin.getCustomConfig().getLong("check-interval");
        maxDailyRestocks = plugin.getCustomConfig().getInt("max-daily-restocks");
        cyclesTillNextRestock = plugin.getCustomConfig().getInt("cycles-till-next-restock");
        Bukkit.getScheduler().runTaskTimer(plugin,new FreezeTask(),checkInterval,checkInterval);
    }

    public final class FreezeTask implements Runnable {

        @Override
        public void run() {
            List<String> ids = new ArrayList<>();
            for(Villager v : villagers.values()) {
                if(!v.isValid()||v.isDead()||!v.getWorld().isChunkLoaded(v.getLocation().getBlockX()/16,v.getLocation().getBlockZ()/16)) {
                    ids.add(v.getUniqueId().toString());
                    continue;
                }

                if(canMove(v.getLocation())) {
                    v.setAware(true);
                    setAI(v,true);
                }
                else {

                    if(exemptVillagers.contains(v.getUniqueId().toString()))
                        return;

                    v.setAware(false);
                    setAI(v, false);

                    List<MerchantRecipe> recipes = v.getRecipes();

                    if(Bukkit.getServer().getWorlds().get(0).getTime() == 2000) {

                        final boolean[] resetNeeded = {false};
                        recipes.forEach(merchantRecipe -> {
                            if(merchantRecipe.getUses()>0) {
                                merchantRecipe.setUses(0);
                                resetNeeded[0] = true;
                            }
                        });
                        if(resetNeeded[0]) {
                            v.setRestocksToday(1);
                        }

                        restockCycle.put(v.getUniqueId().toString(),1);
                    }

                    if(v.getRestocksToday() < maxDailyRestocks) {

                        if(v.isTrading()) {
                            restockCycle.put(v.getUniqueId().toString(),1);
                            return;
                        }

                        if(restockCycle.get(v.getUniqueId().toString()) == cyclesTillNextRestock) {
                            final boolean[] restockNeeded = {false};

                            recipes.forEach(merchantRecipe -> {
                                if(merchantRecipe.getUses()>0) {
                                    merchantRecipe.setUses(0);
                                    restockNeeded[0] = true;
                                }
                            });
                            if(restockNeeded[0])
                                v.setRestocksToday(v.getRestocksToday() + 1);
                        }

                        cycleRestock(v.getUniqueId().toString());

                    }

                }

            }

            ids.forEach(id -> villagers.remove(id));

        }
    }

    public void showTradeParticleEffects(String uid) {
        Villager v = villagers.get(uid);
        if(v==null)
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            v.setAI(true);
            v.setAware(true);
        },1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            v.setAI(false);
            v.setAware(false);
        },2);

        //Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"particle minecraft:happy_villager " + v.getLocation().getX() + " " + (v.getLocation().getY() + 2.5) + " " + v.getLocation().getZ() + " 0.25 0.25 0.25 0.0 6 normal"),5);

    }

    public void checkForLevelUpgrade(String uid) {
        Villager v = villagers.get(uid);
        if(v==null||restockInitiated.get(v.getUniqueId().toString()))
            return;

        /*Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(Bukkit.getServer().getWorlds().get(0).getTime()>=2000 && Bukkit.getServer().getWorlds().get(0).getTime()<10000) {

                if(restockInitiated.get(v.getUniqueId().toString()) || v.isTrading())
                    return;



                if(v.getVillagerLevel()<setLevel(v.getVillagerExperience())) {
                    v.setVillagerLevel(setLevel(v.getVillagerExperience()));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"effect give " + v.getUniqueId() + " minecraft:regeneration 2 255");
                }
            }
        }, 20);*/

        if(v.getVillagerLevel()<setLevel(v.getVillagerExperience())){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                v.setAI(true);
                v.setAware(true);
                exemptVillagers.add(v.getUniqueId().toString());
                restockInitiated.put(v.getUniqueId().toString(),true);
            },1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                v.setAI(false);
                v.setAware(false);
                exemptVillagers.remove(v.getUniqueId().toString());
                restockInitiated.put(v.getUniqueId().toString(),false);
            },60);
        }

    }

    public void disableVillagerAIAfterUpgrade(String uid) {
        if(!villagers.containsKey(uid))
            return;
        Villager v = villagers.get(uid);

        v.setAI(false);
        v.setAware(false);
    }

    private static boolean canMove(Location l) {

        World w = l.getWorld();

        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();

        if(w.getBlockAt(x,y,z).isPassable() || w.getBlockAt(x,y,z).isLiquid() || SPECIAL_IMPASSABLES.contains(w.getBlockAt(x,y,z).getType())) {
            if((w.getBlockAt(x+1,y,z).isPassable()&&w.getBlockAt(x+1,y+1,z).isPassable())||(w.getBlockAt(x-1,y,z).isPassable()&&w.getBlockAt(x-1,y+1,z).isPassable())||(w.getBlockAt(x,y,z+1).isPassable()&&w.getBlockAt(x,y+1,z+1).isPassable())||(w.getBlockAt(x,y,z-1).isPassable()&&w.getBlockAt(x,y+1,z-1).isPassable())) {
                return true;
            }
            else if (w.getBlockAt(x,y+2,z).isPassable()) {
                if(TALL_IMPASSABLES.contains(w.getBlockAt(x+1,y,z).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x-1,y,z).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x,y,z+1).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x,y,z-1).getType())) {
                    return false;
                }
                else if((w.getBlockAt(x+1,y+2,z).isPassable()&&w.getBlockAt(x+1,y+1,z).isPassable())||(w.getBlockAt(x-1,y+2,z).isPassable()&&w.getBlockAt(x-1,y+1,z).isPassable())||(w.getBlockAt(x,y+2,z+1).isPassable()&&w.getBlockAt(x,y+1,z+1).isPassable())||(w.getBlockAt(x,y+2,z-1).isPassable()&&w.getBlockAt(x,y+1,z-1).isPassable())) {
                    return true;
                }
            }
            return false;
        }
        else {
            if (w.getBlockAt(x,y+2,z).isPassable()) {
                if(TALL_IMPASSABLES.contains(w.getBlockAt(x+1,y,z).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x-1,y,z).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x,y,z+1).getType())&&TALL_IMPASSABLES.contains(w.getBlockAt(x,y,z-1).getType())&&!SLABS.contains(w.getBlockAt(x,y,z).getType())) {
                    return false;
                }
                else if((w.getBlockAt(x+1,y+2,z).isPassable()&&w.getBlockAt(x+1,y+1,z).isPassable())||(w.getBlockAt(x-1,y+2,z).isPassable()&&w.getBlockAt(x-1,y+1,z).isPassable())||(w.getBlockAt(x,y+2,z+1).isPassable()&&w.getBlockAt(x,y+1,z+1).isPassable())||(w.getBlockAt(x,y+2,z-1).isPassable()&&w.getBlockAt(x,y+1,z-1).isPassable())) {
                    return true;
                }
                else return false;
            }
        }

        return false;
    }

    public void addVillager(@Nonnull Villager v) {
        villagers.put(v.getUniqueId().toString(),v);
        restockCycle.put(v.getUniqueId().toString(),1);
        restockInitiated.put(v.getUniqueId().toString(),false);
    }

    public void removeVillager(@Nonnull Villager v) {
        villagers.remove(v.getUniqueId().toString());
        restockCycle.remove(v.getUniqueId().toString());
        restockInitiated.remove(v.getUniqueId().toString());
    }

    private void setAI(Entity e, boolean ai) {
        LivingEntity livingEntity = (LivingEntity) e;
        livingEntity.setAI(ai);
    }

    private void cycleRestock(String uid) {
        int i = restockCycle.get(uid);
        if(i<=cyclesTillNextRestock)
            i++;
        else i=1;

        restockCycle.put(uid,i);
    }

    public void clearStorage() {
        villagers.clear();
        restockCycle.clear();
    }

    private int setLevel( int xp) {
        if(xp < 10)
            return 1;
        if(xp < 70)
            return 2;
        if(xp < 150)
            return 3;
        if(xp < 250)
            return 4;
        return 5;
    }

}
