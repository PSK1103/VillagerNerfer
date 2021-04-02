package me.PSK1103.VillagerNerfer.depend;


import java.util.logging.Level;
import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface Inms {
    void setInactive(@NotNull Entity paramEntity);

    void setActive(@NotNull Entity paramEntity);

    @NotNull
    static Inms get(@NotNull VillagerNerfer plugin) {
        if (plugin.getCustomConfig().timingsEnabled())
            try {
                return new nms_1_16_4(plugin);
            } catch (Throwable t) {
                plugin.getLogger().log(Level.INFO, "Encountered an exception while attempting to use native 1.16.4 NMS interface: " + t);
                plugin.getLogger().warning("Your server version " + Bukkit.getBukkitVersion() + " is not natively supported. A fallback interface will be used. Performance may be affected, and the Timings integration may not work.");
                return new nmsFallback(plugin);
            }
        return new Inms() {
            public void setInactive(@NotNull Entity entity) {}

            public void setActive(@NotNull Entity entity) {}
        };
    }
}
