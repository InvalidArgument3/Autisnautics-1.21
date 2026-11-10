package com.neep.neepmeat.transport.block.item_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.block.FlexStorageBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.FlexSiloBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FlexSiloBlock extends FlexStorageBlock<FlexSiloBlockEntity>
{
    private final Supplier<BlockEntityType<FlexSiloBlockEntity>> typeSupplier;
    public final Capacity capacity = new Capacity(16, 256);

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public enum Part implements StringRepresentable
    {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top"),
        NONE("none")
        ;

        private final String name;

        Part(String name)
        {
            this.name = name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }
    }

    public FlexSiloBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings, Supplier<BlockEntityType<FlexSiloBlockEntity>> typeSupplier)
    {
        super(ctx, typeSupplier, itemSettings, settings);
        this.typeSupplier = typeSupplier;
    }

    @Override
    protected void onRootDestroyed(FlexSiloBlockEntity be)
    {
        be.dumpItems();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.getItemInHand(hand).isEmpty())
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FlexSiloBlockEntity be)
            {
                int totalSlots = 0;
                int slotsUsed = 0;

                for (var view : be.getStorage(null))
                {
                    totalSlots++;
                    if (!view.isResourceBlank())
                        slotsUsed++;
                }

                world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
                player.displayClientMessage(Component.literal("Slots used: %s / %s".formatted(slotsUsed, totalSlots)), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        var state = super.getStateForPlacement(ctx);

        if (state == null)
            return null;

        var world = ctx.getLevel();
        var pos = ctx.getClickedPos();

        boolean up = world.getBlockState(pos.above()).is(this);
        boolean down = world.getBlockState(pos.below()).is(this);

        return state.setValue(PART, thing(up, down));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        boolean up = world.getBlockState(pos.above()).is(this);
        boolean down = world.getBlockState(pos.below()).is(this);

        return state.setValue(PART, thing(up, down));
    }

    private static Part thing(boolean up, boolean down)
    {
        if (up && down)
            return Part.MIDDLE;
        else if (!up && !down)
            return Part.NONE;
        else if (up)
            return Part.BOTTOM;
        else
            return Part.TOP;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    public record Capacity(int types, long stackSize) {}
}
