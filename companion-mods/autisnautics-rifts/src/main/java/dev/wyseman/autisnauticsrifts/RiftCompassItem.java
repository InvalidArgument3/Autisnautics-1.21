package dev.wyseman.autisnauticsrifts;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RiftCompassItem extends Item {
    public RiftCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if ((serverLevel.getGameTime() + slotId) % 20L != 0L) {
            return;
        }
        RiftTarget current = stack.getOrDefault(AutisnauticsRifts.RIFT_TARGET.get(), RiftTarget.EMPTY);
        RiftTarget updated = RiftLocator.findNearestRift(serverLevel, player.blockPosition(), current.ceilingMode());
        if (!updated.equals(current)) {
            stack.set(AutisnauticsRifts.RIFT_TARGET.get(), updated);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        RiftTarget current = stack.getOrDefault(AutisnauticsRifts.RIFT_TARGET.get(), RiftTarget.EMPTY);
        String dimId = serverLevel.dimension().location().toString();
        boolean nextCeiling = RiftBoundary.hasBoth(dimId) ? !current.ceilingMode() : current.ceilingMode();
        RiftTarget updated = RiftLocator.findNearestRift(serverLevel, player.blockPosition(), nextCeiling);
        stack.set(AutisnauticsRifts.RIFT_TARGET.get(), updated);

        RiftBoundary.Face face = RiftBoundary.getFace(dimId, updated.ceilingMode());
        if (face == null || !updated.found()) {
            player.displayClientMessage(
                Component.literal("No dimensional rift detected in this dimension.").withStyle(ChatFormatting.GRAY),
                true
            );
        } else {
            double dx = updated.targetX() - player.getX();
            double dz = updated.targetZ() - player.getZ();
            int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            String dir = face.isCeiling() ? "Ceiling Rift (↑ " : "Floor Rift (↓ ";
            player.displayClientMessage(
                Component.literal(dir + face.neighborName() + "): ")
                    .withStyle(face.isCeiling() ? ChatFormatting.AQUA : ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal("X " + updated.targetX() + ", Z " + updated.targetZ() + " (" + dist + "m)")
                        .withStyle(ChatFormatting.WHITE)),
                true
            );
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, face.isCeiling() ? 1.25F : 0.9F);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        RiftTarget target = stack.get(AutisnauticsRifts.RIFT_TARGET.get());
        return target != null && target.found();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RiftTarget target = stack.getOrDefault(AutisnauticsRifts.RIFT_TARGET.get(), RiftTarget.EMPTY);
        tooltip.add(Component.literal("Mode: " + (target.ceilingMode() ? "Ceiling Rift (Ascend ↑)" : "Floor Rift (Descend ↓)"))
            .withStyle(target.ceilingMode() ? ChatFormatting.AQUA : ChatFormatting.LIGHT_PURPLE));
        if (target.found()) {
            tooltip.add(Component.literal("Target: X " + target.targetX() + ", Z " + target.targetZ())
                .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("No rift locked in current dimension")
                .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal("Right-click to toggle Floor / Ceiling & ping distance")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}
