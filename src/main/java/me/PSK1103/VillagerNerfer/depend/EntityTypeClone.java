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

    private final EntityTypes.b<T> bn;

    private String bw;

    private MinecraftKey by;

    public EntityTypeClone(@NotNull EntityTypes<T> original, EntityTypes.b<T> entitytypes_b, EnumCreatureType enumcreaturetype, boolean flag, boolean flag1, boolean flag2, boolean flag3, ImmutableSet<Block> immutableset, EntitySize entitysize, int i, int j, String id) {
        super(entitytypes_b, enumcreaturetype, flag, flag1, flag2, flag3, immutableset, entitysize, i, j, id);
        this.original = original;
        this.bn = entitytypes_b;
    }

    public @NotNull String g() {
        if (this.bw == null)
            this.bw = SystemUtils.a("entity", IRegistry.W.b(this.original));
        return this.bw;
    }

    public @NotNull MinecraftKey j() {
        if (this.by == null) {
            MinecraftKey minecraftkey = IRegistry.W.b(this.original);
            this.by = new MinecraftKey(minecraftkey.b(), "entities/" + minecraftkey.a());
        }
        return this.by;
    }

    @Nullable
    public T a(World world) {
        return (T)this.bn.create(this.original, world);
    }

    public boolean p() {
        return (this.original != EntityTypes.bi && this.original != EntityTypes.W && this.original != EntityTypes.aZ && this.original != EntityTypes.f && this.original != EntityTypes.R && this.original != EntityTypes.H && this.original != EntityTypes.T && this.original != EntityTypes.aj && this.original != EntityTypes.u && this.original != EntityTypes.z);
    }

    public boolean a(TagKey<EntityTypes<?>> tag) {
        try {
            Field _bl = EntityTypes.class.getDeclaredField("bl");
            return ((Holder.c<EntityTypes<?>>)_bl.get(original)).a(tag);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return false;
    }
}

