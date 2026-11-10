package com.neep.neepmeat.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.neep.neepmeat.entity.weapon_switching.WeaponSwitchingHostileEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class GeneralBipedEntityModel<T extends Mob> extends HumanoidModel<T>
{
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;

    public GeneralBipedEntityModel(ModelPart root)
    {
        super(root);
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve = root.getChild("right_sleeve");
        this.leftPants = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
        this.jacket = root.getChild("jacket");
//        this.parts = (List)root.traverse().filter((part) -> !part.isEmpty()).collect(ImmutableList.toImmutableList());
    }

    public static MeshDefinition createMesh(CubeDeformation dilation, float pivotOffsetY)
    {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), PartPose.offset(0.0F, 0.0F + pivotOffsetY, 0.0F));
        modelPartData.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.extend(0.5F)), PartPose.offset(0.0F, 0.0F + pivotOffsetY, 0.0F));
        modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), PartPose.offset(0.0F, 0.0F + pivotOffsetY, 0.0F));
        modelPartData.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), PartPose.offset(-5.0F, 2.0F + pivotOffsetY, 0.0F));
//        modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 2.0F + pivotOffsetY, 0.0F));
        modelPartData.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), PartPose.offset(5.0F, 2.0F, 0.0F));
        modelPartData.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), PartPose.offset(-1.9F, 12.0F + pivotOffsetY, 0.0F));
        modelPartData.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), PartPose.offset(1.9F, 12.0F + pivotOffsetY, 0.0F));

        modelPartData.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.extend(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        modelPartData.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.extend(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        modelPartData.addOrReplaceChild(
                "left_pants",
                CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.extend(0.25F)),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        modelPartData.addOrReplaceChild(
                "right_pants",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.extend(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        modelPartData.addOrReplaceChild(
                PartNames.JACKET, CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation.extend(0.25F)), PartPose.ZERO
        );
        return modelData;
    }

    public void setAngles(T livingEntity, float f, float g, float h, float i, float j)
    {
        super.setupAnim(livingEntity, f, g, h, i, j);
        if (livingEntity instanceof WeaponSwitchingHostileEntity e)
            animateGunPose(e, this.leftArm, this.rightArm, f, g);

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
//        if (livingEntity.getEquippedStack(EquipmentSlot.CHEST).isEmpty())
//        {
//            if (livingEntity.isInSneakingPose())
//            {
//                this.cloak.pivotZ = 1.4F;
//                this.cloak.pivotY = 1.85F;
//            }
//            else
//            {
//                this.cloak.pivotZ = 0.0F;
//                this.cloak.pivotY = 0.0F;
//            }
//        }
//        else if (livingEntity.isInSneakingPose())
//        {
//            this.cloak.pivotZ = 0.3F;
//            this.cloak.pivotY = 0.8F;
//        }
//        else
//        {
//            this.cloak.pivotZ = -1.1F;
//            this.cloak.pivotY = -0.85F;
//        }

    }

    public static void animateGunPose(WeaponSwitchingHostileEntity entity, ModelPart leftArm, ModelPart rightArm, float f, float g)
    {
        if (entity.getWeaponMode() == WeaponSwitchingHostileEntity.WeaponType.RANGED)
        {
//            boolean attacking = true;
//            float swingProgress = 0;
//            float f = MathHelper.sin(swingProgress * (float)Math.PI);
//            float g = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float)Math.PI);

//            rightArm.pitch = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 2.0F * g * 0.5F / k;
//            leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;

            entity.smoothAimProgress = Mth.lerp(0.1f, entity.smoothAimProgress, entity.getAimProgress());
            float ap = Mth.clamp(entity.smoothAimProgress, 0f, 1f);
            rightArm.xRot = Mth.lerp(ap, rightArm.xRot, -Mth.HALF_PI * ap);
            leftArm.yRot = 20 * Mth.DEG_TO_RAD * ap;

            leftArm.xRot = Mth.lerp(ap, leftArm.xRot, -86 * Mth.DEG_TO_RAD * ap);
            leftArm.yRot = 50 * Mth.DEG_TO_RAD * ap;
        }
    }

    protected Iterable<ModelPart> bodyParts()
    {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
    }
}
