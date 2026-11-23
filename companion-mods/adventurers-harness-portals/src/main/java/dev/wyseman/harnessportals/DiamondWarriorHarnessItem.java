package dev.wyseman.harnessportals;

import com.google.common.base.Suppliers;
import com.kompot.adventurersharness.harness.HarnessTier;
import com.kompot.adventurersharness.harness.WarriorHarnessItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Supplier;

public class DiamondWarriorHarnessItem extends WarriorHarnessItem {
    private static final Supplier<ItemAttributeModifiers> DIAMOND_MODIFIERS = Suppliers.memoize(() -> {
        ArmorMaterial mat = ArmorMaterials.DIAMOND.value();
        int defense = mat.getDefense(ArmorItem.Type.CHESTPLATE);
        float toughness = mat.toughness();
        ResourceLocation id = ResourceLocation.withDefaultNamespace("armor." + ArmorItem.Type.CHESTPLATE.getName());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ARMOR, new AttributeModifier(id, defense, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        if (mat.knockbackResistance() > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, mat.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        }
        return builder.build();
    });

    public DiamondWarriorHarnessItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public HarnessTier tier() {
        return HarnessTier.DIAMOND;
    }

    @Override
    public Holder<ArmorMaterial> getMaterial() {
        return ArmorMaterials.DIAMOND;
    }

    @Override
    public int getDefense() {
        return ArmorMaterials.DIAMOND.value().getDefense(ArmorItem.Type.CHESTPLATE);
    }

    @Override
    public float getToughness() {
        return ArmorMaterials.DIAMOND.value().toughness();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return DIAMOND_MODIFIERS.get();
    }

    @Override
    public int getEnchantmentValue() {
        return ArmorMaterials.DIAMOND.value().enchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return ArmorMaterials.DIAMOND.value().repairIngredient().get().test(repair);
    }
}
