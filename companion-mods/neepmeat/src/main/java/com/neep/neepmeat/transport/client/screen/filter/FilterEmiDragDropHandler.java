package com.neep.neepmeat.transport.client.screen.filter;

import com.neep.meatlib.client.screen.auto.Panel;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.gui.screens.Screen;

public class FilterEmiDragDropHandler<T extends Screen> implements EmiDragDropHandler<T>
{
    @Override
    public boolean dropStack(T screen, EmiIngredient stack, int x, int y)
    {
        Panel.traverse(screen, x, y, o ->
        {
            if (o instanceof ItemFilterWidget.ItemSlotWidget widget)
            {
                widget.setStack(stack.getEmiStacks().iterator().next().getItemStack());
                return true;
            }
            return false;
        });
        return false;
    }
}
