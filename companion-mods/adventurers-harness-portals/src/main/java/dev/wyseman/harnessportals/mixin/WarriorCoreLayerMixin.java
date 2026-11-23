package dev.wyseman.harnessportals.mixin;

import com.kompot.adventurersharness.client.WarriorCoreLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.wyseman.harnessportals.DiamondWarriorHarnessItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WarriorCoreLayer.class)
public abstract class WarriorCoreLayerMixin<T extends LivingEntity> {

    @Shadow
    @Final
    private static ResourceLocation SOCKET;

    @Unique
    private static final ResourceLocation DIAMOND_SOCKET =
            ResourceLocation.fromNamespaceAndPath("adventurers_harness_portals", "textures/models/armor/diamond_warrior_core.png");

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType harnessPortals$diamondSocketTexture(
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (texture.equals(SOCKET) && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DiamondWarriorHarnessItem) {
            return RenderType.armorCutoutNoCull(DIAMOND_SOCKET);
        }
        return RenderType.armorCutoutNoCull(texture);
    }
}
