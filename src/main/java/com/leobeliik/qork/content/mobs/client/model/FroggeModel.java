package com.leobeliik.qork.content.mobs.client.model;

import com.leobeliik.qork.content.mobs.entity.Frogge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class FroggeModel extends EntityModel<Frogge> {

    private float frogSize;

    public final ModelPart headTop;
    public final ModelPart headBottom;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightEye;
    public final ModelPart leftEye;

    public FroggeModel(ModelPart root) {
        headTop = root.getChild("headTop");
        headBottom = root.getChild("headBottom");
        body = root.getChild("body");
        rightArm = root.getChild("rightArm");
        leftArm = root.getChild("leftArm");
        rightEye = root.getChild("rightEye");
        leftEye = root.getChild("leftEye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create()
                        .mirror()
                        .texOffs(33, 7)
                        .addBox(-1.0F, -1.0F, -5.0F, 3, 3, 6),
                PartPose.offset(6.5F, 22.0F, 1.0F));

        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create()
                        .texOffs(33, 7)
                        .addBox(-2.0F, -1.0F, -5.0F, 3, 3, 6),
                PartPose.offset(-6.5F, 22.0F, 1.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 7)
                        .addBox(-5.5F, -3.0F, 0.0F, 11, 7, 11),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        root.addOrReplaceChild("headTop",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.5F, -1.0F, -5.0F, 11, 2, 5),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("headBottom",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-5.5F, 1.0F, -5.0F, 11, 2, 5),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("rightEye",
                CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 0)
                        .addBox(1.5F, -1.5F, -4.0F, 1, 1, 1),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        root.addOrReplaceChild("leftEye",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -1.5F, -4.0F, 1, 1, 1),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void prepareMobModel(Frogge frog, float limbSwing, float limbSwingAmount, float partialTickTime) {
        int rawTalkTime = frog.getTalkTime();

        headBottom.xRot = (float) Math.PI / 120;

        if (rawTalkTime != 0) {
            float talkTime = rawTalkTime - partialTickTime;

            int speed = 10;

            headBottom.xRot += Math.PI / 8 * (1 - Mth.cos(talkTime * (float) Math.PI * 2 / speed));
        }
    }

    @Override
    public void setupAnim(Frogge frog, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        frogSize = frog.getSizeModifier();

        rightArm.xRot = Mth.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;
        leftArm.xRot = Mth.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;

        headTop.xRot = headPitch * (float) Math.PI / 180;
        rightEye.xRot = leftEye.xRot = headTop.xRot;
        headBottom.xRot += headPitch * (float) Math.PI / 180;

        if (frog.isVoid()) {
            headTop.xRot *= -1;
            rightEye.xRot *= -1;
            leftEye.xRot *= -1;
            headBottom.xRot *= -1;
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrix, @Nonnull VertexConsumer vb, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrix.pushPose();
        matrix.translate(0, 1.5 - frogSize * 1.5, 0);
        matrix.scale(frogSize, frogSize, frogSize);

        if (young) {
            matrix.pushPose();
            matrix.translate(0, 0.6, 0);
            matrix.scale(0.625F, 0.625F, 0.625F);
        }

        headTop.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        headBottom.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (young) {
            matrix.popPose();
            matrix.scale(0.5F, 0.5F, 0.5F);
            matrix.translate(0, 1.5, 0);
        }

        rightArm.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftArm.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        body.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrix.popPose();
    }

}