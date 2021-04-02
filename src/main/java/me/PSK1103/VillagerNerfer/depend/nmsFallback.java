package me.PSK1103.VillagerNerfer.depend;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class nmsFallback implements Inms {
    private final VillagerNerfer plugin;

    private final Timing tickInactiveEntityTimer;

    private Timing tickVillagerTimer;

    private boolean bindFailure;

    private Field _tickTimer;

    private Method _getHandle;

    public nmsFallback(@NotNull VillagerNerfer plugin) {
        this.tickVillagerTimer = null;
        this.bindFailure = false;
        this.plugin = plugin;
        this.tickInactiveEntityTimer = Timings.of((Plugin)plugin, "Encaged Villagers", MinecraftTimings.tickEntityTimer);
    }

    private Field getTickTimer(@NotNull Object handle) throws ReflectiveOperationException {
        if (this._tickTimer == null)
            this._tickTimer = handle.getClass().getField("tickTimer");
        return this._tickTimer;
    }

    private Object getHandle(@NotNull Entity entity) throws ReflectiveOperationException {
        if (this._getHandle == null)
            this._getHandle = entity.getClass().getMethod("getHandle", new Class[0]);
        return this._getHandle.invoke(entity, new Object[0]);
    }

    public void setActive(@NotNull Entity entity) {
        if (this.bindFailure)
            return;
        try {
            Object handle = getHandle(entity);
            if (this.tickVillagerTimer != null)
                getTickTimer(handle).set(handle, this.tickVillagerTimer);
        } catch (ReflectiveOperationException e) {
            reportFailure(e);
        }
    }

    public void setInactive(@NotNull Entity entity) {
        if (this.bindFailure)
            return;
        try {
            Object handle = getHandle(entity);
            Timing inactive = getInactiveEntityTimings(entity);
            Field tickTimer = getTickTimer(handle);
            if (this.tickVillagerTimer == null &&
                    tickTimer.get(handle) != inactive)
                this.tickVillagerTimer = (Timing)tickTimer.get(handle);
            tickTimer.set(handle, inactive);
        } catch (ReflectiveOperationException e) {
            reportFailure(e);
        }
    }

    private Timing getInactiveEntityTimings(Entity entity) throws ReflectiveOperationException {
        String entityType = getHandle(entity).getClass().getName();
        return Timings.of((Plugin)this.plugin, "## tickEntity - " + entityType + " (inactive)", this.tickInactiveEntityTimer);
    }

    private void reportFailure(@NotNull Exception e) {
        this.bindFailure = true;
        this.plugin.getLogger().log(Level.WARNING, "The fallback NMS interface is not compatible with your server version " +

                Bukkit.getVersion() + ". The timings integration will not work.", e);
    }
}
