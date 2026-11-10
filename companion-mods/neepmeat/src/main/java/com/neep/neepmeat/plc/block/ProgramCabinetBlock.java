package com.neep.neepmeat.plc.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.neepasm.vm.ProgramMemoryModule;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.ProgramCabinetBlockEntity;
import com.neep.neepmeat.plc.screen.ProgramCabinetScreenHandler;
import com.neep.neepmeat.util.BFSGroupFinder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class ProgramCabinetBlock extends TallBlock implements EntityBlock, ComputerBinder
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ProgramCabinetBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof ProgramCabinetBlockEntity be)
        {
            if (!world.isClientSide())
                player.openMenu((SimpleScreenHandlerFactory) (s, i, p) ->
                        new ProgramCabinetScreenHandler(i, s, be));

            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        var stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ProgramCabinetBlockEntity be)
        {
            stacks.forEach(be::saveToItem);

            return stacks;
        }
        return stacks;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof ProgramCabinetBlockEntity be)
        {
            be.updatePrograms();
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        if (ctx.getPlayer() == null)
            return defaultBlockState();

        return defaultBlockState().setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getHorizontalDirection() : ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
    {
        if (world.getBlockEntity(pos) instanceof ProgramCabinetBlockEntity be)
            handleBroken(world, player, be);

        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        super.onPlace(state, world, pos, oldState, notify);

        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof ProgramMemoryModule module)
        {
            new BFSGroupFinder<PLC>()
            {
                @Override
                protected State processPos(BlockPos pos)
                {
                    BlockPos.MutableBlockPos mutable = pos.mutable();

                    for (Direction direction : Direction.values())
                    {
                        mutable.setWithOffset(pos, direction);
                        if (world.getBlockState(mutable).getBlock() instanceof ComputerBinder)
                        {
                            queueBlock(mutable.immutable());
                            visited.add(mutable.asLong());

                            if (world.getBlockEntity(mutable) instanceof PLC plc)
                                addResult(mutable.immutable(), plc);
                        }
                    }
                    return State.CONTINUE;
                }
            }
            .queueBlock(pos)
            .loop(3)
            .getResult().values().forEach(p -> p.getProgramResolver().memoryAdded(module));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world.getBlockEntity(pos) instanceof ProgramCabinetBlockEntity be)
        {
            be.onRemoved();
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected Structure createStructure(RegistrationContext ctx)
    {
        return ctx.append(
                this, new Structure(MeatlibBlockSettings.copyOf(properties))
                {
                    @Override
                    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
                    {
                        if (world.getBlockEntity(pos.below()) instanceof ProgramCabinetBlockEntity be)
                        {
                            if (!world.isClientSide())
                                player.openMenu((SimpleScreenHandlerFactory) (s, i, p) -> new ProgramCabinetScreenHandler(i, s, be));

                            return InteractionResult.SUCCESS;
                        }
                        return super.useWithoutItem(state, world, pos, player, hit);
                    }

                    @Override
                    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
                    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
                        if (world.getBlockEntity(pos.below()) instanceof ProgramCabinetBlockEntity be)
                            handleBroken(world, player, be);

                        super.playerWillDestroy(world, pos, state, player);
                    }
                }, MeatlibBlock::structure);
    }

    private void handleBroken(Level world, Player player, ProgramCabinetBlockEntity be)
    {
        var pos = be.getBlockPos();
        if (!world.isClientSide() && player.isCreative() && !be.isEmpty())
        {
            ItemStack itemStack = asItem().getDefaultInstance();
            be.saveToItem(itemStack);

            ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options)
    {
        super.appendHoverText(stack, world, tooltip, options);

        CompoundTag blockNbt = BlockItem.getBlockEntityData(stack);
        if (blockNbt == null)
            return;

        Style style = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
        ProgramCabinetBlockEntity.getItems(blockNbt).map(ItemStack::getHoverName)
                .forEach(t -> tooltip.add(t.copy().setStyle(style)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return PLCBlocks.PROGRAM_CABINET_BE.create(pos, state);
    }
}
