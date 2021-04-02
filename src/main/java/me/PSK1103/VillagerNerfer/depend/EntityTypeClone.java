package me.PSK1103.VillagerNerfer.depend;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntitySize;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.SystemUtils;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.World;
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

    public String f() {
        if (this.bo == null)
            this.bo = SystemUtils.a("entity", IRegistry.ENTITY_TYPE.getKey(this.original));
        return this.bo;
    }

    public MinecraftKey i() {
        if (this.bq == null) {
            MinecraftKey minecraftkey = IRegistry.ENTITY_TYPE.getKey(this.original);
            this.bq = new MinecraftKey(minecraftkey.getNamespace(), "entities/" + minecraftkey.getKey());
        }
        return this.bq;
    }

    @Nullable
    public T a(World world) {
        return (T)this.bf.create(this.original, world);
    }

    public boolean isDeltaTracking() {
        return (this.original != EntityTypes.PLAYER && this.original != EntityTypes.LLAMA_SPIT && this.original != EntityTypes.WITHER && this.original != EntityTypes.BAT && this.original != EntityTypes.ITEM_FRAME && this.original != EntityTypes.LEASH_KNOT && this.original != EntityTypes.PAINTING && this.original != EntityTypes.END_CRYSTAL && this.original != EntityTypes.EVOKER_FANGS);
    }

    public boolean a(Tag<EntityTypes<?>> tag) {
        return tag.isTagged(this.original);
    }
}

