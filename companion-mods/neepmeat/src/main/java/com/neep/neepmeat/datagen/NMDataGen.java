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

package com.neep.neepmeat.datagen;

import com.neep.meatlib.datagen.BlockTagProvider;
import com.neep.meatlib.datagen.MeatLibDataGen;
import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.datagen.MeatlibItemTagProvider;
import com.neep.meatlib.datagen.loot.BlockLootTableProvider;
import com.neep.meatweapons.datagen.MWRecipeGenerator;
import com.neep.neepmeat.client.datagen.NMModelProvider;
import com.neep.neepmeat.world.NMFeatures;
import com.neep.meatlib.datagen.FabricDataGenerator;

public class NMDataGen extends MeatLibDataGen
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockLootTableProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(MeatRecipeProvider::new);
        pack.addProvider(MWRecipeGenerator::new);
        pack.addProvider(NMRecipeGenerator::new);
        pack.addProvider(MeatlibItemTagProvider::new);
        pack.addProvider(NMModelProvider::new);
        pack.addProvider(NMAdvancements::new);
        pack.addProvider(NMBlockTagProvider::new);
        pack.addProvider(NMItemTagProvider::new);
        pack.addProvider(NMFeatures::new);
//        pack.addProvider(MeatlibAdvancementProvider::new);

        applyFactories(pack);
    }

//    @Override
//    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
//    {
//        NeepMeat.LOGGER.info("Initialising NEEPMeat datagen!");
//    }

//    @Override
//    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
//    {
//        var pack = fabricDataGenerator.createPack();
//        pack.addProvider(NMItemTagProvider::new);
//        System.out.println("ooer");
//    }
}
