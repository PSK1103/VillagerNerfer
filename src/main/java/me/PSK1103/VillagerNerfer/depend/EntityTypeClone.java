package me.PSK1103.VillagerNerfer.depend;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;

import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class EntityTypeClone<T extends Entity> extends EntityTypes<T> {
    private final EntityTypes<T> original;

    private final EntityTypes.b<T> bf;

    private String bo;

    private MinecraftKey bq;

    public EntityTypeClone(@NotNull EntityTypes<T> original, EntityTypes.b<T> entitytypes_b, EnumCreatureType enumcreaturetype, boolean flag, boolean flag1, boolean flag2, boolean flag3, ImmutableSet<Block> immutableset, EntitySize entitysize, int i, int j, String id) {
        super(entitytypes_b, enumcreaturetype, flag, flag1, flag2, flag3, immutableset, entitysize, i, j);
        this.original = original;
        this.bf = entitytypes_b;
    }

    public @NotNull String g() {
        if (this.bo == null)
            this.bo = SystemUtils.a("entity", IRegistry.Y.getKey(this.original));
        return this.bo;
    }

    public @NotNull MinecraftKey j() {
        if (this.bq == null) {
            MinecraftKey minecraftkey = IRegistry.Y.getKey(this.original);
            this.bq = new MinecraftKey(minecraftkey.getNamespace(), "entities/" + minecraftkey.getKey());
        }
        return this.bq;
    }

    @Nullable
    public T a(World world) {
        return (T)this.bf.create(this.original, world);
    }

    public boolean isDeltaTracking() {
        return (this.original != EntityTypes.bi && this.original != EntityTypes.W && this.original != EntityTypes.aZ && this.original != EntityTypes.f && this.original != EntityTypes.R && this.original != EntityTypes.T && this.original != EntityTypes.aj && this.original != EntityTypes.u && this.original != EntityTypes.z);
    }

    public boolean a(Tag<EntityTypes<?>> tag) {
        return tag.isTagged(this.original);
    }
}

