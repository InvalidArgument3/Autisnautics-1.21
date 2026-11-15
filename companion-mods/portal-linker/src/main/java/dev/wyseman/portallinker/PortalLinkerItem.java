package dev.wyseman.portallinker;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class PortalLinkerItem extends Item {
    public PortalLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player) || !(level instanceof ServerLevel server)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        LinkService.Attempt attempt;
        if (player.isShiftKeyDown()) {
            Links.Link link = Links.get(server.getServer()).linkAt(server, context.getClickedPos());
            attempt = link != null ? LinkService.retune(server, context.getClickedPos(), 1) : LinkService.clear(server, stack);
        } else {
            attempt = LinkService.use(player, server, context.getClickedPos(), stack);
        }
        tell(player, attempt);
        if (attempt.success() && "portal_linker.linked".equals(attempt.key())) {
            server.playSound(null, context.getClickedPos(), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.5f, 1.4f);
        } else if (attempt.success() && "portal_linker.orient.updated".equals(attempt.key())) {
            server.playSound(null, context.getClickedPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, 1.4f);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel server)) {
            return InteractionResultHolder.pass(stack);
        }
        LinkService.Attempt attempt = player.isShiftKeyDown()
                ? LinkService.clear(server, stack)
                : LinkService.Attempt.fail("portal_linker.no_frame", Config.maxInterior());
        tell(serverPlayer, attempt);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> lines, TooltipFlag flag) {
        LinkerItems.tooltip(stack, context, lines, flag);
    }

    private static void tell(ServerPlayer player, LinkService.Attempt attempt) {
        player.displayClientMessage(Component.translatable(attempt.key(), attempt.args()), true);
    }
}
