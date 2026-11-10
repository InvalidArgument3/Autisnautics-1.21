/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons;

import com.neep.meatlib.MeatLibRegistration;
import com.neep.meatlib.attachment.player.PlayerAttachmentManager;
import com.neep.meatlib.config.ConfigUtil;
import com.neep.meatweapons.config.MWConfig;
import com.neep.meatweapons.enchantment.MWEnchantments;
import com.neep.meatweapons.entity.*;
import com.neep.meatweapons.implant.AmmunitionGeneratorImplant;
import com.neep.meatweapons.implant.BloodBulletProviderImplant;
import com.neep.meatweapons.implant.MagazineOrganImplant;
import com.neep.meatweapons.init.MWBlockEntities;
import com.neep.meatweapons.init.MWBlocks;
import com.neep.meatweapons.init.MWScreenHandlers;
import com.neep.meatweapons.init.MWSounds;
import com.neep.meatweapons.item.AssaultDrillItem;
import com.neep.meatweapons.item.GunItem;
import com.neep.meatweapons.meatgun.AmmunitionProvider;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleCache;
import com.neep.meatweapons.meatgun.SimpleItemAmmunitionProvider;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.implant.entity.EntityImplantInstaller;
import com.neep.neepmeat.implant.entity.ImplantRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import com.neep.meatlib.transfer.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.neep.meatweapons.meatgun.AmmunitionProvider.F;

public class MeatWeapons implements ModInitializer
{
    public static final String NAMESPACE = "meatweapons";
    public static final MWConfig CONFIG = ConfigUtil.createDefaults(FabricLoader.getInstance().getConfigDir(), "neep", NAMESPACE, MWConfig.class);

    public static final CreativeModeTab WEAPONS = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + NAMESPACE + ".weapons"))
            .icon(() -> new ItemStack(MWItems.MAGAZINE_ORGAN)).build();

    public static EntityType<BulletEntity> BULLET;
    public static EntityType<CannonBulletEntity> CANNON_BULLET;
    public static EntityType<ZapProjectileEntity> ZAP;
    public static EntityType<FusionBlastEntity> FUSION_BLAST;
    public static EntityType<ExplodingShellEntity> EXPLODING_SHELL;
    public static EntityType<AirtruckEntity> AIRTRUCK;
    public static EntityType<BounceGrenadeEntity> BOUNCE_GRENADE;
    public static EntityType<ShockStaffProjectileEntity> SHOCK_STAFF_PROJECTILE;

    public static <T extends Entity> EntityType<T> registerEntity(String id, FabricEntityTypeBuilder<T> builder)
    {
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NAMESPACE, id),
                                   builder
                        .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                        .trackRangeChunks(4).trackedUpdateRate(10)
                        .build());
    }

    public static <T extends Entity> EntityType<T> registerEntity(String id, EntityType<T> type)
    {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NAMESPACE, id), type);
    }

//    static <T> void resetConfig(TrackedValue<T> v)
//    {
//        v.setValue(v.getDefaultValue(), true);
//    }

    @Override
    public void onInitialize()
    {
        MeatLibRegistration.forContext(MWBlocks.class, MWBlocks.C);
        MeatLibRegistration.forContext(MWItems.class, MWItems.C);

        BULLET = registerEntity("bullet", FabricEntityTypeBuilder.<BulletEntity>create(MobCategory.MISC, BulletEntity::new)
                .dimensions(EntityDimensions.fixed(0.2f, 0.2f))
                .trackRangeChunks(4)
                .forceTrackedVelocityUpdates(true)
                .build());
        CANNON_BULLET = registerEntity("cannon_bullet", FabricEntityTypeBuilder.create(MobCategory.MISC, CannonBulletEntity::new));
        ZAP = registerEntity("zap", FabricEntityTypeBuilder.create(MobCategory.MISC, ZapProjectileEntity::new));
        FUSION_BLAST = registerEntity("fusion_blast", FabricEntityTypeBuilder.create(MobCategory.MISC, FusionBlastEntity::new));
        EXPLODING_SHELL = registerEntity("exploding_shell", FabricEntityTypeBuilder.create(MobCategory.MISC, ExplodingShellEntity::new));
        BOUNCE_GRENADE = registerEntity("bounce_grenade", FabricEntityTypeBuilder.<BounceGrenadeEntity>create(MobCategory.MISC, BounceGrenadeEntity::new)
                .dimensions(EntityDimensions.fixed(0.2f, 0.2f))
                .trackRangeBlocks(200)
                .build()
        );
        SHOCK_STAFF_PROJECTILE = registerEntity("shock_staff_projectile", FabricEntityTypeBuilder.<ShockStaffProjectileEntity>create(MobCategory.MISC, ShockStaffProjectileEntity::new)
                .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                .trackRangeBlocks(200)
                .build()
        );

        AIRTRUCK = registerEntity("airtruck", FabricEntityTypeBuilder.create(MobCategory.MISC, AirtruckEntity::new)
                .trackedUpdateRate(1)
                .forceTrackedVelocityUpdates(true)
                .dimensions(EntityDimensions.fixed(3F, 2.2F))
                .trackRangeBlocks(100)
                .build());

        MWItems.init();
        MWBlockEntities.init();
        MWParticles.init();
        MWGraphicsEffects.init();
        MWAttackC2SPacket.init();
        RootModuleCache.init();
        EntityCritRegistry.initDefaults();

        MWScreenHandlers.init();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(NAMESPACE, "weapons"), WEAPONS);

//        MWEnchantmentTargets.init();
        MWEnchantments.init();

        PlayerAttachmentManager.registerAttachment(WeaponCooldownAttachment.ID, WeaponCooldownAttachment::new);

        FluidStorage.ITEM.registerForItems(AssaultDrillItem::getStorage, MWItems.ASSAULT_DRILL);
        AmmunitionProvider.LOOKUP.registerForItems((itemStack, context) -> new SimpleItemAmmunitionProvider(itemStack, context, AmmunitionType.BALLISTIC, F * MeatWeapons.CONFIG.ammunition.smallBallisticAmount.getRealValue()), MWItems.SMALL_BALLISTIC_MAGAZINE);
        AmmunitionProvider.LOOKUP.registerForItems((itemStack, context) -> new SimpleItemAmmunitionProvider(itemStack, context, AmmunitionType.BALLISTIC, F * MeatWeapons.CONFIG.ammunition.mediumBallisticAmount.getRealValue()), MWItems.MEDIUM_BALLISTIC_MAGAZINE);
        AmmunitionProvider.LOOKUP.registerForItems((itemStack, context) -> new SimpleItemAmmunitionProvider(itemStack, context, AmmunitionType.BALLISTIC, F * MeatWeapons.CONFIG.ammunition.largeBallisticAmount.getRealValue()), MWItems.LARGE_BALLISTIC_MAGAZINE);
        AmmunitionProvider.LOOKUP.registerForItems((itemStack, context) -> new SimpleItemAmmunitionProvider(itemStack, context, AmmunitionType.ENERGY, F * MeatWeapons.CONFIG.ammunition.smallEnergyAmount.getRealValue()), MWItems.SMALL_ENERGY_MAGAZINE);
        AmmunitionProvider.LOOKUP.registerForItems((itemStack, context) -> new SimpleItemAmmunitionProvider(itemStack, context, AmmunitionType.ENERGY, F * MeatWeapons.CONFIG.ammunition.mediumEnergyAmount.getRealValue()), MWItems.MEDIUM_ENERGY_MAGAZINE);

        ImplantRegistry.register(BloodBulletProviderImplant.ID, BloodBulletProviderImplant::new, MWItems.BLOOD_BULLET_PROVIDER);
        ImplantRegistry.register(MagazineOrganImplant.ID, MagazineOrganImplant::new, MWItems.MAGAZINE_ORGAN);
        ImplantRegistry.registerItem(AmmunitionGeneratorImplant.ID, MWItems.AMMUNITION_GENERATOR);

        Registry.register(EntityImplantInstaller.REGISTRY, BloodBulletProviderImplant.ID, MWItems.BLOOD_BULLET_PROVIDER);
        Registry.register(EntityImplantInstaller.REGISTRY, MagazineOrganImplant.ID, MWItems.MAGAZINE_ORGAN);

        MWSounds.init();

        MeatLibRegistration.flush();
    }

    @Nullable
    public static GunItem redirectClicks(ItemStack stack)
    {
        if (stack.getItem() instanceof GunItem gunItem && gunItem.redirectClicks())
        {
            return gunItem;
        }
        return null;
    }
}
