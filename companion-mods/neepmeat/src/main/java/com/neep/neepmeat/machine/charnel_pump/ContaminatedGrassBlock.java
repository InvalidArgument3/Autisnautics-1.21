package com.neep.neepmeat.machine.charnel_pump;

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.ContaminationUtil;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.util.IterateRandomly;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ContaminatedGrassBlock extends GrassBlock implements ContaminationBlock
{
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 1);

    public ContaminatedGrassBlock(RegistrationContext c, NMItemSettings itemSettings, Properties settings)
    {
        super(settings);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    // Contamination level, not actual level
    public BlockState withLevel(int level)
    {
        return defaultBlockState().setValue(LEVEL, Mth.clamp(level - 1, 0, 1));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (getLevel(state) > 0)
        {
            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (int i = 0; i < 4; ++i)
            {
                mutable.setWithOffset(pos, random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);

                BlockState offsetState = world.getBlockState(mutable);
                if (offsetState.is(Blocks.GRASS_BLOCK))
                {
                    ContaminationUtil.contaminate(world, mutable, offsetState, getLevel(state) - 1, 1);
                    return;
                }
            }
        }

        super.randomTick(state, world, pos, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    public static int getTint(BlockState state, BlockAndTintGetter world, BlockPos pos, int i)
    {
         int normal = world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : GrassColor.getDefaultColor();
         // Hmmm
         int level = state.getValue(LEVEL);
         return adjustTint(normal, pos != null ? Biome.BIOME_INFO_NOISE.getValue(pos.getX(), pos.getZ(), false) : 0,
                 level == 0 ? 0.8f : 0.5f);
    }

    public static int adjustTint(int normal, double perturb, float blend)
    {
        // Alpha blend
        int browne = 0x3d1911;
        float f = (float) (blend + perturb * 0.2f);
        return ((int) (((normal >> 16) & 0xFF) * f + ((browne >> 16) & 0xFF) * (1 - f)) << 16)
                | ((int) (((normal >> 8) & 0xFF) * f + ((browne >> 8) & 0xFF) * (1 - f)) << 8)
                | (int) (((normal) & 0xFF) * f + ((browne) & 0xFF) * (1 - f));
    }

    public static int getItemTint(ItemStack stack, int i)
    {
        return adjustTint(GrassColor.getDefaultColor(), 0, 0.5f);
    }

    @Override
    public int getLevel(BlockState state)
    {
        return state.getValue(LEVEL) + 1;
    }
}
