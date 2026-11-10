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

package com.neep.meatlib.block;

import com.google.common.collect.Lists;
import com.neep.meatlib.registry.RegistrationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PaintedBlockCollection<T extends PaintedBlockCollection.PaintedBlock>
{
    public static final List<PaintedBlock> COLOURED_BLOCKS = new ArrayList<>();
    public final List<T> entries = Lists.newArrayList();

    public PaintedBlockCollection(RegistrationContext ctx, String name, Constructor<T> constructor, BlockBehaviour.Properties settings)
    {
        for (DyeColor col : DyeColor.values())
        {
            T block = ctx.addParent(new ResourceLocation(ctx.namespace(), name + "_" + col.getName()), constructor.create(ctx, col, settings));
//            T block = BlockRegistry.queue(constructor.create(registryName + "_" + col.getName(), col, settings));
            COLOURED_BLOCKS.add(block);
            entries.add(block);
        }
    }

    @FunctionalInterface
    public interface Constructor<T extends PaintedBlock>
    {
        T create(RegistrationContext ctx, DyeColor col, BlockBehaviour.Properties settings);
    }

    public abstract static class PaintedBlock extends Block implements MeatlibBlock
    {
        public final BlockItem blockItem;
        public final DyeColor col;

        public PaintedBlock(RegistrationContext ctx, DyeColor col, Properties settings)
        {
            super(settings);
            this.blockItem = makeItem(ctx);
            this.col = col;
        }

        protected abstract BlockItem makeItem(RegistrationContext ctx);

        public DyeColor getCol()
        {
            return this.col;
        }

        public int getRawCol()
        {
            return col.getFireworkColor();
        }

        public void generateRecipe(Consumer<FinishedRecipe> exporter)
        {

        }
    }
}
