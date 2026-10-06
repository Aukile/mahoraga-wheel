/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WheelHeadLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public WheelHeadLayer(RenderLayerParent<T, M> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
      if (head.is((Item)ModItems.MAHORAGA_WHEEL.get())) {
         WheelRenderHelper.render(head, entity, this.getParentModel(), poseStack, buffer, light, ageInTicks);
      }
   }
}