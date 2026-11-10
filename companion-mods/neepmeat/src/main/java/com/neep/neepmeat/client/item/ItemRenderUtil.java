package com.neep.neepmeat.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.mixin.client.HeldItemRendererAccessor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ItemRenderUtil
{
    public static float getEquipProgress(ItemInHandRenderer renderer, boolean offHand, float tickDelta)
    {
        HeldItemRendererAccessor accessor = (HeldItemRendererAccessor) renderer;

        if (!offHand)
            return 1.0F - Mth.lerp(tickDelta, accessor.getPrevEquipProgressMainHand(), accessor.getEquipProgressMainHand());
        else
            return 1.0F - Mth.lerp(tickDelta, accessor.getPrevEquipProgressOffHand(), accessor.getEquipProgressOffHand());
    }

    public static void applyEquipOffset(PoseStack matrices, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    /// Applies the Vanilla item equip animation in a controlled and transparent manner.
    /// Remember:
    /// matrices.pop() // Get rid of the model transform
    /// matrices.pop() // Get rid of the Vanilla equip animation
    /// matrices.push() // Empty
    /// matrices.push() // Empty
    public static float defaultItemEquipAnimation(PoseStack matrices, Minecraft client, boolean leftHanded)
    {
        float equipProgress = 0;
        if (!client.player.swinging)
            equipProgress = ItemRenderUtil.getEquipProgress(client.gameRenderer.itemInHandRenderer,
                    leftHanded && client.player.getMainArm() == HumanoidArm.RIGHT, client.getFrameTime());
        ItemRenderUtil.applyEquipOffset(matrices, leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT, equipProgress);

        return equipProgress;
    }
}
