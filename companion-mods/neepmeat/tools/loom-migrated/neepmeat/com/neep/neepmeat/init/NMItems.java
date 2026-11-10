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

package com.neep.neepmeat.init;

import com.neep.meatlib.item.BaseCraftingItem;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.meatgun.MeatgunItem;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.tooltip.MeatgunTooltipBuilder;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.OreDustRegistry;
import com.neep.neepmeat.implant.entity.*;
import com.neep.neepmeat.implant.item.ComplexityUpgradeImplant;
import com.neep.neepmeat.implant.item.LivingToolImplant;
import com.neep.neepmeat.implant.item.PneumaticMotorImplant;
import com.neep.neepmeat.item.*;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorMinecart;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.*;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

@SuppressWarnings("unused")
@RegisterMe(NeepMeat.NAMESPACE)
public class NMItems
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static Item SMALL_COMPRESSOR_MINECART = new BaseMinecartItem(new MeatlibItemSettings().stacksTo(3).group(NMItemGroups.GENERAL), SmallCompressorMinecart::new);

    @Path("compound_injector")
    public static Item COMPOUND_INJECTOR = new CompoundInjectorItem(C, new MeatlibItemSettings().group(MeatWeapons.WEAPONS));

    @Path("sacrificial_dagger")
    public static Item TISSUE_OPERATOR = new TissueOperatorItem(C, new MeatlibItemSettings().group(MeatWeapons.WEAPONS).durability(1561));
//    public static Item CHEESE_CLEAVER = new CheeseCleaverItem(C, new MeatlibItemSettings().group(MeatWeapons.WEAPONS));
//    public static Item SLASHER = new SlasherItem(C, new MeatlibItemSettings().group(MeatWeapons.WEAPONS));

    public static Item ENLIGHTENMENT_CHARGE = new EnlightenmentChargeItem(new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item ANIMAL_HEART = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item REANIMATED_HEART = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item COPPER_COIL = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item BIOELECTRIC_ORGAN = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item STATOR = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item FLYWHEEL = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item REFRACTORY_BRICK = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item WHISPER_BRASS_INGOT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item MEAT_STEEL_INGOT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item MEAT_STEEL_SHOVEL = new ShovelItem(NMToolMaterials.EMBOSSED_MEAT_STEEL, 1.5F, -3.0F, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item MEAT_STEEL_PICKAXE = new PickaxeItem(NMToolMaterials.EMBOSSED_MEAT_STEEL, 1, -2.8F, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item MEAT_STEEL_AXE = new AxeItem(NMToolMaterials.EMBOSSED_MEAT_STEEL, 6.0F, -3.1F, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item MEAT_STEEL_HOE = new MeatSteelHoeItem(NMToolMaterials.EMBOSSED_MEAT_STEEL, 1, 0F, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item BLOOD_BUBBLE = new BaseCraftingItem(C, 1, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.BLOOD_BUBBLE));
    public static Item MEAT_STEEL_COMPONENT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item MEAT_STEEL_BALL = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item CONTROL_UNIT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));

    // MEAT
    public static Item MEAT_SCRAP = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.MEAT_SCRAP));
    public static Item RAW_MEAT_BRICK = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.MEAT_BRICK));
    public static Item COOKED_MEAT_BRICK = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.COOKED_MEAT_BRICK));

//    public static Item MACHINE_FLUID = new BaseCraftingItem("machine_fluid", 1, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));

    // Organism Parts
//    public static Item DIGESTIVE_SYSTEM = new BaseCraftingItem("digestive_system", 1, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
//    public static Item INTERFACE_PORTS = new BaseCraftingItem("interface_array", 1, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item INTERNAL_COMPONENTS = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item CONTRACTILE_ACTUATOR = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
//    public static Item GANGLIAL_CENTRE = new BaseCraftingItem("ganglial_cluster", 1, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));

    @Path("brain_rough") public static Item ROUGH_BRAIN = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item ENLIGHTENED_BRAIN = new EnlightenmentFoodItem(C, 2, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS).food(NMFoodComponents.ENLIGHTENED_BRAIN));

    public static Item OPEN_EYE = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
//    public static Item PROCESSOR_BOARD = new BaseCraftingItem("processor_board", 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item DIVINE_ORGAN = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item HUNGRY_HETEROSTRUCTURE = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item CRUSHED_ENDER_PEARL = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));

    @Path("integration_charge_crude")
    public static Item CRUDE_INTEGRATION_CHARGE = new BaseCraftingItem(C, 1, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    @Path("advanced_integration_charge")
    public static Item ADV_INTEGRATION_CHARGE = new AdvancedIntegrationChargeItem(TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    @Path("biomass")
    public static Item ASSORTED_BIOMASS = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));

    public static Item WHISPER_WHEAT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item WHISPER_FLOUR = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS));
    public static Item WHISPER_BREAD = new EnlightenmentFoodItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.WHISPER_BREAD));
//    public static final Item BROME_STRANDS = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item PROJECTOR = new ProjectorItem(new MeatlibItemSettings().group(NMItemGroups.GENERAL).stacksTo(1));

    public static Item PIPETTE = new PipetteItem(TooltipSupplier.hidden(3), new MeatlibItemSettings().group(NMItemGroups.GENERAL).stacksTo(1));

    public static Item PINKDRINK = new PinkdrinkItem(C, 1, new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.PINKDRINK));

//    public static Item CAN = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD));
    public static Item CARTON = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.FOOD));
    public static Item MILK_CARTON = new MilkCartonItem(TooltipSupplier.simple(1), new MeatlibItemSettings().group(NMItemGroups.FOOD));
    public static FoodCartonItem FOOD_CARTON = new FoodCartonItem(TooltipSupplier.blank(), new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.MEAT_CARTON));
    public static Item MEAT_CARTON = new MeatCartonItem(TooltipSupplier.blank(), new MeatlibItemSettings().group(NMItemGroups.FOOD).food(NMFoodComponents.MEAT_CARTON));

    // Mob cloning
    @Path("essential_saltes")
    public static Item ESSENTIAL_SALTES = new EssentialSaltesItem(C, TooltipSupplier.simple(1), new MeatlibItemSettings().group(NMItemGroups.INGREDIENTS).fireResistant());
    public static Item MOB_EGG = new MobEggItem(C, TooltipSupplier.simple(1), new MeatlibItemSettings());

//    public static Item MEAT_STEEL_BOOTS = new MeatSteelArmourItem("meat_steel_boots", ArmorMaterials.DIAMOND, EquipmentSlot.FEET, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
//    public static Item MEAT_STEEL_LEGS = new MeatSteelArmourItem("meat_steel_legs", ArmorMaterials.DIAMOND, EquipmentSlot.LEGS, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
//    public static Item MEAT_STEEL_CHESTPLATE = new MeatSteelArmourItem("meat_steel_chestplate", ArmorMaterials.DIAMOND, EquipmentSlot.CHEST, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    @Path("goggles")
    public static GogglesItem GOGGLES = new GogglesItem(ArmorMaterials.IRON, new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static PlayerImplantItem PINEAL_EYE = new PlayerImplantItem(C, "pineal_eye", 1, PinealEyeImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem EXTRA_KNEES = new PlayerImplantItem(C, "extra_knees", 1, PoweredKneesImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem EXTRA_MOUTH = new PlayerImplantItem(C, "extra_mouth", 1, ExtraMouthImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem LUNG_EXTENSIONS = new PlayerImplantItem(C, "lung_extensions", 1, LungExtensionsImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem SPIDER_LEGS = new PlayerImplantItem(C, "spider_legs", 1, SpiderLegsImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem CLAWS = new PlayerImplantItem(C, "claws", 1, ClawsImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem CAT_EARS = new PlayerImplantItem(C, "cat_ears", 1, CatEarsImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static PlayerImplantItem PIPE_CLIMBER = new PlayerImplantItem(C, "pipe_climber", 1, PipeClimbImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
//    @Path("shield") public static Item SHIELD = new ItemImplantItem(C, "shield", 1, ShieldImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item LIVING_TOOL_IMPLANT = new LivingToolImplantItem(C, "living_tool_implant", 1, LivingToolImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item PNEUMATIC_MOTOR = new PneumaticMotorImplantItem(C, "pneumatic_motor", 1, PneumaticMotorImplant.ID, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item DRILL_COMPLEXITY_UPGRADE = new ComplexityImplantItem(C, "drill_complexity_upgrade", 1, ComplexityUpgradeImplant.ID, i -> i == NMItems.DRILL_CHASSIS, new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static ChrysalisItem CHRYSALIS = new ChrysalisItem(C ,TooltipSupplier.simple(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item TRANSFORMING_TOOL_BASE = new BaseCraftingItem(C, 1, new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item DOSIMETER = new DosimeterItem(C, TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item FARMING_SCUTTER = new FarmingScutterItem(() -> NMEntities.FARMING_SCUTTER, TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item COMPRESSOR_SCUTTER = new CompressorScutterItem(() -> NMEntities.COMPRESSOR_SCUTTER, TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static final Item DRILL_CHASSIS = new DrillChassisItem(MeatgunItem.MAX_COMPLEXITY.append(TooltipSupplier.simple(1)),
            new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static Item MOP = new MopItem(TooltipSupplier.simple(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static Item PRESSURE_WASHER = new PressureWasherItem(TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

//    public static Item ROLLED_NEWSPAPER = new RolledNewspaperItem(TooltipSupplier.blank(), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    // Dust!
    public static DynamicOreDustItem ORE_DUST = new DynamicOreDustItem(TooltipSupplier.BLANK, OreDustRegistry.ORE_DUST, new MeatlibItemSettings());
    public static DynamicOreDustItem CRUSHED_ORE = new DynamicOreDustItem(TooltipSupplier.BLANK, OreDustRegistry.CRUSHED_ORE, new MeatlibItemSettings());
    public static Item ASBESTOS_DUST = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL).fireResistant().food(NMFoodComponents.ASBESTOS_DUST));
    public static Item ASBESTOS_FABRIC = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL).fireResistant());

    public static final Item CRUSHED_BROWNE_ORE = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final Item BROWNE_ORE_DUST = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final Item PACKED_BROWNE = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final Item BROWNE_INGOT = new BaseCraftingItem(C, 0, new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    // Meatgun
    public static final MeatgunModuleItem ROCK_DRILL_HEAD = new MeatgunModuleItem(
            NMMeatgunModules.ROCK_DRILL_HEAD,
            new MeatgunTooltipBuilder().desc().build(),
            new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final MeatgunModuleItem IRON_ROTARY_DRILL_HEAD = new MeatgunModuleItem(
            NMMeatgunModules.IRON_ROTARY_DRILL_HEAD,
            new MeatgunTooltipBuilder().desc().build(),
            new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final MeatgunModuleItem MEAT_STEEL_ROTARY_DRILL_HEAD = new MeatgunModuleItem(
            NMMeatgunModules.MEAT_STEEL_ROTARY_DRILL_HEAD,
            new MeatgunTooltipBuilder().desc().build(),
            new MeatlibItemSettings().group(NMItemGroups.GENERAL));
    public static final MeatgunModuleItem DIAMOND_ROTARY_DRILL_HEAD = new MeatgunModuleItem(
            NMMeatgunModules.DIAMOND_ROTARY_DRILL_HEAD,
            new MeatgunTooltipBuilder().desc().build(),
            new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    @Path("debug")
    public static Item DEBUG_ITEM = new DebugItem(new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static void init()
    {
        registerCompostable(NMBlocks.WHISPER_WHEAT.getBaseSeedId(), 0.5f);
        registerCompostable(NMBlocks.FLESH_POTATO.getBaseSeedId(), 0.85f);
        registerCompostable(NMBlocks.GRAVE_BROME.getBaseSeedId(), 0.5f);
        registerCompostable(WHISPER_WHEAT, 0.5f);
        registerCompostable(WHISPER_FLOUR, 0.5f);
        registerCompostable(WHISPER_BREAD, 0.85f);
        registerCompostable(MOB_EGG, 1f);
        registerCompostable(ANIMAL_HEART, 0.85f);
        registerCompostable(REANIMATED_HEART, 0.85f);
        registerCompostable(MEAT_SCRAP, 0.5f);
        registerCompostable(RAW_MEAT_BRICK, 1f);
        registerCompostable(COOKED_MEAT_BRICK, 1f);
        registerCompostable(ROUGH_BRAIN, 0.85f);
        registerCompostable(ENLIGHTENED_BRAIN, 0.85f);
        registerCompostable(ASSORTED_BIOMASS, 1f);

        FuelRegistry.INSTANCE.add(NMItems.PACKED_BROWNE, 3200);
        FuelRegistry.INSTANCE.add(NMItems.CRUSHED_BROWNE_ORE, 800);
        FuelRegistry.INSTANCE.add(NMItems.BROWNE_ORE_DUST, 1600);
    }

    private static void registerCompostable(ItemLike itemConvertible, float chance)
    {
        ComposterBlock.COMPOSTABLES.put(itemConvertible.asItem(), chance);
    }
}
