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

import com.neep.meatlib.block.*;
import com.neep.meatlib.block.multi.TallBlock;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.BlockRegistry;
import com.neep.meatlib.registry.ItemRegistry;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.NMSoundGroups;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.block.*;
import com.neep.neepmeat.block.entity.BaseDoorBlock;
import com.neep.neepmeat.block.entity.CandleCronenCakeBlock;
import com.neep.neepmeat.block.entity.CronenCakeBlock;
import com.neep.neepmeat.block.redstone.BigLeverBlock;
import com.neep.neepmeat.block.sapling.BloodBubbleTreeGenerator;
import com.neep.neepmeat.block.vat.VatWindowBlock;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorBlock;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorStructure;
import com.neep.neepmeat.machine.advanced_motor.AdvancedMotorBlock;
import com.neep.neepmeat.machine.alloy_kiln.AlloyKilnBlock;
import com.neep.neepmeat.machine.bottler.BottlerBlock;
import com.neep.neepmeat.machine.breaker.LinearOscillatorBlock;
import com.neep.neepmeat.machine.casting_basin.CastingBasinBlock;
import com.neep.neepmeat.machine.charnel_compactor.CharnelCompactorBlock;
import com.neep.neepmeat.machine.charnel_pump.*;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlock;
import com.neep.neepmeat.machine.crucible.CrucibleBlock;
import com.neep.neepmeat.machine.crucible.FatCollectorBlock;
import com.neep.neepmeat.machine.death_blades.DeathBladesBlock;
import com.neep.neepmeat.machine.deployer.DeployerBlock;
import com.neep.neepmeat.machine.fabricator.FabricatorBlock;
import com.neep.neepmeat.machine.fluid_exciter.FluidExciterBlock;
import com.neep.neepmeat.machine.grinder.CrusherBlock;
import com.neep.neepmeat.machine.heater.HeaterBlock;
import com.neep.neepmeat.machine.homogeniser.HomogeniserBlock;
import com.neep.neepmeat.machine.hydraulic_press.HydraulicPressBlock;
import com.neep.neepmeat.machine.integrator.IntegratorBlock;
import com.neep.neepmeat.machine.item_mincer.ItemMincerBlock;
import com.neep.neepmeat.machine.large_motor.LargeMotorBlock;
import com.neep.neepmeat.machine.living_rock.VerrucousStoneBlock;
import com.neep.neepmeat.machine.living_rock.VerrucousStonePortBlock;
import com.neep.neepmeat.machine.mincer.MincerBlock;
import com.neep.neepmeat.machine.mixer.MixerBlock;
import com.neep.neepmeat.machine.motor.MotorBlock;
import com.neep.neepmeat.machine.pedestal.PedestalBlock;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlock;
import com.neep.neepmeat.machine.power_flower.PowerFlowerControllerBlock;
import com.neep.neepmeat.machine.power_flower.PowerFlowerFluidPortBlock;
import com.neep.neepmeat.machine.power_flower.PowerFlowerGrowthBlock;
import com.neep.neepmeat.machine.power_flower.PowerFlowerSeedsBlock;
import com.neep.neepmeat.machine.pylon.PylonBlock;
import com.neep.neepmeat.machine.separator.SeparatorBlock;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorBlock;
import com.neep.neepmeat.machine.small_trommel.SmallTrommelBlock;
import com.neep.neepmeat.machine.solidity_detector.SolidityDetectorBlock;
import com.neep.neepmeat.machine.stirling_engine.StirlingEngineBlock;
import com.neep.neepmeat.machine.surgery_platform.SurgeryPlatformBlock;
import com.neep.neepmeat.machine.synthesiser.SynthesiserBlock;
import com.neep.neepmeat.machine.transducer.TransducerBlock;
import com.neep.neepmeat.machine.trough.TroughBlock;
import com.neep.neepmeat.machine.upgrade_manager.ImplantManagerBlock;
import com.neep.neepmeat.machine.well_head.WellHeadBlock;
import com.neep.neepmeat.transport.block.energy_transport.*;
import com.neep.neepmeat.transport.block.item_transport.DisplayPlateBlock;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.checkerframework.checker.units.qual.C;

import static net.minecraft.tags.BlockTags.*;

@SuppressWarnings("unused")
@RegisterMe(value = NeepMeat.NAMESPACE)
public class NMBlocks
{
    public static final RegistrationContext C = NeepMeat.C;

    // --- Building Blocks ---
    public static final BlockBehaviour.Properties METAL = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().destroyTime(3.0f).sound(SoundType.NETHERITE_BLOCK);
    public static final BlockBehaviour.Properties BRASS_BLOCKS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(1.8f).sound(SoundType.NETHERITE_BLOCK);
    public static final BlockBehaviour.Properties FLUID_PIPE_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(0.8f).sound(NMSoundGroups.METAL);
    public static final BlockBehaviour.Properties VASCULAR_CONDUIT_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(0.8f).sound(NMSoundGroups.VASCULAR_CONDUIT);
    public static final BlockBehaviour.Properties MACHINE_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(2f).sound(NMSoundGroups.MECHANICAL_MACHINE).noOcclusion().isRedstoneConductor(InventoryDetectorBlock::never);
    public static final BlockBehaviour.Properties OPAQUE_MACHINE_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(2f).sound(NMSoundGroups.MECHANICAL_MACHINE);
//    public static final AbstractBlock.Settings VAT_SETTINGS = MeatlibBlockSettings.of(Material.METAL).strength(2.5f).sounds(NMSoundGroups.MECHANICAL_MACHINE);
    public static final BlockBehaviour.Properties FLUID_MACHINE_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(2f).sound(NMSoundGroups.MECHANICAL_MACHINE);
    public static final BlockBehaviour.Properties ITEM_PIPE_SETTINGS = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(1f).sound(SoundType.METAL);

//    public static Block DUAT_STONE = new BaseBuildingBlock("duat_stone", true, MeatlibBlockSettings.of(Material.STONE).strength(2.5f).sounds(BlockSoundGroup.STONE));
//    public static Block DUAT_COBBLESTONE = new BaseBuildingBlock("duat_cobblestone", true, MeatlibBlockSettings.of(Material.STONE).strength(2.5f).sounds(BlockSoundGroup.STONE));
//    public static Block DUAT_GRAVEL = new BaseBlock("duat_gravel", block(), MeatlibBlockSettings.of(Material.STONE).strength(1.0f).sounds(BlockSoundGroup.GRAVEL)));

//    public static Block OBJ_TEST = new BigBlock("obj_test", MeatlibBlockSettings.copyOf(Blocks.STONE)));

//    public static Block TEST_MULTIBLOCK = new TestMultiblock("test_multiblock", MeatlibBlockSettings.create()));

    public static PaintedBlockCollection<?> SMOOTH_TILE = new PaintedBlockCollection<>(C, "smooth_tile", SmoothTileBlock::new, MeatlibBlockSettings.of().destroyTime(3.0f));

    @Path("polished_metal_bricks") public static Block POLISHED_IRON_BRICKS = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
    @Path("polished_metal_small_bricks") public static Block POLISHED_MERAL_SMALL_BRICKS = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
    @Path("polished_metal") public static BaseBuildingBlock POLISHED_METAL = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
    @Path("blue_polished_metal") public static Block BLUE_IRON_BLOCK = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
    @Path("rusty_metal") public static BaseBuildingBlock RUSTY_METAL_BLOCK = new RustyMetalBlock(C, true, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
    @Path("rusty_metal_sheet") public static BaseBuildingBlock RUSTY_METAL_SHEET = new RustyMetalSheetBlock(C, true, MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    public static Block RUSTY_METAL_DOOR = new BaseDoorBlock(C, MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK).noOcclusion(), block(), NMBlockSets.RUSTY_METAL);
    public static Block RUSTY_METAL_TRAPDOOR = new BaseTrapdoorBlock(C, block(), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK).addTags(TRAPDOORS).simpleDrop(ItemRegistry::getMatchingItem), BlockSetType.IRON);
    public static Block RUSTY_METAL_SHEET_TRAPDOOR = new BaseTrapdoorBlock(C, block(), MeatlibBlockSettings.copyOf(RUSTY_METAL_SHEET).addTags(TRAPDOORS).simpleDrop(ItemRegistry::getMatchingItem), BlockSetType.IRON);
    public static Block RUSTY_METAL_LIGHT = new BaseLightBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK).lightLevel(Blocks.litBlockEmission(15)));
    public static Block WHITE_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.WHITE, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block GREY_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.GRAY, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block YELLOW_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.YELLOW, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block RED_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.RED, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block BLUE_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.BLUE, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block GREEN_ROUGH_CONCRETE = new RoughConcreteBlock(C, true, DyeColor.GREEN, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block YELLOW_TILES = new BaseBuildingBlock(C, false, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block CAUTION_BLOCK = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block CAUTION_BLOCK_DOOR = new BaseDoorBlock(C, MeatlibBlockSettings.copyOf(CAUTION_BLOCK).noOcclusion(), block(), NMBlockSets.RUSTY_METAL);
    public static Block DIRTY_RED_TILES = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block DIRTY_WHITE_TILES = new BaseBuildingBlock(C, true, MeatlibBlockSettings.of().strength(1.8f).sound(SoundType.STONE));
    public static Block SANDY_BRICKS = new BaseBuildingBlock(C, true, MeatlibBlockSettings.copyOf(Blocks.BRICKS));
    public static Block MEAT_STEEL_BLOCK = new BaseBuildingBlock(C, true, MeatlibBlockSettings.copyOf(Blocks.NETHERITE_BLOCK));
    public static Block ASBESTOS = new RawAsbestosBlock(C, true, MeatlibBlockSettings.copyOf(Blocks.STONE));
    public static BaseBuildingBlock ASBESTOS_TILE = new BaseBuildingBlock(C, true, MeatlibBlockSettings.copyOf(ASBESTOS));
    public static Block CORRUGATED_ASBESTOS = new CorrugatedAsbestosBlock(C, true, MeatlibBlockSettings.copyOf(ASBESTOS));
    public static PaintedBlockCollection<?> PAINTED_CORRUGATED_ASBESTOS = new PaintedBlockCollection<>(C, "painted_corrugated_asbestos", PaintedCorrugatedAsbestosBlock::new, MeatlibBlockSettings.copyOf(CORRUGATED_ASBESTOS));
    public static Block ASBESTOS_SHINGLE = new BaseBuildingBlock(C, false, MeatlibBlockSettings.copyOf(ASBESTOS));

    public static Block BLOODY_BRICKS = new BaseBuildingBlock(C, true, MeatlibBlockSettings.copyOf(Blocks.BRICKS));
    public static Block BLOODY_TILES = new BaseBuildingBlock(C, false, MeatlibBlockSettings.copyOf(BLOODY_BRICKS));

    public static Block REINFORCED_GLASS = new BaseBuildingBlock(C, false, (MeatlibBlockSettings) MeatlibBlockSettings.of().strength(1f).sound(SoundType.GLASS).noOcclusion().isValidSpawn(VatWindowBlock::never).isRedstoneConductor(VatWindowBlock::never).isSuffocating(VatWindowBlock::never).isViewBlocking(VatWindowBlock::never))
    {
        @Override
        public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction)
{
            if (stateFrom.is(this) || stateFrom.is((Block) stairs) && stateFrom.getValue(StairBlock.FACING).equals(direction.getOpposite()))
            {
                return true;
            }
            return super.skipRendering(state, stateFrom, direction);
        }

        @Override
        public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return Shapes.empty();
        }

        @Override
        public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
        {
            return 1.0f;
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos)
        {
            return true;
        }
    };


//    public static Block FILLED_SCAFFOLD = new BaseBuildingBlock("filled_scaffold", 64, false, MeatlibBlockSettings.of(Material.METAL).strength(5.0f).sounds(NMSoundGroups.MECHANICAL_MACHINE));

    @Path("rusted_metal_scaffold") public static MetalScaffoldingBlock SCAFFOLD_PLATFORM = new MetalScaffoldingBlock(C, block(), MeatlibBlockSettings.of().strength(1.5f).sound(NMSoundGroups.METAL));
    @Path("blue_metal_scaffold") public static MetalScaffoldingBlock BLUE_SCAFFOLD = new MetalScaffoldingBlock(C, block(), MeatlibBlockSettings.of().strength(1.5f).sound(NMSoundGroups.METAL));
    @Path("yellow_metal_scaffold") public static MetalScaffoldingBlock YELLOW_SCAFFOLD = new MetalScaffoldingBlock(C, block(), MeatlibBlockSettings.of().strength(1.5f).sound(NMSoundGroups.METAL));

    @Path("rusty_column") public static Block RUSTY_VENT = new BaseColumnBlock(C, block(), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    public static Block MESH_PANE = new BasePaneBlock(C, block(), MeatlibBlockSettings.of().strength(3.5f).sound(SoundType.LANTERN));
    public static Block RUSTED_BARS = new BasePaneBlock(C, block(), MeatlibBlockSettings.of().strength(3.5f).sound(NMSoundGroups.METAL));
    public static Block RUSTY_PANEL = new BaseBlock(C, MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    @Path("rusty_vent") public static Block RUSTY_GRATE = new BaseBlock(C, MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));

    // Decorations
    public static Block DIRTY_SINK = new BaseBlock(C, block(), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK).forceSolidOn().noOcclusion());
    public static Block SMALL_SINK = new SinkBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE).noOcclusion());
    public static Block TELEVISION_OFF = new TelevisionBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE).noOcclusion());
    public static Block TELEVISION_STATIC = new TelevisionBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE).noOcclusion());
    @Path("large_fan") public static BigBlock<?> LARGE_FAN = new LargeFanBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK).noOcclusion());
    public static final Block CEILING_LIGHT = new CeilingLightBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.STONE).lightLevel(Blocks.litBlockEmission(15)).destroyTime(0.5f).noOcclusion());

//    public static Block SLOPE_TEST = new BaseStairsBlock(CAUTION_BLOCK.getDefaultState(), "slope_test", 64, MeatlibBlockSettings.of(Material.METAL).nonOpaque()));

    @Path("rusted_metal_scaffold_trapdoor") public static Block SCAFFOLD_TRAPDOOR = new ScaffoldTrapdoorBlock(C, block(), MeatlibBlockSettings.of().strength(2.0f).sound(NMSoundGroups.METAL).noOcclusion());

    public static Block RUSTY_METAL_LADDER = new MetalLadderBlock(C, block(), MeatlibBlockSettings.of().strength(2.0f).sound(NMSoundGroups.METAL).noOcclusion());
    public static Block RUSTY_METAL_RUNGS = new MetalRungsBlock(C, block(), MeatlibBlockSettings.of().strength(2.0f).sound(NMSoundGroups.METAL).noOcclusion());

//    public static Block CAUTION_TAPE = new CautionTapeBlock("caution_tape", 64, false, MeatlibBlockSettings.of(Material.CARPET).strength(1.0f).sounds(BlockSoundGroup.STONE).nonOpaque()));


    // --- Machines
//    public static Block TROMMEL = new TrommelBlock("trommel", block(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));
//    public static Block TROMMEL_STRUCTURE = new TrommelBlock.Structure("trommel_top", MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));
    public static SmallTrommelBlock SMALL_TROMMEL = new SmallTrommelBlock(C, block().requiresMotor().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static Block SMALL_TROMMEL_STRUCTURE = new SmallTrommelBlock.Structure(MeatlibBlockSettings.copyOf(SMALL_TROMMEL), SMALL_TROMMEL);

//    public static Block CENTRIFUGE = new CentrifugeBlock("centrifuge", MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));

    public static Block HEATER = new HeaterBlock(C, block().requiresVascular().requiresIchor().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("breaker") public static Block LINEAR_OSCILLATOR = new LinearOscillatorBlock(C, block().requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("motor_unit") public static Block MOTOR = new MotorBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static Block ADVANCED_MOTOR = new AdvancedMotorBlock(C, block().tooltip(TooltipSupplier.hidden(2)).requiresVascular(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("large_motor") public static LargeMotorBlock LARGE_MOTOR = new LargeMotorBlock(C, block().requiresVascular().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
//    @Path("large_flywheel") public static Block FLYWHEEL = new FlywheelBlock(C, block().tooltip(TooltipSupplier.simple(0)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("stirling_engine") public static Block STIRLING_ENGINE = new StirlingEngineBlock(C, block(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("deployer") public static Block DEPLOYER = new DeployerBlock(C, block().requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
//    public static Block AGITATOR = new AgitatorBlock("agitator", block().requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));
    @Path("grinder") public static Block CRUSHER = new CrusherBlock(C, block().requiresMotor().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("alloy_kiln") public static Block ALLOY_KILN = new AlloyKilnBlock(C, block(), MeatlibBlockSettings.copyOf(NMBlocks.SANDY_BRICKS));
    @Path("crucible") public static Block CRUCIBLE = new CrucibleBlock(C, block(), FLUID_PIPE_SETTINGS);
    @Path("collector") public static Block COLLECTOR = new FatCollectorBlock(C, block(), FLUID_PIPE_SETTINGS);
//    @Path("assembler") public static AssemblerBlock ASSEMBLER = new AssemblerBlock(C, block().tooltip(TooltipSupplier.hidden(1)).requiresVascular(), MACHINE_SETTINGS);
    @Path("fabricator") public static Block FABRICATOR = new FabricatorBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MACHINE_SETTINGS);

    @Path("fluid_exciter") public static TallBlock FLUID_EXCITER = new FluidExciterBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.of().sound(NMSoundGroups.MECHANICAL_MACHINE).destroyTime(4.0f));

    @Path("transducer") public static Block TRANSDUCER = new TransducerBlock(C, block(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS).sound(NMSoundGroups.TRANSDUCER));
    public static final MeatlibBlockSettings POWER_FLOWER_SETTINGS = (MeatlibBlockSettings) MeatlibBlockSettings.create(MINEABLE_WITH_AXE).sound(SoundType.MUDDY_MANGROVE_ROOTS).strength(2.0f);
    @Path("power_flower_seeds") public static Block POWER_FLOWER_SEEDS = new PowerFlowerSeedsBlock(C, block().tooltip(TooltipSupplier.simple(1)), POWER_FLOWER_SETTINGS.copy().tags(MINEABLE_WITH_HOE).destroyTime(0.01f));
    @Path("power_flower_growth") public static PowerFlowerGrowthBlock POWER_FLOWER_GROWTH = new PowerFlowerGrowthBlock(C, block().tooltip(TooltipSupplier.hidden(1)), POWER_FLOWER_SETTINGS.copy());
    @Path("power_flower_controller") public static Block POWER_FLOWER_CONTROLLER = new PowerFlowerControllerBlock(C, block().tooltip(TooltipSupplier.hidden(1)), POWER_FLOWER_SETTINGS.copy());
    @Path("power_flower_fluid_port") public static Block POWER_FLOWER_FLUID_PORT = new PowerFlowerFluidPortBlock(C, block().tooltip(TooltipSupplier.simple(1)), POWER_FLOWER_SETTINGS.copy());

    public static Block VERRUCOUS_STONE = new VerrucousStoneBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE).noLootTable());
    public static Block VERRUCOUS_STONE_PORT = new VerrucousStonePortBlock(C, block(), MeatlibBlockSettings.copyOf(VERRUCOUS_STONE).noLootTable());

    @Path("pedestal") public static Block PEDESTAL = new PedestalBlock(C, block().plc(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    @Path("charnel_compactor") public static Block CHARNEL_COMPACTOR = new CharnelCompactorBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    @Path("charnel_pump") public static CharnelPumpBlock CHARNEL_PUMP = new CharnelPumpBlock(C, block(), MeatlibBlockSettings.copy(RUSTY_METAL_BLOCK));
    @Path("well_head") public static WellHeadBlock WELL_HEAD = new WellHeadBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    @Path("contaminated_dirt") public static Block CONTAMINATED_DIRT = new ContaminatedRubbleBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.DIRT).tags(MINEABLE_WITH_SHOVEL).drops(ResourceLocation.tryBuild("minecraft", "dirt")).sound(SoundType.MUDDY_MANGROVE_ROOTS), 3);
    public static ContaminatedGrassBlock CONTAMINATED_GRASS_BLOCK = new ContaminatedGrassBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.GRASS_BLOCK).tags(MINEABLE_WITH_SHOVEL).dropsLike(Blocks.GRASS_BLOCK).sound(SoundType.MUDDY_MANGROVE_ROOTS));
    public static Block CONTAMINATED_RUBBLE = new ContaminatedRubbleBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.DIRT).tags(MINEABLE_WITH_SHOVEL).simpleDrop(Block::asItem).sound(SoundType.MUDDY_MANGROVE_ROOTS), 4);
    @Path("writhing_earth_spout") public static Block WRITHING_EARTH_SPOUT = new WrithingEarthSpoutBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.DIRT).tags(MINEABLE_WITH_SHOVEL).strength(8).sound(SoundType.MUDDY_MANGROVE_ROOTS));
    @Path("writhing_stone") public static Block WRITHING_STONE = new WrithingStoneBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE).tags(MINEABLE_WITH_PICKAXE).strength(4).sound(SoundType.MUDDY_MANGROVE_ROOTS));

    @Path("phage_ray") public static PhageRayBlock PHAGE_RAY = new PhageRayBlock(C, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    @Path("mixer") public static MixerBlock MIXER = new MixerBlock(C, block().tooltip(TooltipSupplier.hidden(4)).requiresMotor(), MeatlibBlockSettings.copyOf(BRASS_BLOCKS));

    @Path("casting_basin") public static Block CASTING_BASIN = new CastingBasinBlock(C, block(), MeatlibBlockSettings.copyOf(MIXER));
    @Path("hydraulic_press") public static TallBlock HYDRAULIC_PRESS = new HydraulicPressBlock(C, block(), MeatlibBlockSettings.copyOf(MIXER));

//    @Path("workstation") public static Block WORKSTATION = new WorkstationBlock(C, ItemSettings.block().tooltip(TooltipSupplier.hidden(2)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static Block DEATH_BLADES = new DeathBladesBlock(C, block().tooltip(TooltipSupplier.simple(1)).requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static final Block BROWNE_ORE = new BrowneOreBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.IRON_ORE));
    public static final Block DEEPSLATE_BROWNE_ORE = new BrowneOreBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block BROWNE_BLOCK = new BaseBuildingBlock(C, true, MeatlibBlockSettings.copyOf(Blocks.IRON_BLOCK));
    public static final ShortBromeBlock GRAVE_BROME = new ShortBromeBlock(C, MeatlibBlockSettings.copyOf(Blocks.GRASS), block());
//    public static final Block LONG_BROME = C.withItem(new LongBromeBlock(MeatlibBlockSettings.copyOf(Blocks.GRASS)), block());

    public static Block FEEDING_TROUGH = new TroughBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(FLUID_MACHINE_SETTINGS));
    @Path("separator") public static Block SEPARATOR = new SeparatorBlock(C, block().tooltip(TooltipSupplier.hidden(2)).requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

//    public static Block SIFTER = new SifterBlock("sifter", block(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));
//    public static Block SIFTER_HOPPER = new SifterHopperBlock("hopper", 64, true, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)));

    @Path("pylon") public static Block PYLON = new PylonBlock(C, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("synthesiser") public static Block SYNTHESISER = new SynthesiserBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("mincer") public static Block MINCER = new MincerBlock(C, block().requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("item_mincer") public static Block ITEM_MINCER = new ItemMincerBlock(C, block().tooltip(TooltipSupplier.hidden(1)).requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    @Path("homogeniser") public static Block HOMOGENISER = new HomogeniserBlock(C, block().requiresMotor(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static Block SURGERY_PLATFORM = new SurgeryPlatformBlock(C, block().plc(), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static Block UPGRADE_MANAGER = new ImplantManagerBlock(C, block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

//    public static Block VAT_CASING = new VatCasingBlock("vat_casing", block().tooltip(TooltipSupplier.simple(1)), VAT_SETTINGS));
//    public static Block VAT_CONTROLLER = new VatControllerBlock("vat_controller", block(), VAT_SETTINGS));
//    public static Block VAT_ITEM_PORT = new ItemPortBlock("vat_item_port", block(), VAT_SETTINGS));
//    public static Block VAT_FLUID_PORT = new FluidPortBlock("vat_fluid_port", block(), VAT_SETTINGS));
//    public static Block VAT_WINDOW = new VatWindowBlock("clear_tank_wall", block(), AbstractBlock.Settings.of(Material.GLASS).strength(0.3f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(VatWindowBlock::never).solidBlock(VatWindowBlock::never).suffocates(VatWindowBlock::never).blockVision(VatWindowBlock::never)));

    public static Block FLAME_JET = new FlameJetBlock(C, block().tooltip(TooltipSupplier.hidden(1)).factory(FluidComponentItem::new), MACHINE_SETTINGS);

    public static Block ITEM_BUFFER = new DisplayPlateBlock(C, block().tooltip(TooltipSupplier.simple(1)).plc(), MACHINE_SETTINGS.sound(SoundType.METAL));
    //    public static Block SPIGOT = new SpigotBlock("spigot", 64, false, FLUID_MACHINE_SETTINGS));

    public static Block BOTTLER = new BottlerBlock(C, block().tooltip(TooltipSupplier.simple(1)).requiresMotor(), FLUID_MACHINE_SETTINGS);

    public static Block SMALL_COMPRESSOR = new SmallCompressorBlock(C, block().retainsInv().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(FLUID_MACHINE_SETTINGS).noOcclusion());

    // --- Item Transfer ---
    @Path("chute") public static Block CHUTE = new ChuteBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.HOPPER));
    public static Block METAL_BARREL = new MetalBarrelBlock(C, MeatlibBlockSettings.copyOf(RUSTY_METAL_BLOCK));
    @Path("content_detector") public static Block CONTENT_DETECTOR = new InventoryDetectorBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.OBSERVER));
    @Path("solidity_detector") public static Block SOLIDITY_DETECTOR = new SolidityDetectorBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.OBSERVER));

    // --- Data ---
    public static Block DATA_CABLE = new DataCableBlock(C, block().neepbus().tooltip(TooltipSupplier.hidden(1)), VASCULAR_CONDUIT_SETTINGS);

    // --- Vascular System ---
    public static Block ENCASED_DATA_CABLE = new EncasedDataCableBlock(C, block(), VASCULAR_CONDUIT_SETTINGS);
    public static Block VASCULAR_CONDUIT = new VascularConduitBlock(C, block().tooltip(TooltipSupplier.hidden(1)), VASCULAR_CONDUIT_SETTINGS);
    public static Block VASCULAR_CONDENSER = new VascularCondenserBlock(C, block().tooltip(TooltipSupplier.hidden(1)), VASCULAR_CONDUIT_SETTINGS);
    public static Block VASCULAR_SENSOR = new VascularSensorBlock(C, block().neepbus().tooltip(TooltipSupplier.hidden(1)), VASCULAR_CONDUIT_SETTINGS);
    public static Block ENCASED_VASCULAR_CONDUIT = new EncasedVascularConduitBlock(C, block().tooltip(TooltipSupplier.simple(1)), VASCULAR_CONDUIT_SETTINGS);
    public static Block POWER_EMITTER = new PowerEmitterBlock(C, block().tooltip(TooltipSupplier.simple(1)), VASCULAR_CONDUIT_SETTINGS);
    public static Block VSC = new VSCBlock(C, block().neepbus().tooltip(TooltipSupplier.hidden(1)), VASCULAR_CONDUIT_SETTINGS);

    // --- Crops ---
    public static BaseCropBlock WHISPER_WHEAT = new BaseCropBlock(C, 64, 0, MeatlibBlockSettings.copyOf(Blocks.WHEAT).tags());
    @Path("flesh_potato") public static BaseCropBlock FLESH_POTATO = new BaseCropBlock(C, "", 64, 2, MeatlibBlockSettings.copyOf(Blocks.POTATOES).tags());
//    public static Block ROCKWART = new BaseCropBlock("rockwart", 64, true, AbstractBlock.Settings.copy(Blocks.WHEAT)));
    @Path("blood_bubble_sapling") public static Block BLOOD_BUBBLE_SAPLING = new BaseSaplingBlock(C, new BloodBubbleTreeGenerator(), block(), MeatlibBlockSettings.copyOf(Blocks.WARPED_FUNGUS).tags().randomTicks());

//    public static final AbstractBlock.Settings BB_SETTINGS = MeatlibBlockSettings.create().strength(1.1f).sounds(BlockSoundGroup.METAL);
    @Path("blood_bubble_log") public static Block BLOOD_BUBBLE_LOG = BlockRegistry.createLogBlock(C, TooltipSupplier.blank());
    @Path("blood_bubble_wood") public static Block BLOOD_BUBBLE_WOOD = BlockRegistry.createLogBlock(C, TooltipSupplier.blank());
    @Path("blood_bubble_leaves") public static Block BLOOD_BUBBLE_LEAVES = new BloodBubbleLeavesBlock(C, MeatlibBlockSettings.copyOf(Blocks.AZALEA_LEAVES).tags(FabricMineableTags.SHEARS_MINEABLE, LEAVES).sound(SoundType.AZALEA_LEAVES));
    @Path("blood_bubble_leaves_flowering") public static Block BLOOD_BUBBLE_LEAVES_FLOWERING = new BloodBubbleLeavesBlock.FruitingBloodBubbleLeavesBlock(C, MeatlibBlockSettings.copyOf(BLOOD_BUBBLE_LEAVES).sound(SoundType.SLIME_BLOCK));
    @Path("blood_bubble_planks") public static Block BLOOD_BUBBLE_PLANKS = new BaseBuildingBlock(C, true, MeatlibBlockSettings.create(MINEABLE_WITH_AXE, PLANKS).strength(2.0f).sound(SoundType.WOOD));
    @Path("blood_bubble_planks_trapdoor") public static Block BLOOD_BUBBLE_TRAPDOOR = C.withItem(new TrapDoorBlock(MeatlibBlockSettings.create(MINEABLE_WITH_AXE, TRAPDOORS).simpleDrop(ItemRegistry::getMatchingItem).strength(2.0f).sound(SoundType.WOOD), BlockSetType.WARPED), block());
    @Path("blood_bubble_planks_door") public static Block BLOOD_BUBBLE_DOOR = new BaseDoorBlock(C, MeatlibBlockSettings.create(MINEABLE_WITH_AXE, DOORS).simpleDrop(ItemRegistry::getMatchingItem).strength(2.0f).sound(SoundType.WOOD).noOcclusion(), block(), BlockSetType.WARPED);
    @Path("blood_bubble_planks_button") public static Block BLOOD_BUBBLE_BUTTON = C.withItem(new ButtonBlock(MeatlibBlockSettings.create(MINEABLE_WITH_AXE, BUTTONS).simpleDrop(ItemRegistry::getMatchingItem).strength(1.0f).sound(SoundType.WOOD), BlockSetType.WARPED, 20, true), block());
    @Path("blood_bubble_planks_fence_gate") public static Block BLOOD_BUBBLE_FENCE_GATE = C.withItem(new FenceGateBlock(MeatlibBlockSettings.create(MINEABLE_WITH_AXE, FENCE_GATES).simpleDrop(ItemRegistry::getMatchingItem).strength(2.0f).sound(SoundType.WOOD).forceSolidOn(), WoodType.WARPED), block());
    @Path("blood_bubble_planks_pressure_plate") public static Block BLOOD_BUBBLE_PRESSURE_PLATE = C.withItem(new BloodBubblePressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, MeatlibBlockSettings.create(MINEABLE_WITH_AXE).simpleDrop(ItemRegistry::getMatchingItem).forceSolidOn().strength(1.0f).sound(SoundType.WOOD)), block().tooltip(TooltipSupplier.simple(1)));

    // --- Assembly ---
//    public static Block LINEAR_RAIL = new LinearRailBlock("linear_rail", 64, false, MeatlibBlockSettings.of(Material.METAL).strength(4.0f)));

    public static Block BIG_LEVER = new BigLeverBlock(C, MeatlibBlockSettings.of().strength(4.0f));

    // --- Integrator ---
    @Path("integrator_egg") public static Block INTEGRATOR_EGG = new IntegratorBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.of().strength(2.0f).sound(SoundType.SLIME_BLOCK));

//    public static Block ADVANCED_INTEGRATOR_EGG = new AdvancedIntegratorEggBlock("advanced_integrator_egg", block(), MeatlibBlockSettings.copyOf(INTEGRATOR_EGG)));
    @Path("advanced_integrator") public static BigBlock<AdvancedIntegratorStructure> ADVANCED_INTEGRATOR = new AdvancedIntegratorBlock(C, MeatlibBlockSettings.of().strength(3.0f).sound(NMSoundGroups.METAL));
//    public static BigBlockStructure ADVANCED_INTEGRATOR_STRUCTURE = ADVANCED_INTEGRATOR.getStructure();
//    public static BigBlockStructure ADVANCED_INTEGRATOR_STRUCTURE = new BigBlockStructure(ADVANCED_INTEGRATOR, "advanced_integrator_structure", MeatlibBlockSettings.of(Material.METAL).strength(3.0f).sounds(NMSoundGroups.METAL)));

    @Path("statue") public static Block STATUE = new StatueBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.PRISMARINE));

    public static Block DIRTY_TOILET = new ToiletBlock(C, block(), MeatlibBlockSettings.copyOf(Blocks.STONE));

    public static Block CRONENCAKE = new CronenCakeBlock(C, MeatlibBlockSettings.copyOf(Blocks.CAKE).tags().sound(SoundType.SLIME_BLOCK));
    public static Block CANDLE_CRONENCAKE = new CandleCronenCakeBlock(C, MeatlibBlockSettings.copyOf(Blocks.CANDLE_CAKE).tags().sound(SoundType.SLIME_BLOCK));

//    public static Block ROUTE_TEST = new RouteTestBlock("routing_test", MeatlibBlockSettings.of(Material.METAL)));

    public static Block HOLDING_TRACK = new HoldingTrackBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.RAIL));
    public static Block DUMPING_TRACK = new DumpingTrackBlock(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.RAIL));
    public static Block CONTROL_TRACK = new PlayerControlTrack(C, block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.RAIL));

    public static final Block ABOMINATION = new BaseHorFacingBlock(C, block().factory((block, ctx, settings) -> null), MeatlibBlockSettings.copyOf(Blocks.STONE).noOcclusion());

    public static boolean never(BlockState state, BlockGetter world, BlockPos pos)
    {
        return false;
    }

    private static boolean never(BlockState blockState, BlockGetter blockView, BlockPos blockPos, EntityType<?> entityType)
    {
        return false;
    }

    public static NMItemSettings block()
    {
        return NMItemSettings.block();
    }
}

