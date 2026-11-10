package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.auto.Panel;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;

public class ProcessCardEmiScreenDragDropHandler implements EmiDragDropHandler<ProcessCardScreen>
{
    @Override
    public boolean dropStack(ProcessCardScreen screen, EmiIngredient stack, int x, int y)
    {
        Panel.traverse(screen, x, y, o ->
        {
            if (o instanceof ProcessCardScreen.ItemSlotWidget widget)
            {
                widget.setStack(stack.getEmiStacks().iterator().next().getItemStack());
                return true;
            }
            return false;
        });
        return false;
    }
}
