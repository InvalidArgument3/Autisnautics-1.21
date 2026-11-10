package com.neep.neepmeat.api;

import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.charnel_pump.ContaminatedGrassBlock;
import com.neep.neepmeat.machine.charnel_pump.ContaminationBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ContaminationUtil
{
    public static boolean canContaminate(BlockState state)
    {
        return state.is(BlockTags.DIRT);
    }

    public static boolean contaminate(Level world, BlockPos pos, @Nullable BlockState state, int maxLevel, int increase)
    {
        if (state == null)
            state = world.getBlockState(pos);

        if (canContaminate(state))
        {
            world.setBlockAndUpdate(pos, getStateFor(state, world.getRandom(), Math.min(maxLevel, getContaminationLevel(state) + increase)));
            return true;
        }
        return false;
    }

    public static int getContaminationLevel(BlockState state)
    {
        if (state.getBlock() instanceof ContaminationBlock c)
            return c.getLevel(state);

        return 0;
    }

    public static BlockState getStateFor(BlockState prev, RandomSource random, int level)
    {
        if (level == 0)
            return prev;

        if (prev.is(Blocks.GRASS_BLOCK) || prev.is(NMBlocks.CONTAMINATED_GRASS_BLOCK))
        {
            if (level == 1)
                return ((ContaminatedGrassBlock) NMBlocks.CONTAMINATED_GRASS_BLOCK).withLevel(1);
//            return random.nextFloat() > 0.3 ? NMBlocks.CONTAMINATED_GRASS_BLOCK.getDefaultState() : NMBlocks.CONTAMINATED_DIRT.getDefaultState();
            else if (level == 2)
                return ((ContaminatedGrassBlock) NMBlocks.CONTAMINATED_GRASS_BLOCK).withLevel(2);
            else if (level == 3)
                return NMBlocks.CONTAMINATED_DIRT.defaultBlockState();
            else
                return NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState();
        }
        else if (prev.is(BlockTags.DIRT))
        {
            if (level == 1)
                return prev;
            else if (level == 2 || level == 3)
                 return NMBlocks.CONTAMINATED_DIRT.defaultBlockState();
            else if (level > 3)
                return NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState();
        }

        return level > 3 ? NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState() : prev;
    }
}
