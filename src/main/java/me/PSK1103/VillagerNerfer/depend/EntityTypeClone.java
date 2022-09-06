package me.PSK1103.VillagerNerfer.depend;

import com.google.common.collect.ImmutableSet;

import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class EntityTypeClone<T extends Entity> extends EntityTypes<T> {
    private final EntityTypes<T> original;

    private final EntityTypes.b<T> bs;

    private String bB;

    private MinecraftKey bD;

    public EntityTypeClone(@NotNull EntityTypes<T> original, EntityTypes.b<T> factory, EnumCreatureType spawnGroup, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> canSpawnInside, EntitySize dimensions, int maxTrackDistance, int trackTickInterval, String id) {
        super(factory, spawnGroup, saveable, summonable, fireImmune, spawnableFarFromPlayer, canSpawnInside, dimensions, maxTrackDistance, trackTickInterval, id);
        this.original = original;
        this.bs = factory;
    }

    public @NotNull String g() {
        if (this.bB == null)
            this.bB = SystemUtils.a("entity", IRegistry.X.b(this.original));
        return this.bB;
    }

    public @NotNull MinecraftKey j() {
        if (this.bD == null) {
            MinecraftKey minecraftkey = IRegistry.X.b(this.original);
            this.bD = new MinecraftKey(minecraftkey.b(), "entities/" + minecraftkey.a());
        }
        return this.bD;
    }

    @Nullable
    public T a(World world) {
        return (T)this.bs.create(this.original, world);
    }

    public boolean p() {
        return (this.original != EntityTypes.bn && this.original != EntityTypes.Z && this.original != EntityTypes.be && this.original != EntityTypes.g && this.original != EntityTypes.U && this.original != EntityTypes.K && this.original != EntityTypes.W && this.original != EntityTypes.am && this.original != EntityTypes.w && this.original != EntityTypes.B);
    }

    public boolean a(TagKey<EntityTypes<?>> tag) {
        try {
            Field _bq = EntityTypes.class.getDeclaredField("bq");
            return ((Holder.c<EntityTypes<?>>)_bq.get(original)).a(tag);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return false;
    }
}

