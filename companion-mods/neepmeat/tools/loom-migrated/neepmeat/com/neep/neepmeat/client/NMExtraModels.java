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

package com.neep.neepmeat.client;

import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.meatgun.RotaryDrillHeadModel;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class NMExtraModels implements ModelLoadingPlugin
{
    public static final PartialModel P_CRUSHER_JAW = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/grinder/jaw"));
    public static final PartialModel LARGE_CRUSHER_JAW = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/large_crusher/large_crusher_jaw"));
    public static final PartialModel LARGE_CRUSHER_SHEATH = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/large_crusher/large_crusher_sheath"));
    public static final PartialModel CRUSHER_SEGMENT_SHEATH = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/crusher_segment/sheath"));
    public static final PartialModel LARGE_TROMMEL_MESH = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/large_trommel_mesh"));
    public static final PartialModel DISRUPTOR_SEGMENT_CENTRE = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/reactor/disruptor_segment_centre"));
    public static final PartialModel DISRUPTOR_SEGMENT_ARM = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/reactor/disruptor_segment_arm"));
    public static final PartialModel BALL_MILL_DRUM = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/ball_mill/ball_mill_drum"));
    public static final PartialModel BALL_MILL_DRUM_END = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/ball_mill/ball_mill_drum_end"));
    public static final PartialModel P_PYLON_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/pylon/rotor"));
    public static final PartialModel P_PYLON_ACTIVE_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/pylon/rotor_active"));
    public static final PartialModel P_MOTOR_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/motor_rotor"));
    public static final PartialModel P_PLC_ROBOT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/table_controller/robot"));
    public static final PartialModel BUILDER_ROBOT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/builder/builder_robot"));
    public static final PartialModel LO_ARMATURE = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/linear_oscillator/armature"));
    public static final PartialModel STIRLING_ENGINE_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/stirling_engine/rotor"));
    public static final PartialModel HYDRAULIC_PRESS_ARM = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/hydraulic_press/arm"));
    public static final PartialModel LARGE_MOTOR_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/large_motor/large_motor_rotor.obj"));
    public static final PartialModel FLYWHEEL = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/flywheel/flywheel.obj"));
    public static final PartialModel CHARNEL_PUMP_PLUNGER = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/charnel_pump/charnel_pump_plunger.obj"));
    public static final PartialModel PHAGE_RAY_BASE = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/phage_ray/phage_ray.obj"));
    public static final PartialModel PHAGE_RAY_BARREL = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/phage_ray/phage_ray_barrel.obj"));

    public static final PartialModel FABRICATOR_ROTOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/fabricator/rotor"));
    public static final PartialModel FABRICATOR_SEGMENT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/fabricator/segment"));
    public static final PartialModel SKYWORM_SEGMENT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/skyworm/skyworm_segment"));

    // I hindsight I should have called this a turntable, but words elude me when I am forced to quickly name a file.
    public static PartialModel ROBOTIC_ARM_SPINNY_BIT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/robotic_arm/spinny_bit"));
    public static PartialModel ROBOTIC_ARM_SEGMENT_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/robotic_arm/segment_1"));
    public static PartialModel ROBOTIC_ARM_SEGMENT_2 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/robotic_arm/segment_2"));
    public static PartialModel IMPLANTER_SPINNY_BIT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/implanter/spinny_bit"));
    public static PartialModel IMPLANTER_HEAD = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/implanter/head"));

    public static PartialModel LARGE_FAN_VANES = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/large_fan/vanes"));

    public static final PartialModel VERTICAL_GAUGE_INDICATOR = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/neepbus/vertical_gauge_indicator"));
    public static final PartialModel LINEAR_LEVER_HANDLE = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/neepbus/linear_lever_handle"));

    public static final PartialModel COW_LIMB = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/cow_limb"));
    public static final PartialModel COW_HEAD = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/cow_head"));
    public static final PartialModel PIG_HEAD = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/pig_head"));
    public static final PartialModel PIG_HEAD_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/pig_head_1"));
    public static final PartialModel UNKNOWN_LIMB_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/unknown_limb_1"));
    public static final PartialModel UNKNOWN_LIMB_2 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/unknown_limb_2"));
    public static final PartialModel UNKNOWN_BODY_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/parts/unknown_body_1"));
    public static final PartialModel FARMING_SCUTTER = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/scutter"));
    public static final PartialModel COMPRESSOR_SCUTTER = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/compressor_scutter"));
    public static final PartialModel ESCAPED_MACHINE_HEAD = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/escaped_machine/head"));
    public static final PartialModel ESCAPED_MACHINE_SEGMENT = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/escaped_machine/segment"));

    public static final PartialModel LUCKY_ONE_HEAD = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/lucky_one/head"));
    public static final PartialModel LUCKY_ONE_ARM_L = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/lucky_one/arm_l"));
    public static final PartialModel LUCKY_ONE_ARM_R = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/lucky_one/arm_r"));
    public static final PartialModel LUCKY_ONE_BODY = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/lucky_one/body"));

    public static final PartialModel TREE_VACUUM_PLUNGER = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/tree_vacuum/tree_vacuum_plunger"));
    public static final PartialModel TREE_VACUUM_LUNG = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "block/living_machine/tree_vacuum/tree_vacuum_lung"));

    public static NMExtraModels INSTANCE = new NMExtraModels();

    public static ResourceLocation BIG_LEVER_HANDLE = new ResourceLocation(NeepMeat.NAMESPACE, "block/big_lever_handle");
    public static ResourceLocation ITEM_PUMP_SHUTTLE = new ResourceLocation(NeepMeat.NAMESPACE, "block/item_pump_shuttle");
    public static PartialModel P_ITEM_PUMP_SHUTTLE = PartialModel.of(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "block/item_pump_shuttle"));
    public static ResourceLocation ITEM_PUMP_CHEST = new ResourceLocation(NeepMeat.NAMESPACE, "block/item_pump_chest");
    public static ResourceLocation VALVE_WHEEL = new ResourceLocation(NeepMeat.NAMESPACE, "block/valve/wheel");
    public static ResourceLocation MOTOR_ROTOR = new ResourceLocation(NeepMeat.NAMESPACE, "block/motor_rotor");
    public static ResourceLocation DEPLOYER_SHUTTLE = new ResourceLocation(NeepMeat.NAMESPACE, "block/deployer/shuttle");
    public static ResourceLocation AGITATOR_BLADES = new ResourceLocation(NeepMeat.NAMESPACE, "block/agitator/agitator_blades");
    public static ResourceLocation MIXER_AGITATOR_BLADES = new ResourceLocation(NeepMeat.NAMESPACE, "block/mixer/agitator");
    public static ResourceLocation INTEGRATOR_BASE = new ResourceLocation(NeepMeat.NAMESPACE, "block/integrator/base");
    public static ResourceLocation ADVANCED_INTEGRATOR_MEAT = new ResourceLocation(NeepMeat.NAMESPACE, "block/advanced_integrator/meat");
    public static ResourceLocation ADVANCED_INTEGRATOR_DANGLIES = new ResourceLocation(NeepMeat.NAMESPACE, "block/advanced_integrator/danglies");
    public static ResourceLocation TROMMEL_MESH = new ResourceLocation(NeepMeat.NAMESPACE, "block/trommel/mesh");
    public static ResourceLocation SMALL_TROMMEL_MESH = new ResourceLocation(NeepMeat.NAMESPACE, "block/small_trommel/mesh");
    public static ResourceLocation LARGE_BLADE = new ResourceLocation(NeepMeat.NAMESPACE, "block/blades/blade");
    public static ResourceLocation PUMP = new ResourceLocation(NeepMeat.NAMESPACE, "block/bottler/pump");
    public static ResourceLocation PYLON_ROTOR = new ResourceLocation(NeepMeat.NAMESPACE, "block/pylon/rotor");
    public static ResourceLocation PYLON_ROTOR_ACTIVE = new ResourceLocation(NeepMeat.NAMESPACE, "block/pylon/rotor_active");
    public static ResourceLocation SYNTHESISER_PLUNGER = new ResourceLocation(NeepMeat.NAMESPACE, "block/synthesiser/plunger");
    public static ResourceLocation SURGERY_ROBOT = new ResourceLocation(NeepMeat.NAMESPACE, "block/table_controller/robot");
    public static ResourceLocation EGG = new ResourceLocation(NeepMeat.NAMESPACE, "block/mob_egg");
    public static ResourceLocation CRUSHER_JAW = new ResourceLocation(NeepMeat.NAMESPACE, "block/grinder/jaw");

    public static ResourceLocation ROCK_DRILL_OFF = new ResourceLocation(NeepMeat.NAMESPACE, "item/rock_drill/rock_drill_off");
    public static ResourceLocation ROCK_DRILL_ON = new ResourceLocation(NeepMeat.NAMESPACE, "item/rock_drill/rock_drill_on");
    public static ResourceLocation ROCK_DRILL_ROD = new ResourceLocation(NeepMeat.NAMESPACE, "item/rock_drill/rock_drill_rod");
    public static ResourceLocation ROCK_DRILL_HEAD = new ResourceLocation(NeepMeat.NAMESPACE, "item/meatgun/rock_drill_head");
    public static ResourceLocation ROTARY_DRILL_HEAD = new ResourceLocation(NeepMeat.NAMESPACE, "item/meatgun/rotary_drill_head");

    public static ResourceLocation ROLLED_NEWSPAPER = new ResourceLocation(NeepMeat.NAMESPACE, "item/rolled_newspaper_base");

    public static ResourceLocation TISSUE_OPERATOR_MAIN = new ResourceLocation(NeepMeat.NAMESPACE, "item/tissue_operator/tissue_operator");
    public static ResourceLocation TISSUE_OPERATOR_FRONT = new ResourceLocation(NeepMeat.NAMESPACE, "item/tissue_operator/tissue_operator_front");
    public static ResourceLocation TISSUE_OPERATOR_WHEEL = new ResourceLocation(NeepMeat.NAMESPACE, "item/tissue_operator/tissue_operator_wheel");

    public static ResourceLocation MOP_MAIN = new ResourceLocation(NeepMeat.NAMESPACE, "item/mop_main");

    public static final RotaryDrillHeadModel IRON_ROTARY_DRILL_HEAD = new RotaryDrillHeadModel(new ResourceLocation(NeepMeat.NAMESPACE, "item/rotary_drill_head/iron"));
    public static final RotaryDrillHeadModel MEAT_STEEL_ROTARY_DRILL_HEAD = new RotaryDrillHeadModel(new ResourceLocation(NeepMeat.NAMESPACE, "item/rotary_drill_head/meat_steel"));
    public static final RotaryDrillHeadModel DIAMOND_ROTARY_DRILL_HEAD = new RotaryDrillHeadModel(new ResourceLocation(NeepMeat.NAMESPACE, "item/rotary_drill_head/diamond"));
    public static final Map<MeatgunModule.Type<?>, RotaryDrillHeadModel> ROTARY_DRILL_HEADS = Util.make(new IdentityHashMap<>(), m -> {
        m.put(NMMeatgunModules.IRON_ROTARY_DRILL_HEAD, IRON_ROTARY_DRILL_HEAD);
        m.put(NMMeatgunModules.MEAT_STEEL_ROTARY_DRILL_HEAD, MEAT_STEEL_ROTARY_DRILL_HEAD);
        m.put(NMMeatgunModules.DIAMOND_ROTARY_DRILL_HEAD, DIAMOND_ROTARY_DRILL_HEAD);
    });

    public void provideExtraModels(Context ctx, Consumer<ResourceLocation> out)
    {
        out.accept(BIG_LEVER_HANDLE);
        out.accept(ITEM_PUMP_SHUTTLE);
        out.accept(ITEM_PUMP_CHEST);
        out.accept(VALVE_WHEEL);
        out.accept(MOTOR_ROTOR);
        out.accept(DEPLOYER_SHUTTLE);
        out.accept(AGITATOR_BLADES);
        out.accept(MIXER_AGITATOR_BLADES);
        out.accept(INTEGRATOR_BASE);
        out.accept(ADVANCED_INTEGRATOR_MEAT);
        out.accept(ADVANCED_INTEGRATOR_DANGLIES);
        out.accept(TROMMEL_MESH);
        out.accept(SMALL_TROMMEL_MESH);
        out.accept(LARGE_BLADE);
        out.accept(PUMP);
        out.accept(PYLON_ROTOR);
        out.accept(PYLON_ROTOR_ACTIVE);
        out.accept(SYNTHESISER_PLUNGER);
        out.accept(SURGERY_ROBOT);
        out.accept(EGG);
        out.accept(CRUSHER_JAW);
        out.accept(ROCK_DRILL_ON);
        out.accept(ROCK_DRILL_OFF);
        out.accept(ROCK_DRILL_HEAD);
        out.accept(ROTARY_DRILL_HEAD);
        out.accept(ROLLED_NEWSPAPER);
        out.accept(TISSUE_OPERATOR_MAIN);
        out.accept(TISSUE_OPERATOR_FRONT);
        out.accept(TISSUE_OPERATOR_WHEEL);
        out.accept(MOP_MAIN);

        out.accept(NMImplantsClient.SPIDER_LEG_UPPER.modelLocation());

        IRON_ROTARY_DRILL_HEAD.apply(ctx);
        MEAT_STEEL_ROTARY_DRILL_HEAD.apply(ctx);
        DIAMOND_ROTARY_DRILL_HEAD.apply(ctx);
    }

    public static void init()
    {
    }

    @Override
    public void onInitializeModelLoader(Context context)
    {
        provideExtraModels(context, context::addModels);
    }

    private NMExtraModels()
    {

    }
}
