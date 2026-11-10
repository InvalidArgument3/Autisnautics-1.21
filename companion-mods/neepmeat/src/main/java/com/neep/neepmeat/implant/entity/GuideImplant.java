package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GuideImplant implements EntityImplant
{
    public static final ResourceLocation ID = ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "guide");

    public GuideImplant(Entity entity) {}

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove) { }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) { return nbt; }

    @Override
    public void readNbt(CompoundTag nbt) { }
}
