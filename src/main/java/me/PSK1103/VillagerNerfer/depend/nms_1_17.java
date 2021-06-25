package me.PSK1103.VillagerNerfer.depend;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableSet;
import me.PSK1103.VillagerNerfer.VillagerNerfer;

import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class nms_1_17 implements Inms{
    private final VillagerNerfer plugin;
    private final Map<EntityTypes<?>, Map<String[], EntityTypes<?>>> cache;
    private final Map<EntityTypes<?>, EntityTypes<?>> originals;
    private boolean enabled;

    private final Field _bm;
    private final Field _bn;
    private final Field _bo;
    private final Field _bp;
    private final Field _bq;
    private final Field _br;
    private final Field _bs;
    private final Field _bt;
    private final Field _bu;
    private final Field _by;
    private final Field _id;
    private final Field _tickTimer;
    private final Field _inactiveTickTimer;
    private final Field _passengerTickTimer;
    private final Field _passengerInactiveTickTimer;
    private final Field _ar;
    private final Field _materials_bw;

    public nms_1_17(VillagerNerfer plugin) throws ReflectiveOperationException{
        this.plugin = plugin;
        cache = new HashMap<>();
        originals = new HashMap<>();
        enabled = true;
        this._bm = EntityTypes.class.getDeclaredField("bm");
        this._bn = EntityTypes.class.getDeclaredField("bn");
        this._bo = EntityTypes.class.getDeclaredField("bo");
        this._bp = EntityTypes.class.getDeclaredField("bp");
        this._bq = EntityTypes.class.getDeclaredField("bq");
        this._br = EntityTypes.class.getDeclaredField("br");
        this._bs = EntityTypes.class.getDeclaredField("bs");
        this._bt = EntityTypes.class.getDeclaredField("bt");
        this._bu = EntityTypes.class.getDeclaredField("bu");
        this._by = EntityTypes.class.getDeclaredField("by");
        this._id = EntityTypes.class.getDeclaredField("id");
        this._bm.setAccessible(true);
        this._bn.setAccessible(true);
        this._bo.setAccessible(true);
        this._bp.setAccessible(true);
        this._bq.setAccessible(true);
        this._br.setAccessible(true);
        this._bs.setAccessible(true);
        this._bt.setAccessible(true);
        this._bu.setAccessible(true);
        this._by.setAccessible(true);
        this._id.setAccessible(true);
        this._tickTimer = EntityTypes.class.getField("tickTimer");
        this._inactiveTickTimer = EntityTypes.class.getField("inactiveTickTimer");
        this._passengerTickTimer = EntityTypes.class.getField("passengerTickTimer");
        this._passengerInactiveTickTimer = EntityTypes.class.getField("passengerInactiveTickTimer");
        this._tickTimer.setAccessible(true);
        this._inactiveTickTimer.setAccessible(true);
        this._passengerTickTimer.setAccessible(true);
        this._passengerInactiveTickTimer.setAccessible(true);
        this._ar = net.minecraft.world.entity.Entity.class.getDeclaredField("ar");
        this._ar.setAccessible(true);
        this._materials_bw = RegistryMaterials.class.getDeclaredField("bw");
        this._materials_bw.setAccessible(true);
        try {
            Field rootField = Field.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            Field root = (Field)rootField.get(this._ar);
            modifiers.setInt(root, modifiers.getInt(root) & 0xFFFFFFEF);
            modifiers.setInt(this._ar, modifiers.getInt(root) & 0xFFFFFFEF);
        } catch (Exception exception) {}
    }

    @NotNull
    public <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityTypes<?> getType(@NotNull net.minecraft.world.entity.EntityTypes<T> original, @NotNull String[] type) throws ReflectiveOperationException {
        try {
            return this.cache
                    .computeIfAbsent(original, k -> new HashMap<>())
                    .computeIfAbsent(type, k2 -> cloneType(original, type));
        } catch (RuntimeException r) {
            if (r.getCause() instanceof ReflectiveOperationException)
                throw (ReflectiveOperationException)r.getCause();
            throw r;
        }
    }

    public <T extends net.minecraft.world.entity.Entity> void setTimingsHandler(@NotNull net.minecraft.world.entity.Entity entity, @NotNull String[] type) throws ReflectiveOperationException {
        net.minecraft.world.entity.EntityTypes<T> original = (net.minecraft.world.entity.EntityTypes<T>)this._ar.get(entity);
        net.minecraft.world.entity.EntityTypes<T> modified = (net.minecraft.world.entity.EntityTypes<T>) getType(original, type);
        this._ar.set(entity, modified);
    }

    public <T extends net.minecraft.world.entity.Entity> void resetTimingsHandler(@NotNull net.minecraft.world.entity.Entity entity) throws ReflectiveOperationException {
        net.minecraft.world.entity.EntityTypes<T> modified = (net.minecraft.world.entity.EntityTypes<T>)this._ar.get(entity);
        net.minecraft.world.entity.EntityTypes<T> original = (net.minecraft.world.entity.EntityTypes<T>)this.originals.getOrDefault(modified, modified);
        this._ar.set(entity, original);
    }

    @NotNull
    private <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityTypes<T> cloneType(@NotNull net.minecraft.world.entity.EntityTypes<T> original, String[] type) {
        try {
            String id = (String)this._id.get(original);
            EntityTypeClone<T> clone = new EntityTypeClone<>(original, (net.minecraft.world.entity.EntityTypes.b<T>)this._bm.get(original), (EnumCreatureType) this._bn.get(original), ((Boolean)this._bp.get(original)).booleanValue(), ((Boolean)this._bq.get(original)).booleanValue(), ((Boolean)this._br.get(original)).booleanValue(), ((Boolean)this._bs.get(original)).booleanValue(), (ImmutableSet<Block>)this._bo.get(original), (EntitySize) this._by.get(original), ((Integer)this._bt.get(original)).intValue(), ((Integer)this._bu.get(original)).intValue(), id);
            Map ids = (Map)this._materials_bw.get(IRegistry.Y);
            ids.put(clone, ids.get(original));
            Timing tickTimer = (type.length > 0 && type[0] != null) ? MinecraftTimings.getEntityTimings(id, type[0]) : original.tickTimer;
            Timing inactiveTickTimer = (type.length > 1 && type[1] != null) ? MinecraftTimings.getEntityTimings(id, type[1]) : original.tickTimer;
            Timing passengerTickTimer = (type.length > 2 && type[2] != null) ? MinecraftTimings.getEntityTimings(id, type[2]) : original.tickTimer;
            Timing passengerInactiveTickTimer = (type.length > 3 && type[3] != null) ? MinecraftTimings.getEntityTimings(id, type[3]) : original.tickTimer;
            this._tickTimer.set(clone, tickTimer);
            this._inactiveTickTimer.set(clone, inactiveTickTimer);
            this._passengerTickTimer.set(clone, passengerTickTimer);
            this._passengerInactiveTickTimer.set(clone, passengerInactiveTickTimer);
            this.originals.put(clone, original);
            return clone;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInactive(@NotNull org.bukkit.entity.Entity entity) {
        if (!this.enabled)
            return;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        try {
            if (this.originals.containsKey(nmsEntity.getEntityType()))
                return;
            nmsEntity.spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
            setTimingsHandler(nmsEntity, new String[] { "tick (Lobotomized)", "inactiveTick (Lobotomized)", "passengerTick (Lobotomized)", "passengerInactiveTick (Lobotomized)" });
        } catch (Throwable e) {
            this.enabled = false;
            this.plugin.getLogger().log(Level.SEVERE, "Encountered an exception while setting the timings handler of entity " + entity + ". The timings integration will be disabled.", e);
        }
    }

    public void setActive(@NotNull org.bukkit.entity.Entity entity) {
        if (!this.enabled)
            return;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        try {
            if (!this.originals.containsKey(nmsEntity.getEntityType()))
                return;
            nmsEntity.spawnReason = CreatureSpawnEvent.SpawnReason.BREEDING;
            resetTimingsHandler(nmsEntity);
        } catch (Throwable e) {
            this.plugin.getLogger().log(Level.SEVERE, "Encountered an exception while setting the timings handler of entity " + entity + ". The timings integration will be disabled.", e);
            this.enabled = false;
        }
    }

    @NotNull
    private static String hash(@NotNull Object o) {
        return Integer.toHexString(Objects.hashCode(o));
    }
}
