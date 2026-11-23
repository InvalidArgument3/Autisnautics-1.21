package com.neep.meatweapons.implant;

import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.BoneStealerModule;
import com.neep.neepmeat.implant.item.ItemImplant;
import com.neep.neepmeat.item.ItemImplantItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AmmunitionGeneratorImplant implements ItemImplant
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "ammunition_generator");

    public static void tickGeneration(Player player, RootModuleHolder holder)
    {
        if (!player.level().isClientSide() && player.level().getGameTime() % (2 * 20) == 0)
        {
            if (holder.coreImplants().isInstalled(ID))
            {
                int remaining = holder.moduleCache().getAmmo(AmmunitionType.METABOLIC);
                int consumeAmount = 1;

                if (remaining > consumeAmount)
                {
                    if (BoneStealerModule.insertRandom(holder, 2, true))
                    {
                        holder.getAmmoOrReload(AmmunitionType.METABOLIC, consumeAmount, player.getInventory(), player, null)
                                .commit();
                    }
                }
            }
        }
    }

    public static class Item extends ItemImplantItem
    {
        public Item(RegistrationContext c, String name, int i, ResourceLocation id, MeatlibItemSettings group)
        {
            super(c, name, i, id, group);
        }
    }
}
