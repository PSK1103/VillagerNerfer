package me.PSK1103.VillagerNerfer.helpers;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class VillagerListener implements Listener {

    private VillagerNerfer plugin;

    public VillagerListener(VillagerNerfer plugin) {
        this.plugin = plugin;
        Bukkit.getWorlds().forEach(world ->
                world.getEntities().forEach(entity -> {
                    if(entity instanceof Villager v) {
                        plugin.getStorage().addVillager(v);
                    }
                }));
    }

    @EventHandler
    public final void onAdd(@NotNull EntityAddToWorldEvent e) {
        if(e.getEntity() instanceof Villager)
            plugin.getStorage().addVillager((Villager)e.getEntity());
    }

    @EventHandler
    public final void onRemove(@NotNull EntityRemoveFromWorldEvent e) {
        if(e.getEntity() instanceof Villager)
            plugin.getStorage().removeVillager((Villager)e.getEntity());
    }

    @EventHandler
    public final void onCureZombieVillager(@NotNull PlayerInteractEntityEvent e) {
        if(!plugin.getCustomConfig().instantDezombification())
            return;
        if(e.getPlayer().getInventory().getItem(e.getHand()).getType() == Material.GOLDEN_APPLE &&
            e.getRightClicked().getType() == EntityType.ZOMBIE_VILLAGER &&
                ((LivingEntity) e.getRightClicked()).hasPotionEffect(PotionEffectType.WEAKNESS)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> ((ZombieVillager)e.getRightClicked()).setConversionTime(1),1);
        }
    }

}
