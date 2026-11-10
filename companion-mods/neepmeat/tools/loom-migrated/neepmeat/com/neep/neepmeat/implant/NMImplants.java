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

package com.neep.neepmeat.implant;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.config.*;
import com.neep.neepmeat.implant.entity.*;
import com.neep.neepmeat.implant.item.LivingToolImplant;
import com.neep.neepmeat.implant.item.PneumaticMotorImplant;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class NMImplants
{
    public static final ResourceLocation EMPTY = new ResourceLocation(NeepMeat.NAMESPACE, "empty");

    public static void init()
    {
        ImplantRegistry.register(PoweredKneesImplant.ID, PoweredKneesImplant::new, NMItems.EXTRA_KNEES);
        ImplantRegistry.register(PinealEyeImplant.ID, PinealEyeImplant::new, NMItems.PINEAL_EYE);
        ImplantRegistry.register(ExtraMouthImplant.ID, ExtraMouthImplant::new, NMItems.EXTRA_MOUTH);
        ImplantRegistry.register(LungExtensionsImplant.ID, LungExtensionsImplant::new, NMItems.LUNG_EXTENSIONS);
        ImplantRegistry.register(SpiderLegsImplant.ID, SpiderLegsImplant::new, NMItems.SPIDER_LEGS);
        ImplantRegistry.register(ExplosiveImplant.ID, ExplosiveImplant::new, Items.TNT);
        ImplantRegistry.register(ClawsImplant.ID, ClawsImplant::new, NMItems.CLAWS);
        ImplantRegistry.register(CatEarsImplant.ID, CatEarsImplant::new, NMItems.CAT_EARS);
        ImplantRegistry.register(PipeClimbImplant.ID, PipeClimbImplant::new, NMItems.PIPE_CLIMBER);
        ImplantRegistry.register(GuideImplant.ID, GuideImplant::new, NMItems.PROJECTOR);

        ImplantRegistry.registerItem(LivingToolImplant.ID, NMItems.LIVING_TOOL_IMPLANT);
        ImplantRegistry.registerItem(PneumaticMotorImplant.ID, NMItems.PNEUMATIC_MOTOR);

        Registry.register(EntityImplantInstaller.REGISTRY, PinealEyeImplant.ID, NMItems.PINEAL_EYE);
        Registry.register(EntityImplantInstaller.REGISTRY, PoweredKneesImplant.ID, NMItems.EXTRA_KNEES);
        Registry.register(EntityImplantInstaller.REGISTRY, ExtraMouthImplant.ID, NMItems.EXTRA_MOUTH);
        Registry.register(EntityImplantInstaller.REGISTRY, LungExtensionsImplant.ID, NMItems.LUNG_EXTENSIONS);
        Registry.register(EntityImplantInstaller.REGISTRY, SpiderLegsImplant.ID, NMItems.SPIDER_LEGS);
        Registry.register(EntityImplantInstaller.REGISTRY, new ResourceLocation(NeepMeat.NAMESPACE, "chrysalis"), NMItems.CHRYSALIS);
        Registry.register(EntityImplantInstaller.REGISTRY, new ResourceLocation(NeepMeat.NAMESPACE, "explosive"), EntityImplantInstaller.simple(ExplosiveImplant.ID));
        Registry.register(EntityImplantInstaller.REGISTRY, ClawsImplant.ID, NMItems.CLAWS);
        Registry.register(EntityImplantInstaller.REGISTRY, CatEarsImplant.ID, NMItems.CAT_EARS);
        Registry.register(EntityImplantInstaller.REGISTRY, PipeClimbImplant.ID, NMItems.PIPE_CLIMBER);
        Registry.register(EntityImplantInstaller.REGISTRY, new ResourceLocation(NeepMeat.NAMESPACE, "hatch_mob_egg"), new HatchingImplantInstaller());
        Registry.register(EntityImplantInstaller.REGISTRY, new ResourceLocation(NeepMeat.NAMESPACE, "guide"), EntityImplantInstaller.simple(GuideImplant.ID));

        ImplantConfigHandler.register(SpiderLegsImplant.ID, HexapodImplantConfigHandler::new);
        ImplantConfigHandler.register(PoweredKneesImplant.ID, PoweredKneesConfigHandler::new);
        ImplantConfigHandler.register(CatEarsImplant.ID, CatEarsConfigHandler::new);
        ImplantConfigHandler.register(ExtraMouthImplant.ID, ExtraMouthConfigHandler::new);
    }
}
