package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class BaseLongGunModule extends BasePistolModule
{
    public BaseLongGunModule(RootModuleHolder.Listener listener)
    {
        super(listener);
        ModuleSlot front = new SimpleModuleSlot(this.listener, new Matrix4f());

        // This base module already has a stock.
        // The stock slot is positioned so that only the rear two cubes protrude.
        ModuleSlot stock = new SimpleModuleSlot(this.listener, new Matrix4f().rotateY(Mth.PI).translate(0, -0.001f, -9 / 16f), SlotType.STOCK, true);
        ModuleSlot scope = new SimpleModuleSlot(this.listener, new Matrix4f().translate(0, 3 / 16f, 5 / 16f), SlotType.SIGHT, true);
        ModuleSlot aux = new SimpleModuleSlot(this.listener, new Matrix4f().rotateY(-Mth.HALF_PI).translate(4 / 16f, 0, -2 / 16f));
        setSlots(List.of(front, stock, scope, aux));
    }

    public static BaseLongGunModule fromNbt(RootModuleHolder.Listener listener, CompoundTag nbtCompound)
    {
        return new BaseLongGunModule(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BASE_LONG_GUN;
    }
}
