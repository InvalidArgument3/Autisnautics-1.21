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
 *
 * Meatweapons sounds are registered on neepmeat. A repeated upstream id gets the field name appended.
 */

package com.neep.neepmeat.init;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NMSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, NeepMeat.ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_LEVER_ON = register("big_lever_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_LEVER_OFF = register("big_lever_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERROR = register("error");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOME_HIT = register("glome_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOUND_HIT = register("hound_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOUND_DEATH = register("hound_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BH_HIT = register("bovine_horror_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> BH_CHARGE = register("bovine_horror_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> BH_PHASE2 = register("bovine_horror_phase2");
    public static final DeferredHolder<SoundEvent, SoundEvent> BH_SPIT = register("bovine_horror_spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYWORM_ASCEND = register("skyworm_ascend");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYWORM_DESCEND = register("skyworm_descend");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYWORM_DESCEND_SHORT = register("skyworm_descend_short");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYWORM_DISPENSE = register("skyworm_spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> ADVANCED_INTEGRATOR_CHARGE = register("advanced_integrator_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ADVANCED_INTEGRATOR_AMBIENT = register("advanced_integrator_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHAGE_RAY_CHARGE = register("phage_ray_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHAGE_RAY_RUNNING = register("phage_ray_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> METAL_BARREL_OPEN = register("metal_barrel_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> METAL_BARREL_CLOSE = register("metal_barrel_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> TREE_VACUUM_SUCK = register("tree_vacuum_suck");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARNEL_PUMP_UP = register("charnel_pump_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARNEL_PUMP_TOP = register("charnel_pump_top");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARNEL_PUMP_DOWN = register("charnel_pump_down");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARNEL_PUMP_GLUG = register("charnel_pump_glug");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARNEL_PUMP_IDLE = register("charnel_pump_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> MECHANICAL_MACHINE_PLACE = register("mechanical_machine_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> MECHANICAL_MACHINE_BREAK = register("mechanical_machine_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLESH_MACHINE_PLACE = register("flesh_machine_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSDUCER_PLACE = register("block/transductive_cannula_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> VASCULAR_CONDUIT_PLACE = register("block/vascular_conduit_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> TANK_PLACE = register("tank_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> MULTIBLOCK_ASSEMBLE = register("multiblock_assemble");
    public static final DeferredHolder<SoundEvent, SoundEvent> MULTIBLOCK_DISASSEMBLE = register("multiblock_disassemble");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBINE_INSTRUCTION_APPLY = register("combine_instruction_apply");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPLANT_INSTRUCTION_APPLY = register("implant_instruction_apply");
    public static final DeferredHolder<SoundEvent, SoundEvent> INJECT_INSTRUCTION_APPLY = register("inject");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEPOSIT_ITEMS = register("deposit_items");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_FIRE = register("fusion_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_BLAST_FIRE = register("fusion_blast_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_BLAST_CHARGE = register("fusion_blast_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD = register("fusion_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MECHANICAL_HISS = register("reload_mechanical_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP_FIRE = register("zap_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP_HIT = register("zap_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEATGUN_MODULE_PLACE = register("meatgun_module_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEATGUN_MODULE_REMOVE = register("meatgun_module_remove");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHUGGER_FIRE = register("chugger_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSHER_FIRE = register("bosher_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> LONG_BOI_CHARGE = register("long_boi_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> LONG_BOI_FIRE = register("long_boi_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRENADE_LAUNCHER_FIRE = register("grenade_launcher_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOODTHROWER_ACTIVE = register("bloodthrower_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK_STAFF_ATTACK = register("shock_staff_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK_STAFF_HIT = register("shock_staff_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAND_CANNON_FIRE = register("hand_cannon_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAND_CANNON_HIT = register("fusion_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAND_CANNON_RELOAD = register("fusion_reload_hand_cannon_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_RICOCHET = register("bullet_ricochet");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRILL_RUNNING = register("drill_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCK_DRILL = register("rock_drill");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRESSURE_WASHER_SQUIRT = register("pressure_washer_squirt");
    public static final DeferredHolder<SoundEvent, SoundEvent> AIRTRUCK_STARTING = register("airtruck_starting");
    public static final DeferredHolder<SoundEvent, SoundEvent> AIRTRUCK_RUNNING = register("airtruck_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPOUND_INJECTOR = register("compound_injector");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAKE_SCREAM = register("cake_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAKE_FIRE = register("cake_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> COSMIC_BEAM = register("cosmic_beam");
    public static final DeferredHolder<SoundEvent, SoundEvent> PYLON_START = register("pylon_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> PYLON_ACTIVE = register("pylon_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARATHON_BEEP = register("beep");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOSIMETER_ALARM = register("dosimeter_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPRESSED_AIR_FILL = register("compressed_air_fill");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIVISECTION_COMPLETE = register("vivisection_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCALPEL_HIT = register("scalpel_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> LMG_FIRE = register("light_machine_gun_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> AR_FIRE = register("ar_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRENADE = register("grenade_fire");

    public static final SoundEvent INSERT_ITEM = SoundEvents.BUNDLE_INSERT;
    public static final SoundEvent EXTRACT_ITEM = SoundEvents.BUNDLE_REMOVE_ONE;

    private NMSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        String safe = path.replace('/', '_');
        return SOUNDS.register(safe, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, safe)));
    }
}
