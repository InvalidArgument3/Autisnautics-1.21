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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.config.MWConfig;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

import static com.neep.meatweapons.MeatWeapons.CONFIG;

public class MeatgunModules
{
    private static final MWConfig.Meatgun C = CONFIG.meatgun;

    public static ResourceKey<Registry<MeatgunModule.Type<? extends MeatgunModule>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "meatgun_module"));
    public static final DefaultedRegistry<MeatgunModule.Type<? extends MeatgunModule>> REGISTRY = FabricRegistryBuilder.createDefaulted(REGISTRY_KEY, MeatgunModule.DEFAULT_ID).buildAndRegister();

    public static final MeatgunModule.Type<RawGunBaseModule> BASIC_PISTOL_BASE = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "basic_pistol_base"), (l, p, stack) -> new RawGunBaseModule(l), RawGunBaseModule::new);
    public static final MeatgunModule.Type<BasePistolModule> BASE_PISTOL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "base_pistol"), (l, p, stack) -> new BasePistolModule(l), BasePistolModule::fromNbt);
    public static final MeatgunModule.Type<BaseStaffModule> BASE_STAFF = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "base_staff"), (l, p, stack) -> new BaseStaffModule(l), BaseStaffModule::fromNbt);
    public static final MeatgunModule.Type<BaseLongGunModule> BASE_LONG_GUN = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "base_long_gun"), (l, p, stack) -> new BaseLongGunModule(l), BaseLongGunModule::fromNbt);

    public static final MeatgunModule.Type<PistolModule> PISTOL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "pistol"),
            new AttributeContainer().complexity(4).capacity(C.pistolCapacity.getRealValue()).cooldown(10).attackDamage(C.pistolAttackDamage.getRealValue()).consume(1), (l, p, stack) -> new PistolModule(l), PistolModule::new);
    public static final MeatgunModule.Type<ChuggerModule> CHUGGER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "chugger"),
            new AttributeContainer().complexity(6).capacity(C.chuggerCapacity.value()).cooldown(15).consume(2).attackDamage(C.chuggerAttackDamage.getRealValue()), (l, p, stack) -> new ChuggerModule(l), ChuggerModule::new);
    public static final MeatgunModule.Type<BosherModule> BOSHER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "bosher"),
            new AttributeContainer().complexity(7).capacity(C.bosherCapacity.value()).cooldown(20).consume(4).attackDamage(C.bosherAttackDamage.getRealValue()), (l, p, stack) -> new BosherModule(l), BosherModule::new);
    public static final MeatgunModule.Type<LongBoiModule> LONG_BOI = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "long_boi"),
            new AttributeContainer().complexity(10).capacity(C.longBoiCapacity.value()).cooldown(30).consume(12).attackDamage(C.longBoiBaseDamage.getRealValue()), (l, p, stack) -> new LongBoiModule(l), LongBoiModule::new);
    public static final MeatgunModule.Type<GrenadeLauncherModule> GRENADE_LAUNCHER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "grenade_launcher"),
            new AttributeContainer().complexity(6).capacity(C.grenadeLauncherCapacity.getRealValue()).cooldown(15).consume(3), (l, p, stack) -> new GrenadeLauncherModule(l), GrenadeLauncherModule::new);
    public static final MeatgunModule.Type<BloodthrowerModule> BLOODTHROWER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "bloodthrower"),
            new AttributeContainer().complexity(8).capacity(C.bloodThrowerCapacity.getRealValue()).cooldown(2).consume(1).attackDamage(C.bloodThrowerDamage.getRealValue()), (l, p, stack) -> new BloodthrowerModule(l), BloodthrowerModule::new);
    public static final MeatgunModule.Type<ShortPhageRayModule> SHORT_PHAGE_RAY = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "short_phage_ray"),
            new AttributeContainer().complexity(8).capacity(16).consume(1), (l, p, stack) -> new ShortPhageRayModule(l, stack), ShortPhageRayModule::new);
    public static final MeatgunModule.Type<TripleCarouselModule> TRIPLE_CAROUSEL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "triple_carousel"),
            new AttributeContainer().complexity(2), (l, p, stack) -> new TripleCarouselModule(l), TripleCarouselModule::new);
    public static final MeatgunModule.Type<DoubleCarouselModule> DOUBLE_CAROUSEL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "double_carousel"),
            new AttributeContainer().complexity(2), (l, p, stack) -> new DoubleCarouselModule(l), DoubleCarouselModule::new);
    public static final MeatgunModule.Type<UnderbarrelModule> UNDERBARREL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "underbarrel"),
            new AttributeContainer().complexity(1), (l, p, stack) -> new UnderbarrelModule(l), UnderbarrelModule::new);

    public static final MeatgunModule.Type<BatteryModule> BATTERY = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "battery"),
            new AttributeContainer().complexity(3).capacity(C.batteryCapacity.getRealValue()), (l, p, stack) -> new BatteryModule(l), BatteryModule::new);
    public static final MeatgunModule.Type<HomingModule> HOMING_BRAIN = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "homing_brain"),
            new AttributeContainer().complexity(4), (l, p, stack) -> new HomingModule(l), HomingModule::new);
    public static final MeatgunModule.Type<BoneStealerModule> BONE_STEALER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "bone_stealer"),
            new AttributeContainer().complexity(4), (l, p, stack) -> new BoneStealerModule(l), (l, n) -> new BoneStealerModule(l));
    public static final MeatgunModule.Type<SlowBarrelModule> SLOW_BARREL = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "slow_barrel"),
            new AttributeContainer().applicable(SlotType.BARREL).complexity(1), (l, p, stack) -> new SlowBarrelModule(l), SlowBarrelModule::new);
    public static final MeatgunModule.Type<BasicStockModule> BASIC_STOCK = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "basic_stock"),
            new AttributeContainer().applicable(SlotType.STOCK).complexity(1), (l, p, stack) -> new BasicStockModule(l), BasicStockModule::new);
    public static final MeatgunModule.Type<SpeedModifierModule> PHAGE_RAY_SPEED_MODIFIER = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "phage_ray_speed_modifier"),
            new AttributeContainer().complexity(2), (l, p, stack) -> new SpeedModifierModule(l), SpeedModifierModule::new);

    public static final MeatgunModule.Type<?> X2_FIRING_MECHANISM = makeFiringMechanism(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "x2_firing_mechanism"),
            new AttributeContainer().complexity(1).applicable(SlotType.FIRING_MECHANISM), () -> MeatgunModules.X2_FIRING_MECHANISM, 2);
    public static final MeatgunModule.Type<?> X8_FIRING_MECHANISM = makeFiringMechanism(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "x8_firing_mechanism"),
            new AttributeContainer().complexity(1).applicable(SlotType.FIRING_MECHANISM), () -> MeatgunModules.X8_FIRING_MECHANISM, 8);

    public static final MeatgunModule.Type<HalberdModule> HALBERD = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "halberd"),
            new AttributeContainer().complexity(6).attackDamage(C.halberdDamage.getRealValue()), (l, p, stack) -> new HalberdModule(l), HalberdModule::new);
    public static final MeatgunModule.Type<ShockStaffModule> SHOCK_STAFF = register(ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "shock_staff"),
            new AttributeContainer().complexity(6).capacity(C.shockStaffCapacity.getRealValue()).attackDamage(C.shockStaffDamage.getRealValue()), (l, p, stack) -> new ShockStaffModule(l), ShockStaffModule::new);

    private static MeatgunModule.Type<?> makeFiringMechanism(ResourceLocation id, AttributeContainer attributes, Supplier<MeatgunModule.Type<?>> typeSupplier, int overclock)
    {
        class C extends FiringMechanismModule
        {
            public C(RootModuleHolder.Listener listener)
            {
                super(listener);
            }

            @Override
            public Type<? extends MeatgunModule> getType()
            {
                return typeSupplier.get();
            }

            @Override
            public float getOverclock()
            {
                return overclock;
            }
        }

        var type = new MeatgunModule.Type<>(attributes, (listener, parent, stack) -> new C(listener), (l, n) -> new C(l));
        return register(id, type);
    }

    public static <T extends MeatgunModule> MeatgunModule.Type<T> register(ResourceLocation id, AttributeContainer attributes, MeatgunModule.Factory<T> factory, MeatgunModule.NbtFactory<T> nbtFactory)
    {
        return Registry.register(REGISTRY, id, new MeatgunModule.Type<>(attributes, factory, nbtFactory));
    }

    public static <T extends MeatgunModule, R extends MeatgunModule.Type<T>> R register(ResourceLocation id, R type)
    {
        return Registry.register(REGISTRY, id, type);
    }

    public static <T extends MeatgunModule> MeatgunModule.Type<T> register(ResourceLocation id, MeatgunModule.Factory<T> factory, MeatgunModule.NbtFactory<T> nbtFactory)
    {
        return Registry.register(REGISTRY, id, new MeatgunModule.Type<>(factory, nbtFactory));
    }

    static
    {
        Registry.register(REGISTRY, REGISTRY.getDefaultKey(), MeatgunModule.DEFAULT_TYPE);
    }

}
