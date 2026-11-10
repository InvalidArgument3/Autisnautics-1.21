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

package com.neep.neepmeat.config;

import com.neep.neepmeat.machine.live_machine.component.FluidOutputComponent;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import org.w3c.dom.CDATASection;

public class NMConfig extends ReflectiveConfig
{
    @SerializedName("ore_fat")
    public final OreFat oreFat = new OreFat();
    public static final class OreFat extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Rendering ore fat yield when not overridden in JSON")
        public final TrackedValue<Float> defaultRenderingYield_1 = value(1.2f);

        @Comment("Trommel ore fat yield when not overridden in JSON")
        public final TrackedValue<Float> defaultTrommelYield_1 = value(1.2f);
    }


    @SerializedName("motors")
    public final Motors motors = new Motors();
    public static final class Motors extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Maximum power that can be transferred by a basic motor (eJ/t)")
        public final TrackedValue<Integer> basicMotorMaxPower = value(400);

        @Comment("Maximum power that can be transferred by an advanced motor (eJ/t)")
        public final TrackedValue<Integer> advancedMotorMaxPower = value(800);

        @Comment("Maximum power that can be transferred by a large motor (eJ/t)")
        public final TrackedValue<Integer> largeMotorMaxPower = value(2000);
    }

    @SerializedName("large_compressor")
    public final LargeCompressor largeCompressor = new LargeCompressor();
    public static final class LargeCompressor extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Droplets per unit energy per tick (droplets/eJ/t)")
        public final TrackedValue<Integer> powerToDroplets = value(400);
    }



    @SerializedName("charnel_pump")
    public final CharnelPump charnelPump = new CharnelPump();
    public static final class CharnelPump extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Minimum power required to run (eJ/t)")
        public final TrackedValue<Integer> minPower = value(500);

        @Comment("Air consumed per unit energy (droplets/eJ/t)")
        public final TrackedValue<Integer> powerToAir = value(100);
    }


    @SerializedName("pylon")
    public final Pylon pylon = new Pylon();
    public static final class Pylon extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Maximum angular speed of the Pylon (???)")
        public final TrackedValue<Float> maxSpeed = value(40f);

        @Comment("Angular speed at which the Pylon begins working (???)")
        public final TrackedValue<Float> runningSpeed = value(16f);
    }


    @SerializedName("feeding_trough")
    public final FeedingTrough feedingTrough = new FeedingTrough();
    public static final class FeedingTrough extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Amount of feed used to breed two animals (droplets)")
        public final TrackedValue<Long> useAmount = value(FluidConstants.BUCKET / 6);

        @Comment("Amount of animal feed added per gland potato (droplets)")
        public final TrackedValue<Long> feedPerItem = value(useAmount.getDefaultValue() / 2);
    }

    @SerializedName("metaboliser")
    public final Metaboliser metaboliser = new Metaboliser();
    public static final class Metaboliser extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Maximum segments")
        public final TrackedValue<Integer> maxSegments = value(6);
    }


    @SerializedName("verrucous_stone")
    public final VerrucousStone verrucousStone = new VerrucousStone();
    public static final class VerrucousStone extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Energy generated per Verrucous Stone block per tick (eJ/t)")
        public final TrackedValue<Integer> passiveCoefficient = value(10);
    }

    @SerializedName("living_machines")
    public final LivingMachines livingMachines = new LivingMachines();
    public static final class LivingMachines extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Maximum number of blocks that can be part of a single living machine")
        public final TrackedValue<Integer> maxBlocks = value(128);
    }

    @SerializedName("power_flower")
    public final PowerFlower powerFlower = new PowerFlower();
    public static final class PowerFlower extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Energy generated per Power Flower surface block per tick (eJ/t)")
        public final TrackedValue<Integer> solarCoefficient = value(10);

        @Comment("Maximum number of blocks that can be part of a single Power Flower")
        public final TrackedValue<Integer> maxBlocks = value(160);
    }

    @SerializedName("fluid_drain")
    public final Drain drain = new Drain();
    public static final class Drain extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("Amount of blood per mob when killed above a drain with a Tissue Operator")
        public final TrackedValue<Long> bloodPerMob = value(FluidConstants.BUCKET / 4);

        @Comment("Fluid to produce when a mob is killed")
        public final TrackedValue<String> fluidId = value("neepmeat:blood");
    }

    @SerializedName("loot_tables")
    public final LootTables lootTables = new LootTables();
    public static final class LootTables extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        @Comment("If true, Integrator eggs will be injected into key loot tables")
        public final TrackedValue<Boolean> injectIntegratorEggs = value(true);
    }

    @Comment("If true, the Meatgun's ammo counter will be rendered on the item model instead of in the hud.")
    @Comment("Set to false if transparency issues occur.")
    public final TrackedValue<Boolean> meatgunAmmoOnModel = value(true);

    @Comment("Max module complexity of the drill chassis")
    public final TrackedValue<Integer> drillChassisMaxComplexity = value(16);
}
