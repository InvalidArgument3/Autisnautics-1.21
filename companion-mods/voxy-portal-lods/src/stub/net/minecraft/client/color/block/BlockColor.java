package net.minecraft.client.color.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockColor {
    int getColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex);
}
