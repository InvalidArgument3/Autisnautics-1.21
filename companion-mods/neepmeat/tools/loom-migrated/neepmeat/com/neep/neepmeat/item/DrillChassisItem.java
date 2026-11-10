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

package com.neep.neepmeat.item;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.meatgun.animation.PistolReloadMeatgunAnimation;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.meatgun.MeatgunAnimationManager;
import com.neep.meatweapons.item.meatgun.MeatgunItem;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.MeatgunModuleCapability;
import com.neep.meatweapons.meatgun.RootModuleCache;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.meatgun.animation.DrillPhageRayHoldMeatgunAnimation;
import com.neep.neepmeat.implant.item.ComplexityUpgradeImplant;
import com.neep.neepmeat.implant.item.ItemImplantManager;
import com.neep.neepmeat.implant.item.PneumaticMotorImplant;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import com.neep.neepmeat.meatgun.module.DrillChassisBaseModule;
import com.neep.neepmeat.meatgun.module.MiningMeatgunModule;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class DrillChassisItem extends MeatgunItem implements OverrideSwingItem, LivingToolApplicable, ClientBlockAttackListener
{
    public static final GlobalChannelManager<BooleanConsumer> ATTACK_CHANNEL = GlobalChannelManager.create(
        ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "drill_chassis"), ChannelFormat.BOOLEAN);

    static
    {
        ATTACK_CHANNEL.receiverHandler(EnvType.SERVER, DrillChassisItem::onAttackPacket);
    }

    private final RootModuleCache cache = new RootModuleCache();

    public DrillChassisItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.stacksTo(1));
        FluidStorage.ITEM.registerForItems(this::getStorage, this);
    }

    private Storage<FluidVariant> getStorage(ItemStack stack, ContainerItemContext containerItemContext)
    {
        return new FoodStorage(containerItemContext);
    }

    @Override
    public ArmPose getThirdPersonArmPose(AbstractClientPlayer player, ItemStack stackInHand, InteractionHand hand)
    {
        return ArmPose.CROSSBOW_HOLD;
    }

    @Override
    public boolean onSwing(ItemStack stack, Player player)
    {
        return false;
//        return !MWComponents.MEATGUN.get(stack).getCore().canRun(player, ReciprocatingDrillHeadModule.METAB_PER_BLOCK);
    }

    @Override
    public boolean setEquipProgess()
    {
        return true;
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public boolean redirectTrigger(ItemStack stack, MeatgunModuleCapability trigger)
    {
//        return false;
        return getHead(stack).getCapabilities().contains(trigger);
    }

    private static BooleanConsumer onAttackPacket(Player playerEntity, FriendlyByteBuf packetByteBuf, PacketSender packetSender)
    {
        return attacking ->
        {
            ItemStack mainStack = playerEntity.getMainHandItem();
            if (mainStack.getItem() instanceof DrillChassisItem)
            {
//                mainStack.getOrCreateNbt().putBoolean("attacking", attacking);
            }
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner)
    {
        if (getHead(stack) instanceof MiningMeatgunModule mining)
        {
            mining.onBlockBroken(world, pos, state, miner, miner.getMainHandItem().equals(stack) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        }
        return true;
    }

    private void sendAttack(boolean attacking)
    {
        ATTACK_CHANNEL.emitter(null).accept(attacking);
    }


    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state)
    {
        if (getHead(stack) instanceof MiningMeatgunModule mining)
        {
            return mining.isSuitableFor(state);
        }
        return super.isSuitableFor(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (getHead(stack) instanceof MiningMeatgunModule mining)
        {
            return mining.getMiningSpeedMultiplier(state);
        }

        return super.getDestroySpeed(stack, state);
    }

    public MeatgunModule getHead(ItemStack stack)
    {
        return ((DrillChassisBaseModule) MWComponents.MEATGUN.get(stack).getRootHolder().root).getHead();
    }

    @Override
    public ItemStack getDefaultInstance()
    {
        ItemStack stack = super.getDefaultInstance();

        var meatgun = MWComponents.MEATGUN.get(stack);
//        createDefaultTree(meatgun.getRootHolder().root, meatgun.getRootHolder().getListener());
//
        var implants = meatgun.getRootHolder().coreImplants();
        implants.installImplant(PneumaticMotorImplant.ID);
        return stack;
    }

    @Override
    public void createDefaultTree(MeatgunModule root, RootModuleHolder.Listener listener)
    {
        if (root instanceof DrillChassisBaseModule baseModule)
        {
            baseModule.getHeadSlot().set(NMMeatgunModules.IRON_ROTARY_DRILL_HEAD.create(listener, root, NMItems.IRON_ROTARY_DRILL_HEAD.getDefaultInstance()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context)
    {
        if (NMComponents.IMPLANT_MANAGER.getNullable(stack) instanceof ItemImplantManager manager)
        {
            tooltip.add(Component.literal("Core: ").append(Component.translatable(manager.getCoreType().toLanguageKey("implant"))).setStyle(
                    Style.EMPTY.withColor(PLCCols.TEXT.col))
            );
        }
        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public void onAttackBlock(ItemStack stack, Player player)
    {
        if (!redirectTrigger(stack, MeatgunModuleCapability.ATTACK))
            sendAttack(true);
    }

    @Override
    public void onFinishAttackBlock(ItemStack stack, Player player)
    {
        sendAttack(false);
    }

    @Override
    public MeatgunModule createBase(RootModuleHolder.Listener listener)
    {
        return new DrillChassisBaseModule(listener);
    }

    @Override
    public Supplier<Object> createAnimationManager()
    {
        return new Supplier<Object>()
        {
            @Override
            public Object get()
            {
                return new MeatgunAnimationManager(MeatgunAnimation.EMPTY)
                        .add("reload", new PistolReloadMeatgunAnimation())
                        .add("phage_ray_hold", new DrillPhageRayHoldMeatgunAnimation())
                        ;
            }
        };
    }

    @Override
    public int getMaxComplexity(ItemStack stack)
    {
        int base = NeepMeat.CONFIG.drillChassisMaxComplexity.getRealValue();
        if (NMComponents.IMPLANT_MANAGER.getNullable(stack) instanceof ItemImplantManager manager)
        {
            return ComplexityUpgradeImplant.getComplexity(base, manager);
        }

        return base;
    }

    @Override
    public float getWorldHeightOffset()
    {
        return -0.2f;
    }

    @Override
    public RootModuleCache getCache()
    {
        return cache;
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return MWComponents.MEATGUN.get(stack).getCore().getResource() == AmmunitionType.METABOLIC;
    }

    @Override
    public boolean supportsAds()
    {
        return false;
    }

    public record FoodStorage(ContainerItemContext context) implements Storage<FluidVariant>
    {
        private ItemStack getStack()
        {
            return context.getItemVariant().toStack((int) context.getAmount());
        }

        @Override
        public long insert(FluidVariant insertedResource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(insertedResource, maxAmount);

            ItemStack stack = getStack();
            long inserted = MWComponents.MEATGUN.get(stack).getCore().acceptResource(insertedResource, maxAmount, transaction);

            // TODO: Fix the meatgun so that enabling this doesn't break everything
            context.exchange(ItemVariant.of(stack), 1, transaction);
            return inserted;
        }

        @Override
        public long extract(FluidVariant extractedResource, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public @NotNull Iterator<StorageView<FluidVariant>> iterator()
        {
            return Collections.emptyIterator();
        }
    }

    @Environment(EnvType.CLIENT)
    public static final class Client
    {
        public static boolean using(ItemStack stack, boolean firstPerson)
        {
            return firstPerson && Minecraft.getInstance().options.keyAttack.isDown();
        }
    }
}
