package com.markin.mechatech.client.render;

import com.markin.mechatech.MechaTech;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Modelo 3D da exo-armadura, estilo "mecha musume": blindagem pesada nos membros,
 * peitoral, chifre e aletas no capacete, asas/propulsores nas costas, saiote e
 * botas grandes — deixando braços, barriga e coxas à mostra (aparece a skin).
 * Cada slot usa uma cópia do modelo mostrando só as peças daquele slot.
 * (Gerado a partir do script de modelagem — as coordenadas batem com a textura.)
 */
public class ExoArmorModel extends HumanoidModel<HumanoidRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(MechaTech.id("exo_armor"), "main");

	public ExoArmorModel(ModelPart root, EquipmentSlot slot) {
		super(root);
		this.head.getChild("helm").visible = slot == EquipmentSlot.HEAD;
		this.hat.visible = false;
		this.body.getChild("chest").visible = slot == EquipmentSlot.CHEST;
		this.body.getChild("waist").visible = slot == EquipmentSlot.LEGS;
		this.rightArm.getChild("arm").visible = slot == EquipmentSlot.CHEST;
		this.leftArm.getChild("arm").visible = slot == EquipmentSlot.CHEST;
		this.rightLeg.getChild("thigh").visible = slot == EquipmentSlot.LEGS;
		this.leftLeg.getChild("thigh").visible = slot == EquipmentSlot.LEGS;
		this.rightLeg.getChild("boot").visible = slot == EquipmentSlot.FEET;
		this.leftLeg.getChild("boot").visible = slot == EquipmentSlot.FEET;
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition head_helm = head.addOrReplaceChild("helm", CubeListBuilder.create()
			.texOffs(56, 33).addBox(-4.6F, -7.4F, -4.7F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(18, 0).addBox(-4.7F, -7.4F, -4.6F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(18, 0).addBox(3.7F, -7.4F, -4.6F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(56, 33).addBox(-4.6F, -7.4F, 3.7F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 33).addBox(-2.0F, -8.6F, -5.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(30, 20).addBox(-5.4F, -5.2F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(30, 20).addBox(4.4F, -5.2F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(102, 33).addBox(-3.0F, -6.1F, -4.9F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(0, 37).addBox(-4.5F, -6.6F, -4.85F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.3F, -0.2F))
			.texOffs(6, 33).addBox(-5.6F, -5.6F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F, 0.1F, 0.1F))
			.texOffs(6, 33).addBox(4.6F, -5.6F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F, 0.1F, 0.1F))
			.texOffs(86, 33).addBox(-1.5F, -8.1F, 3.8F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.2F)), PartPose.ZERO);
		head_helm.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(58, 0).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(62, 27).addBox(-1.0F, -3.0F, -0.6F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(0.0F, -7.6F, -4.4F, -0.45F, 0.0F, 0.0F));
		head_helm.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(0, 12).addBox(-1.0F, -1.0F, -0.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(68, 27).addBox(-1.0F, -1.2F, 4.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-4.7F, -6.6F, -2.0F, 0.1F, -0.55F, 0.45F));
		head_helm.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(0, 12).addBox(0.0F, -1.0F, -0.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(68, 27).addBox(0.0F, -1.2F, 4.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(4.7F, -6.6F, -2.0F, 0.1F, 0.55F, -0.45F));
		head_helm.addOrReplaceChild("p3", CubeListBuilder.create()
			.texOffs(48, 27).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(-1.6F, -8.0F, -4.5F, -0.35F, 0.0F, 0.3F));
		head_helm.addOrReplaceChild("p4", CubeListBuilder.create()
			.texOffs(48, 27).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(1.6F, -8.0F, -4.5F, -0.35F, 0.0F, -0.3F));
		head_helm.addOrReplaceChild("p5", CubeListBuilder.create()
			.texOffs(12, 33).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.25F, 0.0F, -0.25F)),
			PartPose.offsetAndRotation(-5.2F, -6.5F, 1.2F, -0.5F, 0.0F, 0.2F));
		head_helm.addOrReplaceChild("p6", CubeListBuilder.create()
			.texOffs(12, 33).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.25F, 0.0F, -0.25F)),
			PartPose.offsetAndRotation(5.2F, -6.5F, 1.2F, -0.5F, 0.0F, -0.2F));
		PartDefinition body_chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
			.texOffs(38, 20).addBox(-4.2F, -0.2F, -3.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 27).addBox(-3.6F, 0.8F, -3.35F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 27).addBox(0.6F, 0.8F, -3.35F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(52, 12).addBox(-3.0F, -0.9F, -2.8F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(116, 33).addBox(-0.5F, 4.4F, -3.2F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(38, 20).addBox(-4.2F, -0.2F, 2.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(14, 12).addBox(-3.0F, 0.6F, 2.8F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(12, 27).addBox(-3.3F, 6.2F, 3.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(12, 27).addBox(1.3F, 6.2F, 3.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(20, 37).addBox(-4.3F, -0.4F, -3.25F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.05F, -0.2F, -0.3F))
			.texOffs(40, 12).addBox(-4.6F, 3.6F, -2.6F, 1.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F, 0.0F, 0.0F))
			.texOffs(40, 12).addBox(3.6F, 3.6F, -2.6F, 1.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F, 0.0F, 0.0F))
			.texOffs(38, 37).addBox(-3.8F, 0.2F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
			.texOffs(38, 37).addBox(2.8F, 0.2F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
			.texOffs(42, 37).addBox(-1.0F, 5.4F, -3.15F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.3F))
			.texOffs(48, 37).addBox(-2.5F, 1.4F, 4.6F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.1F, -0.2F))
			.texOffs(48, 37).addBox(-2.5F, 3.0F, 4.6F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.1F, -0.2F))
			.texOffs(60, 37).addBox(-3.1F, 9.1F, 3.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F, 0.1F, 0.1F))
			.texOffs(60, 37).addBox(2.1F, 9.1F, 3.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.ZERO);
		body_chest.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-8.0F, -6.0F, 0.0F, 8.0F, 11.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(20, 27).addBox(-8.2F, -6.2F, -0.2F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(20, 37).addBox(-8.0F, 4.6F, -0.2F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.1F)),
			PartPose.offsetAndRotation(-2.5F, 2.5F, 4.6F, 0.15F, -0.55F, -0.3F));
		body_chest.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(0, 0).addBox(0.0F, -6.0F, 0.0F, 8.0F, 11.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(20, 27).addBox(6.2F, -6.2F, -0.2F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(20, 37).addBox(0.0F, 4.6F, -0.2F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.1F)),
			PartPose.offsetAndRotation(2.5F, 2.5F, 4.6F, 0.15F, 0.55F, 0.3F));
		body_chest.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(96, 0).addBox(-6.0F, -4.0F, 0.6F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.3F)),
			PartPose.offsetAndRotation(-2.5F, 3.5F, 4.4F, 0.25F, -0.85F, -0.55F));
		body_chest.addOrReplaceChild("p3", CubeListBuilder.create()
			.texOffs(96, 0).addBox(1.0F, -4.0F, 0.6F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.3F)),
			PartPose.offsetAndRotation(2.5F, 3.5F, 4.4F, 0.25F, 0.85F, 0.55F));
		body_chest.addOrReplaceChild("p4", CubeListBuilder.create()
			.texOffs(16, 33).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-2.6F, 3.6F, 4.4F, 0.0F, 0.0F, 0.0F));
		body_chest.addOrReplaceChild("p5", CubeListBuilder.create()
			.texOffs(16, 33).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(2.6F, 3.6F, 4.4F, 0.0F, 0.0F, 0.0F));
		body_chest.addOrReplaceChild("p6", CubeListBuilder.create()
			.texOffs(16, 33).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
			PartPose.offsetAndRotation(-4.6F, 0.6F, 0.0F, 0.0F, 0.0F, 0.0F));
		body_chest.addOrReplaceChild("p7", CubeListBuilder.create()
			.texOffs(16, 33).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
			PartPose.offsetAndRotation(4.6F, 0.6F, 0.0F, 0.0F, 0.0F, 0.0F));
		PartDefinition body_waist = body.addOrReplaceChild("waist", CubeListBuilder.create()
			.texOffs(56, 20).addBox(-4.4F, 8.8F, -2.4F, 9.0F, 1.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(84, 27).addBox(-2.5F, 9.5F, -2.9F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(96, 27).addBox(-3.5F, 9.5F, 1.9F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(64, 37).addBox(-1.0F, 8.9F, -2.7F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.1F, -0.1F))
			.texOffs(62, 27).addBox(-2.4F, 9.6F, -3.2F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.35F))
			.texOffs(62, 27).addBox(0.4F, 9.6F, -3.2F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.35F)), PartPose.ZERO);
		body_waist.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(108, 0).addBox(-1.0F, -0.5F, -2.6F, 1.0F, 4.0F, 5.0F, new CubeDeformation(-0.25F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-4.3F, 9.4F, 0.0F, 0.0F, 0.0F, 0.35F));
		body_waist.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(108, 0).addBox(0.0F, -0.5F, -2.6F, 1.0F, 4.0F, 5.0F, new CubeDeformation(-0.25F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(4.3F, 9.4F, 0.0F, 0.0F, 0.0F, -0.35F));
		body_waist.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(24, 33).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(0.0F, 11.6F, 2.4F, 0.45F, 0.0F, 0.0F));
		PartDefinition rightArm_arm = rightArm.addOrReplaceChild("arm", CubeListBuilder.create()
			.texOffs(38, 0).addBox(-3.7F, 4.4F, -2.7F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 12).addBox(-3.9F, 4.0F, -2.9F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(26, 27).addBox(-3.3F, 9.6F, -2.3F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(98, 20).addBox(-3.4F, 3.0F, -2.4F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F, -0.3F, 0.0F))
			.texOffs(114, 20).addBox(-2.6F, 4.2F, -3.3F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.35F))
			.texOffs(0, 27).addBox(-4.1F, 5.2F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F, 0.0F, 0.0F))
			.texOffs(70, 37).addBox(-3.6F, 9.2F, -2.9F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.2F)), PartPose.ZERO);
		rightArm_arm.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(62, 0).addBox(-4.0F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 12).addBox(-4.1F, 0.8F, -3.1F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(82, 37).addBox(-4.3F, -2.7F, -3.2F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.3F, -0.3F)),
			PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.22F));
		rightArm_arm.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(30, 12).addBox(-1.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-3.6F, 6.5F, 0.0F, 0.0F, 0.0F, 0.15F));
		rightArm_arm.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(38, 33).addBox(-1.0F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.3F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(-3.8F, -2.4F, 0.0F, 0.0F, 0.0F, 0.6F));
		PartDefinition leftArm_arm = leftArm.addOrReplaceChild("arm", CubeListBuilder.create()
			.texOffs(38, 0).addBox(-1.3F, 4.4F, -2.7F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 12).addBox(-1.1F, 4.0F, -2.9F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(26, 27).addBox(-0.7F, 9.6F, -2.3F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(98, 20).addBox(-0.6F, 3.0F, -2.4F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F, -0.3F, 0.0F))
			.texOffs(114, 20).addBox(-0.4F, 4.2F, -3.3F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.35F))
			.texOffs(0, 27).addBox(3.1F, 5.2F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F, 0.0F, 0.0F))
			.texOffs(70, 37).addBox(-1.4F, 9.2F, -2.9F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.2F)), PartPose.ZERO);
		leftArm_arm.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(62, 0).addBox(-1.0F, -2.5F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(76, 12).addBox(-0.9F, 0.8F, -3.1F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(82, 37).addBox(-0.7F, -2.7F, -3.2F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.3F, -0.3F)),
			PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.22F));
		leftArm_arm.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(30, 12).addBox(0.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(3.6F, 6.5F, 0.0F, 0.0F, 0.0F, -0.15F));
		leftArm_arm.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(38, 33).addBox(0.0F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.3F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(3.8F, -2.4F, 0.0F, 0.0F, 0.0F, -0.6F));
		PartDefinition rightLeg_thigh = rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create()
			.texOffs(84, 20).addBox(-2.6F, 3.6F, -3.2F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(86, 33).addBox(-1.5F, 4.1F, -3.6F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(48, 27).addBox(-2.9F, 0.6F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.35F, 0.0F, -0.3F)), PartPose.ZERO);
		rightLeg_thigh.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(84, 0).addBox(-1.0F, -1.0F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(52, 27).addBox(-1.2F, -0.6F, -2.2F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.3F, -0.2F, 0.0F)),
			PartPose.offsetAndRotation(-2.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.28F));
		rightLeg_thigh.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(38, 33).addBox(-0.5F, -3.2F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(0.0F, 4.2F, -3.8F, -0.6F, 0.0F, 0.0F));
		PartDefinition leftLeg_thigh = leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create()
			.texOffs(84, 20).addBox(-2.4F, 3.6F, -3.2F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(86, 33).addBox(-1.5F, 4.1F, -3.6F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(48, 27).addBox(1.9F, 0.6F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.35F, 0.0F, -0.3F)), PartPose.ZERO);
		leftLeg_thigh.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(84, 0).addBox(0.0F, -1.0F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(52, 27).addBox(0.2F, -0.6F, -2.2F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.3F, -0.2F, 0.0F)),
			PartPose.offsetAndRotation(2.4F, 0.0F, 0.0F, 0.0F, 0.0F, -0.28F));
		leftLeg_thigh.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(38, 33).addBox(-0.5F, -3.2F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F, 0.0F, -0.2F)),
			PartPose.offsetAndRotation(0.0F, 4.2F, -3.8F, -0.6F, 0.0F, 0.0F));
		PartDefinition rightLeg_boot = rightLeg.addOrReplaceChild("boot", CubeListBuilder.create()
			.texOffs(38, 0).addBox(-2.6F, 6.6F, -2.7F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(42, 27).addBox(-1.0F, 7.2F, -3.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(112, 27).addBox(-2.1F, 10.6F, -4.4F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(0, 33).addBox(-1.0F, 9.0F, 2.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(8, 27).addBox(-0.5F, 6.4F, -3.2F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(0, 20).addBox(-2.7F, 11.3F, -2.8F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.1F, -0.2F, 0.0F))
			.texOffs(42, 33).addBox(-2.9F, 8.6F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F, 0.0F, 0.0F))
			.texOffs(94, 33).addBox(-1.6F, 7.0F, 2.35F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.3F))
			.texOffs(94, 37).addBox(-1.7F, 10.0F, -4.7F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.2F)), PartPose.ZERO);
		rightLeg_boot.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(98, 12).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-2.6F, 9.2F, 1.2F, 0.0F, 0.45F, 0.0F));
		rightLeg_boot.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(48, 33).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F, -0.2F, 0.0F)),
			PartPose.offsetAndRotation(0.0F, 11.5F, 2.5F, 0.5F, 0.0F, 0.0F));
		rightLeg_boot.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(22, 20).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-0.3F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(-2.4F, 7.0F, 1.6F, 0.0F, 0.5F, 0.15F));
		PartDefinition leftLeg_boot = leftLeg.addOrReplaceChild("boot", CubeListBuilder.create()
			.texOffs(38, 0).addBox(-2.4F, 6.6F, -2.7F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(42, 27).addBox(-1.0F, 7.2F, -3.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(112, 27).addBox(-1.9F, 10.6F, -4.4F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(0, 33).addBox(-1.0F, 9.0F, 2.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(8, 27).addBox(-0.5F, 6.4F, -3.2F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, 0.0F))
			.texOffs(0, 20).addBox(-2.3F, 11.3F, -2.8F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.1F, -0.2F, 0.0F))
			.texOffs(42, 33).addBox(1.9F, 8.6F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F, 0.0F, 0.0F))
			.texOffs(94, 33).addBox(-1.4F, 7.0F, 2.35F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F, 0.0F, -0.3F))
			.texOffs(94, 37).addBox(-1.3F, 10.0F, -4.7F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F, -0.2F, -0.2F)), PartPose.ZERO);
		leftLeg_boot.addOrReplaceChild("p0", CubeListBuilder.create()
			.texOffs(98, 12).addBox(0.0F, -2.0F, 0.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(2.6F, 9.2F, 1.2F, 0.0F, -0.45F, 0.0F));
		leftLeg_boot.addOrReplaceChild("p1", CubeListBuilder.create()
			.texOffs(48, 33).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F, -0.2F, 0.0F)),
			PartPose.offsetAndRotation(0.0F, 11.5F, 2.5F, 0.5F, 0.0F, 0.0F));
		leftLeg_boot.addOrReplaceChild("p2", CubeListBuilder.create()
			.texOffs(22, 20).addBox(0.0F, -3.0F, 0.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-0.3F, 0.0F, 0.0F)),
			PartPose.offsetAndRotation(2.4F, 7.0F, 1.6F, 0.0F, -0.5F, -0.15F));

		return LayerDefinition.create(mesh, 128, 128);
	}
}
