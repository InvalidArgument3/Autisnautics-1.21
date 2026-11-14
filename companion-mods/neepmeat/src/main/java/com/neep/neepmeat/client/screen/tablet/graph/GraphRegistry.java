package com.neep.neepmeat.client.screen.tablet.graph;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.machine.breaker.LinearOscillatorBlockEntity;
import com.neep.neepmeat.machine.death_blades.DeathBladesBlockEntity;
import com.neep.neepmeat.machine.grinder.CrusherBlockEntity;
import com.neep.neepmeat.machine.heater.HeaterBlockEntity;
import com.neep.neepmeat.machine.item_mincer.ItemMincerBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity;
import com.neep.neepmeat.machine.live_machine.process.TreeVacuumProcess;
import com.neep.neepmeat.machine.mincer.MincerBlockEnity;
import com.neep.neepmeat.machine.mixer.MixerBlockEntity;
import com.neep.neepmeat.machine.multitank.MultiTankBlockEntity;
import com.neep.neepmeat.machine.small_trommel.SmallTrommelBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class GraphRegistry
{
    private static final Map<ResourceLocation, Supplier<GraphData>> GRAPHS = new HashMap<>();
    private static final GraphData DEFAULT = new GraphData();

    @Nullable
    public static GraphData get(ResourceLocation id)
    {
        return GRAPHS.getOrDefault(id, () -> DEFAULT).get();
    }

    public static void register(ResourceLocation id, Supplier<GraphData> data)
    {
        GRAPHS.put(id, data);
    }

    static
    {
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "test"),
                () -> new GraphData()
                .setPoints(new float[][]{
                        {0, 0},
                        {0.1f, 0.1f},
                        {0.2f, 0.1f},
                        {0.5f, 0.5f},
                        {1f, 0.8f},
                })
                .setXTitle("Input power", "eJ/t")
                .setYTitle("Feed rate", "1/min")
                .setYTicks(new double[]{ 0.1, 0.8 })
                .setXTicks(List.of(new GraphData.Tick(0.1f, true), new GraphData.Tick(0.5f, true)))
        );

        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "feeding_trough"), () -> {
                    // hardcoded for now. 100 is the minimum time between feeds and 60*20 (1 min) is the maximum.
                    float maxTime = 60 * 20 / 20f;
                    float minTime = 100 / 20f;
                    float maxPu = (1f - minTime / (60f)) * PowerUtil.BASE_POWER;
                    float minPu = (1f - maxTime / (60f)) * PowerUtil.BASE_POWER;
                    return new GraphData()
                            .setPoints(new float[][]{
                                    {0, maxTime},
                                    {minPu, maxTime},
                                    {maxPu, minTime},
                                    {1000, minTime},
                            })
                            .setXRange(new GraphData.Range(0, 1000))
                            .setYRange(new GraphData.Range(0, maxTime * 1.1f))
                            .setXTitle("Motor power", "eJ/t")
                            .setYTitle("Time between feeds", "s")
                            .setYTicks(new double[]{minTime, maxTime})
                            .setXTicks(List.of(new GraphData.Tick(minPu, false), new GraphData.Tick(maxPu, true)));
        });

        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "crusher"), () ->
                arbitraryStandardPower(CrusherBlockEntity.MIN_POWER, CrusherBlockEntity.MAX_POWER, 40e3f));
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "large_crusher"), () ->
                arbitraryStandardPower(0.1f, 2f, 40e3f));

        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "large_mincer"), MincerBlockEnity::makePowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "death_blades"), DeathBladesBlockEntity::makePowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "small_trommel"), SmallTrommelBlockEntity::makePowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "mixer"), MixerBlockEntity::makePowerGraph);

        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "heater"), HeaterBlockEntity::makePowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "heater_fluid_heating"), MultiTankBlockEntity::makeHeatingPowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "breaker"), LinearOscillatorBlockEntity::makePowerGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "tree_vacuum"), TreeVacuumProcess::createGraph);
        register(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "item_mincer"), ItemMincerBlockEntity::makePowerGraph);
    }

    /// Generates a graph where the axis scales are arbitrary, but the min and max power values are displayed.
    /// Alternative to a log scale.
    public static GraphData arbitraryStandardPower(float minPu, float maxPu, float referenceEnergy)
    {
        float referenceWork = referenceEnergy / 1000;

        // Minimum and saturation power
        float minAbs = minPu * 1000;
        float maxAbs = maxPu * 1000;

        // Time taken to impart referenceAbsEnergy
        float time0 = referenceWork / minPu / 20;
        float time1 = referenceWork / maxPu / 20;

        float x0 = minAbs / 2;
        float x1 = minAbs;
        float x2 = maxAbs;
        float x3 = maxAbs * 1.5f;
        float yEnd = time0 * 1.1f;

        DecimalFormat df = new DecimalFormat("###.#");

        return new GraphData()
                .semiLogX(true)
                .setTitle("Process time for a 40keJ recipe")
                .setXRange(new GraphData.Range(x0, x3))
                .setYRange(new GraphData.Range(0, yEnd))
                .setXTicks(List.of(
                        new GraphData.Tick(0, false),
                        new GraphData.Tick(x1, true, df.format(minAbs)),
                        new GraphData.Tick(x2, true, df.format(maxAbs))))
                .setYTicks(List.of(
                        new GraphData.Tick(time1, false, df.format(time1)),
                        new GraphData.Tick(time0, false, df.format(time0))
                ))
                .setXTitle("Input power", "ej/t")
                .setYTitle("Recipe time", "s")
                .setPoints(GraphData.join(
                        new float[][]{{0, 0}, {x1, 0}, {x1, time0}},
                        GraphData.sample(
                                GraphData.logRange(x1, x2, 10),
                                x -> x, x -> (referenceWork / (x / 1000 * 20))),
                        new float[][]{{x3, time1}}
                ))
                ;
    }

    public static GraphData standardPower(float minPu, float maxPu, float referenceAbsEnergy)
    {
        float minAbs = minPu * 1000;
        float maxAbs = maxPu * 1000;

        float minX = Math.min(0, minAbs);
        float maxX = Math.max(1000, maxAbs * 1.1f);

        // Time taken to impart referenceAbsEnergy
        float time0 = referenceAbsEnergy / minAbs / 20;
        float time1 = referenceAbsEnergy / maxAbs / 20;

        return new GraphData()
                .setXRange(new GraphData.Range(minX, maxX))
                .setYRange(new GraphData.Range(0, Math.max(time1, time0) * 1.1f))
                .setXTicks(List.of(new GraphData.Tick(minAbs, true), new GraphData.Tick(maxAbs, true)))
                .setYTicks(new double[]{time0, time1})
                .setXTitle("Input power", "ej/t")
                .setYTitle("Time of a 40keJ recipe", "s")
                .setPoints(new float[][]{
                        {minX, 0},
                        {minAbs, 0},
                        {minAbs, time0},
                        {maxAbs, time1},
                        {maxX, time1}
                })
                ;
    }
}
