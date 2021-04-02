package me.PSK1103.VillagerNerfer.helpers;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.MerchantInventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class VillagerListener implements Listener {

    private VillagerNerfer plugin;

    public VillagerListener(VillagerNerfer plugin) {
        this.plugin = plugin;
        Bukkit.getWorlds().forEach(world ->
                world.getEntities().forEach(entity -> {
                    if(entity instanceof Villager) {
                        Villager v = (Villager) entity;
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
    public final void onRemove(@Nonnull EntityRemoveFromWorldEvent e) {
        if(e.getEntity() instanceof Villager)
            plugin.getStorage().removeVillager((Villager)e.getEntity());
    }
}
