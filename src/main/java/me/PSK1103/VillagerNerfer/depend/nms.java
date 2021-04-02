package me.PSK1103.VillagerNerfer.depend;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class nms implements Inms {
    private final VillagerNerfer plugin;

    private final Timing tickInactiveEntityTimer;

    public nms(@NotNull VillagerNerfer plugin) {
        this.plugin = plugin;
        this.tickInactiveEntityTimer = Timings.of((Plugin)plugin, "Encaged Villagers", MinecraftTimings.tickEntityTimer);
    }

    private Timing tickVillagerTimer = null;

    public void setActive(@NotNull Entity entity) {
        if (this.tickVillagerTimer != null);
    }

    public void setInactive(@NotNull Entity entity) {}

    private Timing getInactiveEntityTimings(Entity entity) {
        String entityType = ((CraftEntity)entity).getHandle().getClass().getName();
        return Timings.of((Plugin)this.plugin, "## tickEntity - " + entityType + " (inactive)", this.tickInactiveEntityTimer);
    }
}
