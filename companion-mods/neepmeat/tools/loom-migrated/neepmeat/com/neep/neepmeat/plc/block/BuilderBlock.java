package com.neep.neepmeat.plc.block;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.screen.BuilderScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BuilderBlock extends BaseHorFacingBlock implements EntityBlock
{
    public BuilderBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return BuilderBlock.box(0, 0, 0, 16, 12, 16);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof BuilderBlockEntity be)
        {
            player.openMenu(
                    (SimpleScreenHandlerFactory) (s, i, p) -> new BuilderScreenHandler(i, s,
                            be.getInventory(),
                            be::getSelectedSlot,
                            be::getSelectedPattern
                    ));
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!world.isClientSide() && !newState.is(this) && world.getBlockEntity(pos) instanceof BuilderBlockEntity be)
        {
            MeatlibStorageUtil.scatter(world, pos, be.getStorage(null));
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        if (context.getPlayer() == null)
            return defaultBlockState();

        return defaultBlockState().setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection() : context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return PLCBlocks.BUILDER_BE.create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, PLCBlocks.BUILDER_BE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }
}
