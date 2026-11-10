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

package com.neep.neepmeat.client.meatgun;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

public class RotaryDrillHeadModel
{
    public final ResourceLocation head1Id;
    public final ResourceLocation head2Id;
    public final ResourceLocation head3Id;
    public final ResourceLocation head4Id;

    public RotaryDrillHeadModel(ResourceLocation root)
    {
        this.head1Id = ResourceLocation.fromNamespaceAndPath(root.getNamespace(), root.getPath() + "/head_1");
        this.head2Id = ResourceLocation.fromNamespaceAndPath(root.getNamespace(), root.getPath() + "/head_2");
        this.head3Id = ResourceLocation.fromNamespaceAndPath(root.getNamespace(), root.getPath() + "/head_3");
        this.head4Id = ResourceLocation.fromNamespaceAndPath(root.getNamespace(), root.getPath() + "/head_4");
    }

    public void apply(ModelLoadingPlugin.Context ctx)
    {
        ctx.addModels(head1Id);
        ctx.addModels(head2Id);
        ctx.addModels(head3Id);
        ctx.addModels(head4Id);
    }
}
