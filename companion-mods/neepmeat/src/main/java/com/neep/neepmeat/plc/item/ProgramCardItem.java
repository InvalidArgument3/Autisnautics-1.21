package com.neep.neepmeat.plc.item;
import net.minecraft.world.item.Item;

import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.api.block.PlacementPreview;
import com.neep.neepmeat.component.ProgramStorageComponent;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.screen.ProgramCardScreenHandler;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ProgramCardItem extends BaseItem implements PlacementPreview
{
    public ProgramCardItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();

        ItemStack stack = context.getItemInHand();
        ProgramStorageComponent component = NMComponents.PROGRAM_STORAGE.get(stack);

        if (world.getBlockEntity(pos) instanceof PLCBlockEntity plc)
        {
            if (!world.isClientSide())
            {
                var editor = plc.getProgramEditor();
                if (!context.getPlayer().isShiftKeyDown())
                {
                    component.setSource(editor.getSource(), editor.getProgram());
                }
                else
                {
                    String source = component.getSource();
                    plc.getProgramEditor().setProgramSource(source);
                    editor.setProgram(component.getProgram(plc::getLevel));
                }
                world.playSound(null, pos, MeatlibSounds.UI_BEEP, SoundSource.BLOCKS);
            }
            return InteractionResult.SUCCESS;
        }
        else
        {
            if (!world.isClientSide())
            {
                context.getPlayer().openMenu((SimpleScreenHandlerFactory) (s, i, p) ->
                        new ProgramCardScreenHandler(i, s, component));
            }

            return InteractionResult.sidedSuccess(true);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);
        ProgramStorageComponent component = NMComponents.PROGRAM_STORAGE.getNullable(stack);
        if (component != null && !world.isClientSide())
        {
            user.openMenu((SimpleScreenHandlerFactory) (s, i, p) ->
                    new ProgramCardScreenHandler(i, s, component));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        var component = NMComponents.PROGRAM_STORAGE.get(stack);
        if (component.isEmpty())
            return Component.translatable(getDescriptionId() + ".empty");

        String name = component.getName();
        if (name.isEmpty())
            name = "Unnamed Program";

        return Component.literal(name).setStyle(Style.EMPTY.withItalic(true));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
        tooltip.add(Component.nullToEmpty("Symbols:"));

        Style style = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);

        var component = NMComponents.PROGRAM_STORAGE.get(itemStack);
        if (!component.isEmpty())
        {
            for (var entry : component.getProgram(() -> world).exportedSymbols().entrySet())
            {
                tooltip.add(Component.literal(" " + entry.getKey()).setStyle(style));
            }
        }
    }
}
