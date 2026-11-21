/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.RootModuleHolder;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class DoubleCarouselModule extends AbstractMeatgunModule
{
//    static Codec<DoubleCarouselModule> CODEC = Codec.of(
//            new Encoder<>()
//            {
//                @Override
//                public <T> DataResult<T> encode(DoubleCarouselModule input, DynamicOps<T> ops, T prefix)
//                {
//                    return null;
//                }
//            },
//            new Decoder<>()
//            {
//                @Override
//                public <T> DataResult<Pair<DoubleCarouselModule, T>> decode(DynamicOps<T> ops, T input)
//                {
//                    var list = ops.getList(input);
//                    return Pair.of();
//                }
//            }
//    );

//    static Codec<DoubleCarouselModule> CODEC = RecordCodecBuilder.create(instance ->
//    {
//        instance.group(
//        )
//    })

    private final ModuleSlot upSlot;
    private final ModuleSlot downSlot;
    private final ModuleSlot auxSlot;

    public DoubleCarouselModule(RootModuleHolder.Listener listener)
    {
        super(listener);
        auxSlot = new SimpleModuleSlot(this.listener, new Matrix4f().rotateY(Mth.PI).translate(0, 5 / 16f, 0 / 16f));
        downSlot = new SimpleModuleSlot(this.listener, new Matrix4f().rotateZ(Mth.PI).translate(0, 4 / 16f, -3 / 16f));
        upSlot = new SimpleModuleSlot(this.listener, new Matrix4f().translate(0, 4 / 16f, -3 / 16f));
        setSlots(List.of(upSlot, downSlot, auxSlot));
    }

    public DoubleCarouselModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.DOUBLE_CAROUSEL;
    }

}
