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
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class nms_1_19 implements Inms{
    private final VillagerNerfer plugin;
    private final Map<EntityTypes<?>, Map<String[], EntityTypes<?>>> cache;
    private final Map<EntityTypes<?>, EntityTypes<?>> originals;
    private boolean enabled;

    private final Field _bs;
    private final Field _bt;
    private final Field _bu;
    private final Field _bv;
    private final Field _bw;
    private final Field _bx;
    private final Field _by;
    private final Field _bz;
    private final Field _bA;
    private final Field _bE;
    private final Field _id;
    private final Field _tickTimer;
    private final Field _inactiveTickTimer;
    private final Field _passengerTickTimer;
    private final Field _passengerInactiveTickTimer;
    private final Field _as;
    private final Field _materials_bT;

    public nms_1_19(VillagerNerfer plugin) throws ReflectiveOperationException{
        this.plugin = plugin;
        cache = new HashMap<>();
        originals = new HashMap<>();
        enabled = true;
        this._bs = EntityTypes.class.getDeclaredField("bs");
        this._bt = EntityTypes.class.getDeclaredField("bt");
        this._bu = EntityTypes.class.getDeclaredField("bu");
        this._bv = EntityTypes.class.getDeclaredField("bv");
        this._bw = EntityTypes.class.getDeclaredField("bw");
        this._bx = EntityTypes.class.getDeclaredField("bx");
        this._by = EntityTypes.class.getDeclaredField("by");
        this._bz = EntityTypes.class.getDeclaredField("bz");
        this._bA = EntityTypes.class.getDeclaredField("bA");
        this._bE = EntityTypes.class.getDeclaredField("bE");
        this._id = EntityTypes.class.getDeclaredField("id");
        this._bs.setAccessible(true);
        this._bt.setAccessible(true);
        this._bu.setAccessible(true);
        this._bv.setAccessible(true);
        this._bw.setAccessible(true);
        this._bx.setAccessible(true);
        this._by.setAccessible(true);
        this._bz.setAccessible(true);
        this._bA.setAccessible(true);
        this._bE.setAccessible(true);
        this._id.setAccessible(true);
        this._tickTimer = EntityTypes.class.getField("tickTimer");
        this._inactiveTickTimer = EntityTypes.class.getField("inactiveTickTimer");
        this._passengerTickTimer = EntityTypes.class.getField("passengerTickTimer");
        this._passengerInactiveTickTimer = EntityTypes.class.getField("passengerInactiveTickTimer");
        this._tickTimer.setAccessible(true);
        this._inactiveTickTimer.setAccessible(true);
        this._passengerTickTimer.setAccessible(true);
        this._passengerInactiveTickTimer.setAccessible(true);
        this._as = net.minecraft.world.entity.Entity.class.getDeclaredField("as");
        this._as.setAccessible(true);
        this._materials_bT = RegistryMaterials.class.getDeclaredField("bT");
        this._materials_bT.setAccessible(true);
        try {
            Field rootField = Field.class.getDeclaredField("root");
            rootField.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            Field root = (Field)rootField.get(this._as);
            modifiers.setInt(root, modifiers.getInt(root) & 0xFFFFFFEF);
            modifiers.setInt(this._as, modifiers.getInt(root) & 0xFFFFFFEF);
        } catch (Exception ignored) {}
        unfreezeRegistry();
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
        net.minecraft.world.entity.EntityTypes<T> original = (net.minecraft.world.entity.EntityTypes<T>)this._as.get(entity);
        plugin.getLogger().info("B - orig type " + original.id);
        net.minecraft.world.entity.EntityTypes<T> modified = (net.minecraft.world.entity.EntityTypes<T>) getType(original, type);
        plugin.getLogger().info("C - mod type " + modified.id);
        this._as.set(entity, modified);
    }

    public <T extends net.minecraft.world.entity.Entity> void resetTimingsHandler(@NotNull net.minecraft.world.entity.Entity entity) throws ReflectiveOperationException {
        net.minecraft.world.entity.EntityTypes<T> modified = (net.minecraft.world.entity.EntityTypes<T>)this._as.get(entity);
        net.minecraft.world.entity.EntityTypes<T> original = (net.minecraft.world.entity.EntityTypes<T>)this.originals.getOrDefault(modified, modified);
        this._as.set(entity, original);
    }

    @NotNull
    private <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityTypes<T> cloneType(@NotNull net.minecraft.world.entity.EntityTypes<T> original, String[] type) {
        try {
            String id = (String)this._id.get(original);
            EntityTypeClone<T> clone = new EntityTypeClone<>(original, (net.minecraft.world.entity.EntityTypes.b<T>)this._bs.get(original), (EnumCreatureType) this._bt.get(original), (Boolean) this._bv.get(original), (Boolean) this._bw.get(original), (Boolean) this._bx.get(original), (Boolean) this._by.get(original), (ImmutableSet<Block>)this._bu.get(original), (EntitySize) this._bE.get(original), (Integer) this._bz.get(original), (Integer) this._bA.get(original), id);
            Map ids = (Map)this._materials_bT.get(IRegistry.X);
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
            if (this.originals.containsKey(nmsEntity.ad()))
                return;
            nmsEntity.spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
            plugin.getLogger().info("A - type " + EntityTypes.a(nmsEntity.ad()).a());
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
            if (!this.originals.containsKey(nmsEntity.ad()))
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

    private static void unfreezeRegistry() {
        /* As of 1.18.2, registries are frozen once NMS is done adding to them,
           so we have to do some super hacky things to add custom entities now.
           Basically, when the registry is frozen, the "frozen" field is set to prevent new entries,
           and a map "intrusiveHolderCache" is set to null (don't really know what it does.)
           If frozen is true or "intrusiveHolderCache" is null, it will refuse to add entries,
           so we just have to fix both of those things and it'll let us add entries again.
           The registry being frozen may be vital to how the registry works (idk), so it is refrozen after adding our entries.

           Partial stack trace produced when trying to add entities when the registry is frozen:
           [Server thread/ERROR]: Registry is already frozen initializing UltraCosmetics v2.6.1-DEV-b5 (Is it up to date?)
            java.lang.IllegalStateException: Registry is already frozen
                    at net.minecraft.core.RegistryMaterials.e(SourceFile:343) ~[spigot-1.18.2-R0.1-SNAPSHOT.jar:3445-Spigot-fb0dd5f-05a38da]
                    at net.minecraft.world.entity.EntityTypes.<init>(EntityTypes.java:300) ~[spigot-1.18.2-R0.1-SNAPSHOT.jar:3445-Spigot-fb0dd5f-05a38da]
                    at net.minecraft.world.entity.EntityTypes$Builder.a(EntityTypes.java:669) ~[spigot-1.18.2-R0.1-SNAPSHOT.jar:3445-Spigot-fb0dd5f-05a38da]
                    at be.isach.ultracosmetics.v1_18_R2.customentities.CustomEntities.registerEntity(CustomEntities.java:78) ~[?:?]
        */
        try {
            Field intrusiveHolderCache = RegistryMaterials.class.getDeclaredField("cc");
            intrusiveHolderCache.setAccessible(true);
            intrusiveHolderCache.set(IRegistry.X, new IdentityHashMap<>());
            Field frozen = RegistryMaterials.class.getDeclaredField("ca");
            frozen.setAccessible(true);
            frozen.set(IRegistry.X, false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
