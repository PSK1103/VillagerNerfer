package me.PSK1103.VillagerNerfer.depend;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntitySize;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.RegistryMaterials;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

public class nms_1_16_4 implements Inms {
    private final VillagerNerfer plugin;

    private final Field _bf;

    private final Field _bg;

    private final Field _bl;

    private final Field _bi;

    private final Field _bj;

    private final Field _bk;

    private final Field _bh;

    private final Field _br;

    private final Field _bm;

    private final Field _bn;

    private final Field _id;

    private final Field _tickTimer;

    private final Field _inactiveTickTimer;

    private final Field _passengerTickTimer;

    private final Field _passengerInactiveTickTimer;

    private final Field _f;

    private final Field _materials_bg;

    private final Map<EntityTypes<?>, Map<String[], EntityTypes<?>>> cache;

    private final Map<EntityTypes<?>, EntityTypes<?>> originals;

    private boolean enabled;

    public nms_1_16_4(@NotNull VillagerNerfer plugin) throws ReflectiveOperationException {
        this.cache = new HashMap<>();
        this.originals = new HashMap<>();
        this.enabled = true;
        this.plugin = plugin;
        this._bf = EntityTypes.class.getDeclaredField("bf");
        this._bg = EntityTypes.class.getDeclaredField("bg");
        this._bl = EntityTypes.class.getDeclaredField("bl");
        this._bi = EntityTypes.class.getDeclaredField("bi");
        this._bj = EntityTypes.class.getDeclaredField("bj");
        this._bk = EntityTypes.class.getDeclaredField("bk");
        this._bh = EntityTypes.class.getDeclaredField("bh");
        this._br = EntityTypes.class.getDeclaredField("br");
        this._bm = EntityTypes.class.getDeclaredField("bm");
        this._bn = EntityTypes.class.getDeclaredField("bn");
        this._id = EntityTypes.class.getDeclaredField("id");
        this._bf.setAccessible(true);
        this._bg.setAccessible(true);
        this._bl.setAccessible(true);
        this._bi.setAccessible(true);
        this._bj.setAccessible(true);
        this._bk.setAccessible(true);
        this._bh.setAccessible(true);
        this._br.setAccessible(true);
        this._bm.setAccessible(true);
        this._bn.setAccessible(true);
        this._id.setAccessible(true);
        this._tickTimer = EntityTypes.class.getField("tickTimer");
        this._inactiveTickTimer = EntityTypes.class.getField("inactiveTickTimer");
        this._passengerTickTimer = EntityTypes.class.getField("passengerTickTimer");
        this._passengerInactiveTickTimer = EntityTypes.class.getField("passengerInactiveTickTimer");
        this._tickTimer.setAccessible(true);
        this._inactiveTickTimer.setAccessible(true);
        this._passengerTickTimer.setAccessible(true);
        this._passengerInactiveTickTimer.setAccessible(true);
        this._f = Entity.class.getDeclaredField("f");
        this._f.setAccessible(true);
        this._materials_bg = RegistryMaterials.class.getDeclaredField("bg");
        this._materials_bg.setAccessible(true);
        try {
            Field rootField = Field.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            Field root = (Field)rootField.get(this._f);
            modifiers.setInt(root, modifiers.getInt(root) & 0xFFFFFFEF);
            modifiers.setInt(this._f, modifiers.getInt(root) & 0xFFFFFFEF);
        } catch (Exception exception) {}
    }

    @NotNull
    public <T extends Entity> EntityTypes<?> getType(@NotNull EntityTypes<T> original, @NotNull String[] type) throws ReflectiveOperationException {
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

    public <T extends Entity> void setTimingsHandler(@NotNull Entity entity, @NotNull String[] type) throws ReflectiveOperationException {
        EntityTypes<T> original = (EntityTypes<T>)this._f.get(entity);
        EntityTypes<T> modified = (EntityTypes<T>) getType(original, type);
        this._f.set(entity, modified);
    }

    public <T extends Entity> void resetTimingsHandler(@NotNull Entity entity) throws ReflectiveOperationException {
        EntityTypes<T> modified = (EntityTypes<T>)this._f.get(entity);
        EntityTypes<T> original = (EntityTypes<T>)this.originals.getOrDefault(modified, modified);
        this._f.set(entity, original);
    }

    @NotNull
    private <T extends Entity> EntityTypes<T> cloneType(@NotNull EntityTypes<T> original, String[] type) {
        try {
            String id = (String)this._id.get(original);
            EntityTypeClone<T> clone = new EntityTypeClone<>(original, (EntityTypes.b<T>)this._bf.get(original), (EnumCreatureType)this._bg.get(original), ((Boolean)this._bi.get(original)).booleanValue(), ((Boolean)this._bj.get(original)).booleanValue(), ((Boolean)this._bk.get(original)).booleanValue(), ((Boolean)this._bl.get(original)).booleanValue(), (ImmutableSet<Block>)this._bh.get(original), (EntitySize)this._br.get(original), ((Integer)this._bm.get(original)).intValue(), ((Integer)this._bn.get(original)).intValue(), id);
            Map ids = (Map)this._materials_bg.get(IRegistry.ENTITY_TYPE);
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
        Entity nmsEntity = ((CraftEntity)entity).getHandle();
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
        Entity nmsEntity = ((CraftEntity)entity).getHandle();
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