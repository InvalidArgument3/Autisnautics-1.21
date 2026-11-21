package com.neep.meatweapons.meatgun.module;

import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.mixin.client.LivingEntityAccessor;
import com.neep.neepmeat.util.IterateRandomly;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class BoneStealerModule extends AbstractMeatgunModule
{
    public BoneStealerModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    public static void apply(LivingEntity shooter, Entity entity, float damage)
    {
        RandomSource random = shooter.getRandom();

        if (entity instanceof LivingEntity living)
        {
            // Use damage taken rather than damage dealt to account for armour and other things
            float damageTaken = ((LivingEntityAccessor) living).getLastDamageTaken();

            // If damageTaken > damage, this damage event could not be responsible
            if (damageTaken > 0 && damage >= damageTaken)
            {
                Level world = shooter.level();
                int time = (int) (2 + damageTaken);

                // Don't spawn the particles every time for very low damage
                if (time >= 6 || random.nextFloat() < 0.4)
                {
                    PlayerLookup.around((ServerLevel) world, shooter.position(), 20).forEach(p ->
                    {
                        var buf = PacketByteBufs.create();
                        buf.writeVarInt(entity.getId());
                        buf.writeVarInt(shooter.getId());
                        GraphicsEffects.CHANNEL.emitter(p).apply(world.dimension(),
                                MWGraphicsEffects.BONE_STEALER,
                                entity.position(),
                                Vec3.ZERO,
                                Vec3.ZERO,
                                1,
                                time,
                                buf);
                    });
                }

                if (random.nextFloat() <= 0.4f)
                {
                   living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 1));
                }

                // Only add ammo to players
                if (shooter instanceof Player player)
                {
                    // Adjust for amount.
                    // Should be fine for the moment as ballistic and energy have the same health per unit
                    // TODO: use the amount for the specific type
                    int amount = (int) (Math.ceil(damageTaken) / AmmunitionType.BALLISTIC.healthPerUnit)
                            / 4;

                    // Hotbar slots are between 0 and 9 exclusive
                    for (int i : IterateRandomly.of(Inventory.getSelectionSize() + 1))
                    {
                        ItemStack stack;
                        if (i == 9)
                            stack = player.getInventory().offhand.get(0);
                        else
                            stack = player.getInventory().getItem(i);

                        var c = MWComponents.MEATGUN.getNullable(stack);
                        if (c != null)
                        {
                            if (insertRandom(c.getRootHolder(), amount, true))
                                break;
                        }
                    }
                }
            }
        }
    }

    public static boolean insertRandom(RootModuleHolder holder, int amount, boolean excludeMetabolic)
    {
        var supported = holder.getSupportedAmmo();
        List<AmmunitionType> types = new ObjectArrayList<>(supported);

        for (int i : IterateRandomly.of(types.size()))
        {
            var type = types.get(i);

            if (excludeMetabolic && type == AmmunitionType.METABOLIC)
                continue;

            int inserted = holder.insertAmmo(type, amount);
            if (inserted != 0)
            {
                holder.markDirty(RootModuleHolder.Reason.SAVE_DATA);
                return true;
            }
        }

        return false;
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BONE_STEALER;
    }
}
