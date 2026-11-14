package com.neep.neepmeat.block;

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ShortBromeBlock extends CropBlock
{
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final IntegerProperty AGE1 = BlockStateProperties.AGE_2;

    protected final BlockItem seedsItem;

    public ShortBromeBlock(RegistrationContext c, Properties settings, ItemSettings itemSettings)
    {
        super(settings);
        this.seedsItem = new BaseBlockItem(this, c, itemSettings);
        c.append(this, seedsItem);
        registerDefaultState(getStateDefinition().any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public static void onPlayerDeath(Player playerEntity)
    {
        Level world = playerEntity.level();
        var random = playerEntity.getRandom();
        Block block = NMBlocks.GRAVE_BROME;

        // 50% chance if the player was not carrying brome
        if (random.nextBoolean() && contains(playerEntity.getInventory(), block.asItem()))
        {
            BlockPos pos = playerEntity.blockPosition();
            BlockPos.randomInCube(random, 2, pos, 1).forEach(p ->
            {
                int age = random.nextInt(2);
                BlockState toPlace = block.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE1, age);
                if (playerEntity.level().getBlockState(p).isAir() && toPlace.canSurvive(playerEntity.level(), p))
                {
                    world.setBlockAndUpdate(p, toPlace);
                }
            });
        }
    }

    private static boolean contains(Inventory inventory, Item item)
    {
        for (int i = 0; i < inventory.getContainerSize(); i++)
        {
            var stack = inventory.getItem(i);
            if (stack.is(item))
                return true;
        }

        return false;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    protected IntegerProperty getAgeProperty()
    {
        return AGE1;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockState floor = world.getBlockState(pos.below());
        return floor.is(BlockTags.DIRT) || floor.is(NMTags.CONTAMINATED_DIRT) || floor.is(IntrusionReactor.ACTIVE_WASTE) || floor.is(Blocks.FARMLAND)
                || floor.is(this) && state.getValue(HALF) == DoubleBlockHalf.UPPER
                && floor.getValue(HALF) == DoubleBlockHalf.LOWER && floor.getValue(AGE1) == 2;
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
    {
        super.playerWillDestroy(world, pos, state, player);
        if (!world.isClientSide())
        {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER)
            {
                BlockState upState = world.getBlockState(pos.above());
                if (upState.is(this) && upState.getValue(HALF) == DoubleBlockHalf.UPPER)
                {
                    world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            else if (player.isCreative())
            {
                BlockState downState = world.getBlockState(pos.below());
                if (downState.is(this) && downState.getValue(HALF) == DoubleBlockHalf.LOWER)
                {
                    world.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state)
    {
        return asItem().getDefaultInstance();
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool)
    {
        if (!world.isClientSide())
        {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
            {
                // Drop loot from the bottom block
                super.playerDestroy(world, player, pos, state.setValue(HALF, DoubleBlockHalf.LOWER), blockEntity, tool);

                // Clean up the bottom block
                BlockState downState = world.getBlockState(pos.below());
                if (downState.is(this) && downState.getValue(HALF) == DoubleBlockHalf.LOWER)
                {
                    world.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }

                return;
            }

            // No need to do anything otherwise
        }

        super.playerDestroy(world, player, pos, state.setValue(HALF, DoubleBlockHalf.LOWER), blockEntity, tool);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
            return;

        if (world.getRawBrightness(pos, 0) >= 9)
        {
            int i = this.getAge(state);
            if (i < this.getMaxAge())
            {
                float f = 3;
                if (random.nextInt((int) (25.0F / f) + 1) == 0)
                {
                    int newAge = i + 1;
                    growTo(world, pos, state, newAge);
                }
            }
        }
    }

    public void growCrops(Level world, BlockPos pos, BlockState state)
    {
        int i = this.getAge(state) + this.getBonemealAgeIncrease(world);
        int j = this.getMaxAge();
        if (i > j)
        {
            i = j;
        }

        growTo(world, pos, state, i);
    }

    protected void growTo(Level world, BlockPos pos, BlockState state, int newAge)
    {
        world.setBlock(pos, this.withAge(newAge, false), Block.UPDATE_CLIENTS);
        if (newAge >= 2)
        {
            BlockPos upPos = pos.above();
            if (world.getBlockState(upPos).isAir())
            {
                world.setBlockAndUpdate(upPos, withAge(newAge, true));
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(AGE1))
        {
            case 0 -> Block.box(2, 0, 2, 14, 8, 14);
            case 1, 2 -> Block.box(2, 0, 2, 14, 16, 14);
            default -> Shapes.block();
        };
    }

    public BlockState withAge(int age, boolean top)
    {
        return this.defaultBlockState().setValue(this.getAgeProperty(), age).setValue(HALF, top ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
    }

    @Override
    public ItemLike getBaseSeedId()
    {
        return seedsItem;
    }

    @Override
    public int getMaxAge()
    {
        return 2;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AGE1);
        builder.add(HALF);
    }
}
