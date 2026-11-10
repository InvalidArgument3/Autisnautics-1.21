package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.network.chat.Component;

public class NMItemSettings extends ItemSettings
{
    public static NMItemSettings block()
    {
        NMItemSettings settings = new NMItemSettings();
        settings.factory(BaseBlockItem::new);
        return settings;
    }

    public NMItemSettings tooltip(TooltipSupplier supplier)
    {
        super.tooltip(supplier);
        return this;
    }

    public NMItemSettings requiresMotor()
    {
        return tooltip(REQUIRES_MOTOR);
    }

    public NMItemSettings requiresVascular()
    {
        return tooltip(REQUIRES_VASCULAR);
    }

    public NMItemSettings requiresIchor()
    {
        return tooltip(REQUIRES_ICHOR);
    }

    public NMItemSettings neepbus()
    {
        return tooltip(NEEPBUS_PART);
    }

    public NMItemSettings retainsInv()
    {
        return tooltip(RETAINS_INVENTORY);
    }

    public NMItemSettings plc()
    {
        return tooltip(PLC_COMPATIBLE);
    }

    public NMItemSettings plcInterrupt()
    {
        return tooltip(PLC_INTERRUPT);
    }

    public NMItemSettings plcActuator()
    {
        return tooltip(PLC_ACTUATOR);
    }

    public static final TooltipSupplier REQUIRES_MOTOR = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".requires_motor").setStyle(PLCCols.EMPHASIS_TEXT));

    public static final TooltipSupplier REQUIRES_VASCULAR = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".requires_vascular").setStyle(PLCCols.EMPHASIS_TEXT));
    public static final TooltipSupplier REQUIRES_ICHOR = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".requires_ichor").setStyle(PLCCols.EMPHASIS_TEXT));
    public static final TooltipSupplier PLC_COMPATIBLE = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".plc_compatible").setStyle(PLCCols.EMPHASIS_TEXT));
    public static final TooltipSupplier PLC_INTERRUPT = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".plc_interrupt").setStyle(PLCCols.EMPHASIS_TEXT));
    public static final TooltipSupplier PLC_ACTUATOR = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".plc_actuator").setStyle(PLCCols.EMPHASIS_TEXT));

    public static final TooltipSupplier NEEPBUS_PART = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".neepbus_part").setStyle(PLCCols.EMPHASIS_TEXT));
    public static final TooltipSupplier RETAINS_INVENTORY = (stack, tooltip) -> tooltip.add(Component.translatable("tooltip." + NeepMeat.NAMESPACE + ".retains_inventory").setStyle(PLCCols.EMPHASIS_TEXT));
}
