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

import com.neep.meatlib.registry.SoundRegistry;
import com.neep.meatweapons.MeatWeapons;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class NMSounds
{
    public static SoundEvent BIG_LEVER_ON = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "big_lever_on");
    public static SoundEvent BIG_LEVER_OFF = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "big_lever_off");
    public static SoundEvent ERROR = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "error");
    public static SoundEvent INSERT_ITEM = SoundEvents.BUNDLE_INSERT;
    public static SoundEvent EXTRACT_ITEM = SoundEvents.BUNDLE_REMOVE_ONE;

    public static SoundEvent GLOME_HIT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "glome_hit");
    public static SoundEvent HOUND_HIT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "hound_hit");
    public static SoundEvent HOUND_DEATH = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "hound_death");

    public static SoundEvent BH_HIT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "bovine_horror_hit");
    public static SoundEvent BH_CHARGE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "bovine_horror_charge");
    public static SoundEvent BH_PHASE2 = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "bovine_horror_phase2");
    public static SoundEvent BH_SPIT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "bovine_horror_spit");

    public static SoundEvent SKYWORM_ASCEND = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "skyworm_ascend");
    public static SoundEvent SKYWORM_DESCEND = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "skyworm_descend");
    public static SoundEvent SKYWORM_DESCEND_SHORT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "skyworm_descend_short");
    public static SoundEvent SKYWORM_DISPENSE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "skyworm_spit");

    public static SoundEvent ADVANCED_INTEGRATOR_CHARGE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "advanced_integrator_charge");
    public static SoundEvent ADVANCED_INTEGRATOR_AMBIENT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "advanced_integrator_ambient");

    public static SoundEvent PHAGE_RAY_CHARGE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "phage_ray_charge");
    public static SoundEvent PHAGE_RAY_RUNNING = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "phage_ray_running");

    public static SoundEvent METAL_BARREL_OPEN = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "metal_barrel_open");
    public static SoundEvent METAL_BARREL_CLOSE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "metal_barrel_close");

    public static SoundEvent TREE_VACUUM_SUCK = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "tree_vacuum_suck");


    public static SoundEvent CHARNEL_PUMP_UP = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "charnel_pump_up");
    public static SoundEvent CHARNEL_PUMP_TOP = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "charnel_pump_top");
    public static SoundEvent CHARNEL_PUMP_DOWN = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "charnel_pump_down");
    public static SoundEvent CHARNEL_PUMP_GLUG = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "charnel_pump_glug");
    public static SoundEvent CHARNEL_PUMP_IDLE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "charnel_pump_idle");

    public static SoundEvent MECHANICAL_MACHINE_PLACE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "mechanical_machine_place");
    public static SoundEvent MECHANICAL_MACHINE_BREAK = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "mechanical_machine_break");
    public static SoundEvent FLESH_MACHINE_PLACE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "flesh_machine_place");
    public static SoundEvent TRANSDUCER_PLACE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "block/transductive_cannula_place");
    public static SoundEvent VASCULAR_CONDUIT_PLACE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "block/vascular_conduit_place");
    public static SoundEvent TANK_PLACE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "tank_place");

    public static SoundEvent MULTIBLOCK_ASSEMBLE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "multiblock_assemble");
    public static SoundEvent MULTIBLOCK_DISASSEMBLE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "multiblock_disassemble");

    public static SoundEvent COMBINE_INSTRUCTION_APPLY = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "combine_instruction_apply");
    public static SoundEvent IMPLANT_INSTRUCTION_APPLY = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "implant_instruction_apply");
    public static SoundEvent INJECT_INSTRUCTION_APPLY = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "inject");

    public static SoundEvent DEPOSIT_ITEMS = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "deposit_items");

    public static SoundEvent FUSION_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_fire");
    public static SoundEvent FUSION_BLAST_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_blast_fire");
    public static SoundEvent FUSION_BLAST_CHARGE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_blast_charge");

    public static SoundEvent RELOAD = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_reload");
    public static SoundEvent RELOAD_MECHANICAL_HISS = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "reload_mechanical_hiss");
    public static SoundEvent ZAP_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "zap_fire");
    public static SoundEvent ZAP_HIT = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "zap_hit");

    public static SoundEvent MEATGUN_MODULE_PLACE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "meatgun_module_place");
    public static SoundEvent MEATGUN_MODULE_REMOVE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "meatgun_module_remove");
    public static SoundEvent CHUGGER_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "chugger_fire");
    public static SoundEvent BOSHER_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "bosher_fire");
    public static SoundEvent LONG_BOI_CHARGE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "long_boi_charge");
    public static SoundEvent LONG_BOI_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "long_boi_fire");
    public static SoundEvent GRENADE_LAUNCHER_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "grenade_launcher_fire");
    public static SoundEvent BLOODTHROWER_ACTIVE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "bloodthrower_active");
    public static SoundEvent SHOCK_STAFF_ATTACK = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "shock_staff_attack");
    public static SoundEvent SHOCK_STAFF_HIT = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "shock_staff_hit");

    public static SoundEvent HAND_CANNON_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "hand_cannon_fire");
    public static SoundEvent HAND_CANNON_HIT = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_hit");
    public static SoundEvent HAND_CANNON_RELOAD = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "fusion_reload");

    public static SoundEvent BULLET_RICOCHET = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "bullet_ricochet");

    public static SoundEvent DRILL_RUNNING = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "drill_running");
    public static SoundEvent ROCK_DRILL = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "rock_drill");

    public static SoundEvent PRESSURE_WASHER_SQUIRT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "pressure_washer_squirt");

    public static SoundEvent AIRTRUCK_STARTING = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "airtruck_starting");
    public static SoundEvent AIRTRUCK_RUNNING = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "airtruck_running");

    public static SoundEvent COMPOUND_INJECTOR = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "compound_injector");

    public static SoundEvent CAKE_SCREAM = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "cake_scream");
    public static SoundEvent CAKE_FIRE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "cake_fire");

    public static SoundEvent COSMIC_BEAM = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "cosmic_beam");
    public static SoundEvent PYLON_START = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "pylon_start");
    public static SoundEvent PYLON_ACTIVE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "pylon_active");
    public static SoundEvent MARATHON_BEEP = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "beep");
    public static SoundEvent DOSIMETER_ALARM = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "dosimeter_alarm");


    public static SoundEvent COMPRESSED_AIR_FILL = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "compressed_air_fill");

    public static SoundEvent VIVISECTION_COMPLETE = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "vivisection_complete");
    public static SoundEvent SCALPEL_HIT = SoundRegistry.registerSound(NeepMeat.NAMESPACE, "scalpel_hit");

    public static SoundEvent LMG_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "light_machine_gun_fire");
    public static SoundEvent AR_FIRE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "ar_fire");
    public static SoundEvent GRENADE = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "grenade_fire");

    public static void init()
    {
        SoundRegistry.flush();
    }
}
