package com.neep.neepmeat.transport.item;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.client.MeatlibItemColours;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.transport.screen.ProcessCardScreenHandler;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ProcessCardItem extends BaseItem
{
    public ProcessCardItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);
        var component = NMComponents.PROCESS_STORAGE.getNullable(stack);
        if (component != null && !world.isClientSide())
        {
            user.openMenu((SimpleScreenHandlerFactory) (s, i, p) ->
                    new ProcessCardScreenHandler(i, s, component));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
        var component = NMComponents.PROCESS_STORAGE.getNullable(itemStack);
        if (component != null)
        {
            Style style = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
            tooltip.add(Component.literal("Ingredients: "));
            component.getItems().getItems().stream().filter(s -> !s.isEmpty()).forEach(s ->
                    tooltip.add(Component.literal(" ").append(s.getCount() + " × ").append(s.getHoverName()).setStyle(style)));
        }
    }

    @Override
    public Component getName(ItemStack stack)
    {
        var component = NMComponents.PROCESS_STORAGE.getNullable(stack);
        if (component != null)
        {
            if (!component.getResult().resource().isBlank())
            {
                var style = Style.EMPTY;
                Item item = component.getResult().resource().getItem();
                if (MeatLib.isClient())
                    style = style.withColor(MeatlibItemColours.getTint(item.builtInRegistryHolder().key().location(), true));
                return Component.translatable(getDescriptionId(stack) + ".full", item.getDescription()).setStyle(style);
            }
            else
            {
                return Component.translatable(getDescriptionId(stack));
            }
        }

        return super.getName(stack).copy();
    }
}
