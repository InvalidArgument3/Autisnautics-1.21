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
        return (NMItemSettings) new NMItemSettings().factory(BaseBlockItem::new);
    }

    public NMItemSettings tooltip(TooltipSupplier tooltipSupplier)
    {
        super.tooltip(tooltipSupplier);
        return this;
    }

    public NMItemSettings requiresMotor()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, REQUIRES_MOTOR);
        return this;
    }

    public NMItemSettings requiresVascular()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, REQUIRES_VASCULAR);
        return this;
    }

    public NMItemSettings requiresIchor()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, REQUIRES_ICHOR);
        return this;
    }

    public NMItemSettings neepbus()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, NEEPBUS_PART);
        return this;
    }

    public NMItemSettings retainsInv()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, RETAINS_INVENTORY);
        return this;
    }

    public NMItemSettings plc()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, PLC_COMPATIBLE);
        return this;
    }

    public NMItemSettings plcInterrupt()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, PLC_INTERRUPT);
        return this;
    }

    public NMItemSettings plcActuator()
    {
        this.tooltipSupplier = TooltipSupplier.combine(tooltipSupplier, PLC_ACTUATOR);
        return this;
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
