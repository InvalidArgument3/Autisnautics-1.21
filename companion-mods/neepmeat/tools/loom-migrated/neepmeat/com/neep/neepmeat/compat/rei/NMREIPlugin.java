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

package com.neep.neepmeat.compat.rei;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.compat.rei.display.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public interface NMREIPlugin
{
    CategoryIdentifier<GrindingDisplay> GRINDING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/grinding");
    CategoryIdentifier<GrindingDisplay> ADVANCED_CRUSHING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/advanced_crushing");
    CategoryIdentifier<BlockCrushingDisplay> BLOCK_CRUSHING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/block_crushing");
    CategoryIdentifier<BlockCrushingDisplay> ADVANCED_BLOCK_CRUSHING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/advanced_block_crushing");
    CategoryIdentifier<CompactingDisplay> COMPACTING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/compacting");
    CategoryIdentifier<MixingDisplay> MIXING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/mixing");
    CategoryIdentifier<AlloySmeltingDisplay> ALLOY_SMELTING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/alloy_smelting");
    CategoryIdentifier<VivisectionDisplay> VIVISECTION = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/vivisection");
    CategoryIdentifier<EnlighteningDisplay> ENLIGHTENING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/enlightening");
    CategoryIdentifier<PressingDisplay> PRESSING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/pressing");

    CategoryIdentifier<ItemManufactureDisplay> ITEM_MANUFACTURE = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/item_manufacture");
    CategoryIdentifier<EntityToItemDisplay> ENTITY_TO_ITEM = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/entity_to_item_manufacture");

    CategoryIdentifier<TrommelDisplay> TROMMEL = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/trommel");
    CategoryIdentifier<HeatingDisplay> HEATING = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/heating");
    CategoryIdentifier<TransformingToolDisplay> TRANSFORMING_TOOL = CategoryIdentifier.of(NeepMeat.NAMESPACE, "plugins/transforming_tool");
}
