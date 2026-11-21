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

package com.neep.neepmeat.machine.fabricator;

import net.minecraft.core.HolderLookup;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.api.item_network.Craftable;
import com.neep.neepmeat.transport.api.item_network.CraftingPredicate;
import com.neep.neepmeat.transport.api.item_network.RoutablePipe;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.ItemRoute;
import com.neep.neepmeat.transport.item_network.PipeCacheImpl;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.neepmeat.transport.util.SimpleView;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.meatlib.transfer.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class FabricatorBlockEntity extends SyncableBlockEntity implements MotorisedBlock, ExtendedScreenHandlerFactory, RoutablePipe
{
    public static final ResourceLocation CHANNEL_ID = ResourceLocation.parse("fabricator_animation");

    private final ObjectArrayList<BlockApiCache<Storage<ItemVariant>, Direction>> caches = new ObjectArrayList<>(Collections.nCopies(6, null));
    private final FabricatorInventory inventory = new FabricatorInventory();
    private final ImplementedInventory inputInventory = ImplementedInventory.ofSize(2 * 9, this::setChanged);
    private final InventoryStorage inputStorage = InventoryStorage.of(inputInventory, null);
    private final FabricatorStorage storage = new FabricatorStorage();

    public boolean useExternal;
    public boolean useInternalForBOM;

    private float increment;
    private float progress;

    // Use on client only. Immediately set to false by instance.
    public boolean animation;

    public FabricatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        progress = Math.min(5, progress + increment);

        if (progress >= 5)
        {
            progress = 0;

            try
            {
                motorCraft();
            }
            catch (RecipeMatching.FabricatorLoopException ignored)
            {
            }
        }

        return true;
    }

    @Override
    public void setInputPower(float power)
    {
        this.increment = power;
    }

    private void motorCraft() throws RecipeMatching.FabricatorLoopException
    {
        CraftingRecipe recipe = getCurrentRecipe();
        if (recipe != null)
        {
            Direction facing = getBlockState().getValue(FabricatorBlock.FACING);
            List<Storage<ItemVariant>> input = getInput(facing, false);

            // Mutable list of ingredients
            List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
            List<ItemVariant> takenResources = new ArrayList<>();

            try (Transaction transaction = Transaction.openOuter())
            {
                boolean foundAll = RecipeMatching.findMatching(input, ingredients, takenResources, transaction);
                if (!foundAll)
                {
                    transaction.abort();
                    return;
                }

                ItemStack result = recipe.getResultItem(level.registryAccess());

                // ItemVariant::of shouldn't mutate the stack.
                long ejected = ItemPipeUtil.EjectOperation.of(level, worldPosition, facing)
                        .useRouting(true)
                        .stackToAny(ItemVariant.of(result), result.getCount(), transaction)
                        .amount();

                if (ejected != result.getCount())
                {
                    transaction.abort();
                    return;
                }

                sendAnimation();
                transaction.commit();
            }
        }
    }

    private Either<Collection<RecipeMatching.Residual>, ItemStack> craft(int batchSize, boolean bomRequest, TransactionContext transaction) throws RecipeMatching.FabricatorLoopException
    {
        CraftingRecipe recipe = getCurrentRecipe();
        if (recipe != null)
        {
            Direction facing = getBlockState().getValue(FabricatorBlock.FACING);
            List<Storage<ItemVariant>> input = getInput(facing, bomRequest);

            try (Transaction inner = transaction.openNested())
            {
                Collection<RecipeMatching.Residual> residuals = RecipeMatching.findMatchingWithResiduals(batchSize, input, recipe.getIngredients(), inner);

                if (!residuals.isEmpty())
                {
                    inner.abort();
                    return Either.left(residuals);
                }

                ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
                result.setCount(batchSize * result.getCount());

                transaction.addCloseCallback((t, r) ->
                {
                    if (r.wasCommitted())
                        sendAnimation();
                });

                inner.commit();
                return Either.right(result);
            }
        }

        return Either.left(List.of());
    }


    @Override
    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        inventory.writeNbt(nbt);
        storage.writeNbt(nbt);
        nbt.put("input", inputInventory.writeNbt(new CompoundTag()));
        nbt.putBoolean("use_external", useExternal);
        nbt.putBoolean("use_internal_for_bom", useInternalForBOM);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        inventory.readNbt(nbt);
        storage.readNbt(nbt);
        this.inputInventory.readNbt(nbt.getCompound("input"));

        this.useExternal = nbt.getBoolean("use_external");
        this.useInternalForBOM = nbt.getBoolean("use_internal_for_bom");
    }

    private List<Storage<ItemVariant>> getInput(Direction facing, boolean bomRequest)
    {
        if (!useExternal && !(bomRequest && !useInternalForBOM))
        {
            return List.of(inputStorage);
        }

        List<Storage<ItemVariant>> storages = new ArrayList<>();
        storages.add(inputStorage);
        for (Direction direction : Direction.values())
        {
            if (direction == facing.getOpposite() || direction == facing.getClockWise() || direction == facing.getCounterClockWise())
            {
                Storage<ItemVariant> storage = findStorage(direction);
                if (storage != null)
                    storages.add(storage);
            }
        }
        return storages;
    }

    @Nullable
    private Storage<ItemVariant> findStorage(Direction face)
    {
        if (caches.get(face.ordinal()) == null)
        {
            caches.set(face.ordinal(), BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) level, worldPosition.relative(face)));
        }

        return caches.get(face.ordinal()).find(face.getOpposite());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new FabricatorScreenHandler(playerInventory, inventory, inputInventory, syncId, this);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(getBlockPos());
    }

    @Nullable
    public CraftingRecipe getCurrentRecipe()
    {
        return storage.getRecipe();
    }

    public Storage<ItemVariant> getStorage(Direction direction)
    {
        return direction == getBlockState().getValue(FabricatorBlock.FACING) || direction == Direction.DOWN
                ? storage : inputStorage;
    }

    public void sendAnimation()
    {
        if (level instanceof ServerLevel serverWorld)
        {
            PlayerLookup.tracking(this).forEach(p ->
            {
                // Not sure if you can send the same buf to multiple players.
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(getBlockPos());
                ServerPlayNetworking.send(p, CHANNEL_ID, buf);
            });
        }
    }

    // Looks around the block for an item pipe
    @Nullable
    private Pair<BlockPos, Direction> findRequestPos(Direction exclude)
    {
        BlockPos.MutableBlockPos mutable = worldPosition.mutable();
        for (var face : Direction.Plane.HORIZONTAL)
        {
            if (face == exclude)
                continue;

            mutable.setWithOffset(worldPosition, face);
            BlockState state = level.getBlockState(mutable);
            var pipe = ItemPipe.findItemPipe(level, mutable, state);
            if (pipe != null && pipe.isConnected(level, mutable, state, face.getOpposite()))
            {
                return Pair.of(mutable, face.getOpposite());
            }
        }

        return null;
    }

    @Override
    public Result request(Predicate<ItemVariant> predicate, long maxAmount, NodePos fromPos, RoutingNetwork.RequestContext context, TransactionContext transaction)
    {
        storage.load();
        if (!predicate.test(ItemVariant.of(storage.previewStack)))
        {
            // Handle residual items from a different recipe
            if (!storage.bufferedStack.isEmpty() && !storage.previewStack.is(storage.bufferedStack.getItem()))
            {
                MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(ItemVariant.of(storage.bufferedStack), storage.bufferedStack.getCount()));
                storage.bufferedStack = ItemStack.EMPTY;
            }
            return Result.FAIL;
        }

        Direction facing = getBlockState().getValue(FabricatorBlock.FACING);

        IngredientResult result = storage.request(predicate, true, maxAmount, transaction);
        PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();

        RoutingOperation operation = t ->
        {
            // Immediately route immediate items
            long immediateRouted = 0;
            if (result.amount() > 0)
            {
                ItemVariant extractable = ItemVariant.of(storage.previewStack);

                // Check storage before finding a route
                ItemRoute route = itemNetwork.findPath(new ResourceAmount<>(extractable, result.amount()), worldPosition.relative(facing), fromPos.pos(), fromPos.face());

                immediateRouted = itemNetwork.route(worldPosition, facing, route, extractable, result.amount(), t);
            }

            if (result.deferredAmount() > 0 || !result.residuals().isEmpty())
            {
                var requestPos = findRequestPos(facing);

                // If there's no way of requesting the extra ingredients, we can't do anything.
                if (requestPos == null)
                    return 0;

                if (level.getBlockEntity(requestPos.first()) instanceof ItemPipeBlockEntity ipbe)
                {
                    var network = ipbe.getCache().getNetwork();
                    if (network != null)
                    {
                        for (RecipeMatching.Residual residual : result.residuals())
                        {
                            if (!network.request(
                                    CraftingPredicate.ingredient(residual.ingredient), residual.residual,
                                    requestPos.first(), requestPos.second(), RoutingNetwork.RequestType.EXACT_AMOUNT, context, t))
                                return 0;
                        }
                    }
                }
            }

            return immediateRouted;
        };


//        return new Result(operation, immediateRouted, result.deferredAmount(), result.deferredResult());
        return null;
    }

    @Override
    public long request2(Predicate<ItemVariant> predicate, long amount, NodePos dest, TransactionContext transaction)
    {
        // Extract real items from the buffer
        ItemVariant extractable = ItemVariant.of(storage.bufferedStack);
        if (!storage.bufferedStack.isEmpty() && predicate.test(extractable))
        {
            int extractableAmount = Math.min(storage.bufferedStack.getCount(), (int) amount);
            if (extractableAmount >= 0 && extractableAmount == amount)
            {
                storage.bufferedStack.shrink(extractableAmount);

                PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
                Direction facing = getBlockState().getValue(FabricatorBlock.FACING);

                // Check storage before finding a route
                ItemRoute route = itemNetwork.findPath(new ResourceAmount<>(extractable, amount),
                        worldPosition.relative(facing), dest.pos(), dest.face());
                long routed = itemNetwork.route(worldPosition, facing, route, extractable, amount, transaction);

                if (routed < amount)
                    MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(extractable, amount - routed));

                return amount;
            }
        }

        return 0;
    }

    @Override
    public DeferredResult deferredUpdate(RoutingNetwork.DeferredRequest request, RoutingNetwork network, TransactionContext transaction)
    {
        storage.load();
        if (!request.getItemVariant().matches(storage.previewStack))
            return DeferredResult.FAIL;

        long remaining = request.getRequested();
        ItemVariant buffered = ItemVariant.of(storage.bufferedStack);
        if (!buffered.isBlank() && request.getItemVariant().equals(buffered))
        {
            remaining -= storage.bufferedStack.getCount();
        }

        try
        {
            var craftResult = craft((int) Math.ceil(remaining / (double) storage.previewStack.getCount()), false, transaction);
            if (craftResult.right().isPresent())
            {
                // Important: this occurs outside transaction scope.
                storage.appendBuffered(craftResult.right().get());
            }

            if (storage.bufferedStack.getCount() >= request.getRequested())
            {
                buffered = ItemVariant.of(storage.bufferedStack);
                long amount = request.getRequested();

                storage.bufferedStack.shrink((int) request.getRequested());

                PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
                Direction facing = getBlockState().getValue(FabricatorBlock.FACING);

                // Check storage before finding a route
                ItemRoute route = itemNetwork.findPath(new ResourceAmount<>(buffered, amount),
                        worldPosition.relative(facing), request.requesterPos, request.requesterFace);
                long routed = itemNetwork.route(worldPosition, facing, route, buffered, amount, transaction);

                if (routed < amount)
                    MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(buffered, amount - routed));

                return DeferredResult.SUCCESS;
            }
            else
            {
                return DeferredResult.PASS;
            }
        }
        catch (RecipeMatching.FabricatorLoopException e)
        {
            return DeferredResult.FAIL;
        }
    }

    @Override
    public NodePos requestDestPosDir()
    {
        Direction facing = getBlockState().getValue(FabricatorBlock.FACING);

        return new NodePos(worldPosition.relative(facing), facing.getOpposite());
    }

    @Override
    public Stream<StorageView<ItemVariant>> getAvailable(TransactionContext transaction)
    {
        if (!storage.bufferedStack.isEmpty())
            return Stream.of(new SimpleView(new ResourceAmount<>(ItemVariant.of(storage.bufferedStack), storage.bufferedStack.getCount())));

        return Stream.empty();
//        return Streams.stream(storage);
    }

    @Override
    public Stream<Craftable> getCraftable(TransactionContext transaction)
    {
        storage.load();
        if (storage.recipe != null)
        {
            var result = new ResourceAmount<>(ItemVariant.of(storage.previewStack), storage.previewStack.getCount());

            return Stream.of(
                    new Craftable()
                    {
                        @Override
                        public ResourceAmount<ItemVariant> result()
                        {
                            return result;
                        }

                        @Override
                        public Dependencies getDependencies(long amount, TransactionContext transaction)
                        {
                            storage.load();
                            if (!storage.previewStack.isEmpty())
                            {
                                try (var inner = transaction.openNested())
                                {
                                    int batchSize = Mth.positiveCeilDiv((int) amount, (int) result.amount());
                                    var canCraft = craft(batchSize, true, inner);
                                    return canCraft.map(res -> new Dependencies(
                                            res.stream().map(r -> new Dependency(CraftingPredicate.ingredient(r.ingredient), r.residual)).toList(),
                                                    batchSize * result.amount() - amount, batchSize),
                                            stack -> new Dependencies(List.of(), 0, batchSize)
                                    );
                                }
                                catch (RecipeMatching.FabricatorLoopException ignored) { }
                            }

                            return null;
                        }

                        @Override
                        public RoutablePipe underlying()
                        {
                            return FabricatorBlockEntity.this;
                        }
                    });
        }

        return Stream.empty();
    }

    @Override
    public BlockPos getRoutablePipePos()
    {
        return worldPosition;
    }

    public FabricatorStorage getStorage()
    {
        return storage;
    }

    public void setUseExternal(boolean useExternal, boolean useInternalForBOM)
    {
        this.useExternal = useExternal;
        this.useInternalForBOM = useInternalForBOM;
        setChanged();
    }

    public Storage<ItemVariant> getInputStorage()
    {
        return inputStorage;
    }

    public class FabricatorInventory implements ImplementedInventory, CraftingContainer
    {
        private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

        @Override
        public NonNullList<ItemStack> getItems()
        {
            return items;
        }

        @Override
        public void setChanged()
        {
            storage.invalidateRecipe();
            FabricatorBlockEntity.this.setChanged();
        }

        @Override
        public int getWidth()
        {
            return 3;
        }

        @Override
        public int getHeight()
        {
            return 3;
        }

        @Override
        public List<ItemStack> getItems()
        {
            return items;
        }

        @Override
        public void fillStackedContents(StackedContents finder)
        {
            for (ItemStack itemStack : this.items)
            {
                finder.accountSimpleStack(itemStack);
            }
        }
    }

    public class FabricatorStorage extends SnapshotParticipant<ItemStack> implements Storage<ItemVariant>, StorageView<ItemVariant>, NbtSerialisable
    {
        private boolean needsLoading = true;

        private @Nullable net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe> recipe;

        private ItemStack bufferedStack = ItemStack.EMPTY; // Always real items produced from crafting.
        private ItemStack previewStack = ItemStack.EMPTY; // Always a preview of the craft.

        private void updateRecipe()
        {
            recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventory, level).orElse(null);

            if (recipe != null)
            {
                previewStack = recipe.value().getResultItem(level.registryAccess()).copy();
            }
        }

        @Nullable
        public CraftingRecipe getRecipe()
        {
            load();

            return recipe == null ? null : recipe.value();
        }

        @Nullable
        public net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe> recipeHolder()
        {
            load();
            return recipe;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            if (!bufferedStack.isEmpty() && resource.matches(bufferedStack) || resource.matches(previewStack))
            {
                // No recursive crafts for you
                return extractOrCraft(maxAmount, false, transaction).total();
            }
            return 0;
        }

        @Override
        public IngredientResult request(Predicate<ItemVariant> predicate, boolean recursive, long maxAmount, TransactionContext transaction)
        {
            if (!bufferedStack.isEmpty() && predicate.test(ItemVariant.of(bufferedStack)) || predicate.test(ItemVariant.of(previewStack)))
            {
                return extractOrCraft(maxAmount, recursive, transaction);
            }

            return IngredientResult.FAIL;
        }

        private IngredientResult extractOrCraft(long maxAmount, boolean recursive, TransactionContext transaction)
        {
            long requiredExtra = maxAmount - bufferedStack.getCount();

            if (!bufferedStack.isEmpty() && requiredExtra <= 0)
            {
                // Simple case where there are already enough items buffered. No confusion here.
                int amountToExtract = (int) Math.min(bufferedStack.getCount(), maxAmount);
                if (amountToExtract > 0)
                {
                    updateSnapshots(transaction);

                    bufferedStack.shrink(amountToExtract);

                    ItemVariant resultVariant = ItemVariant.of(bufferedStack);
                    return IngredientResult.immediate(amountToExtract);
                }
            }
            else if (requiredExtra > 0)
            {
                // Oh gawd
                updateSnapshots(transaction);

                int remainingCapacity = previewStack.getMaxStackSize() - bufferedStack.getCount();
                int maxBatchSize = Math.min(remainingCapacity / previewStack.getCount(), 64);
                int desiredBatchSize = (int) Math.ceil((double) requiredExtra / previewStack.getCount());

                int batchSize = Math.min(maxBatchSize, desiredBatchSize);

                // An exception seemed the best way to interrupt the recursion without loads of return value checking.
                try
                {
                    var result = craft(batchSize, false, transaction);
                    if (result.right().isPresent())
                    {
                        // Full stack crafted
                        ItemStack newStack = result.right().get();
                        appendBuffered(newStack);
                    }
                    else if (result.left().isPresent() && recursive)
                    {
                        // Immediately return the items from bufferedStack, but submit a request for ingredients.
                        ItemVariant resultVariant = ItemVariant.of(previewStack);
                        int immediate = bufferedStack.getCount();
                        bufferedStack.shrink(immediate);
                        return new IngredientResult(immediate, requiredExtra, resultVariant, result.left().get());
                    }
                    else
                    {
                        // Failure
//                        bufferedStack = ItemStack.EMPTY;

                        // If the batch craft failed, default to the result of a single craft.
                        // This should return a non-zero result when maxAmount = Long.MAX_VALUE as in StorageUtil::findExtractableResource.
                        if (batchSize > 1 && bufferedStack.isEmpty())
                        {
                            bufferedStack = craft(1, false, transaction).right().orElse(ItemStack.EMPTY);
                        }
                    }
                }
                catch (RecipeMatching.FabricatorLoopException ignored)
                {
                    return IngredientResult.FAIL;
                }

                int extractable = (int) Math.min(bufferedStack.getCount(), maxAmount);

                if (extractable > 0)
                {
                    ItemVariant resultVariant = ItemVariant.of(bufferedStack);
                    bufferedStack.shrink(extractable);
                    return IngredientResult.immediate(extractable);
                }

            }

            return IngredientResult.FAIL;
        }

        // Outside transaction scope - no snapshots
        public void appendBuffered(ItemStack newBuffered)
        {
            if (!bufferedStack.isEmpty() && ItemStack.isSameItemSameComponents(bufferedStack, newBuffered))
                bufferedStack.grow(newBuffered.getCount());
            else
                bufferedStack = newBuffered; // This will overwrite the buffer if it can't combine with the new stack, but it will probably be fine.
        }

        @Override
        public boolean supportsInsertion()
        {
            return false;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return inputStorage.insert(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank()
        {
            load();

            return bufferedStack.isEmpty() && previewStack.isEmpty();
        }

        @Override
        public ItemVariant getResource()
        {
            load();

            if (!bufferedStack.isEmpty())
                return ItemVariant.of(bufferedStack);
            else
                return ItemVariant.of(previewStack);
        }

        @Override
        public long getAmount()
        {
            load();

            if (!bufferedStack.isEmpty())
                return bufferedStack.getCount();
            else
                return previewStack.getCount();
        }

        @Override
        public long getCapacity()
        {
            load();

            if (!bufferedStack.isEmpty())
                return bufferedStack.getCount();
            else
                return previewStack.getCount();
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator()
        {
            load();

            return Iterators.singletonIterator(this);
        }

        @Override
        private void load()
        {
            if (needsLoading)
            {
                needsLoading = false;
                updateRecipe();
            }
        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            if (recipe != null)
                nbt.putString("recipe", recipe.id().toString());

            nbt.put("buffered", bufferedStack.save(new CompoundTag()));

            return nbt;
        }

        @Override
        public void readNbt(CompoundTag nbt)
        {
            if (nbt.contains("recipe"))
                this.recipe = MeatlibRecipes.getInstance().getVanilla(RecipeType.CRAFTING, ResourceLocation.tryParse(nbt.getString("recipe"))).orElse(null);
            else
                this.recipe = null;

            this.bufferedStack = ItemStack.of(nbt.getCompound("buffered"));
        }

        @Override
        protected ItemStack createSnapshot()
        {
            return bufferedStack.copy();
        }

        @Override
        protected void readSnapshot(ItemStack snapshot)
        {
            this.bufferedStack = snapshot.copy();
        }

        @Override
        protected void onFinalCommit()
        {
            setChanged();
        }

        public void invalidateRecipe()
        {
            needsLoading = true;
        }

        public ItemStack getBufferedStack()
        {
            return bufferedStack;
        }
    }
}
