/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.WheelConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class WheelRenderHelper {
   private static final Map<LivingEntity, WheelRenderHelper.Anim> ANIMS = new WeakHashMap<>();

   private WheelRenderHelper() {
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      float x = t - 1.0F;
      return 1.0F + c3 * x * x * x + c1 * x * x;
   }

   public static void render(
      ItemStack stack, LivingEntity entity, EntityModel<?> model, PoseStack poseStack, MultiBufferSource buffer, int light, float ageInTicks
   ) {
      int turns = AdaptationData.getTurns(stack);
      float degreesPerTurn = WheelConfig.degreesPerTurn();
      float turnTicks = WheelConfig.turnTicks();
      WheelRenderHelper.Anim anim = ANIMS.get(entity);
      if (anim == null) {
         anim = new WheelRenderHelper.Anim();
         anim.turns = turns;
         anim.from = anim.to = turns * degreesPerTurn;
         anim.start = ageInTicks;
         ANIMS.put(entity, anim);
      } else if (anim.turns != turns) {
         float current = anim.angle(ageInTicks);
         int diff = turns - anim.turns;
         anim.from = current;
         anim.to += diff * degreesPerTurn;
         anim.turns = turns;
         anim.start = ageInTicks;
         anim.duration = turnTicks * Math.min(Math.max(Math.abs(diff), 1), 3);
      }

      float angle = anim.angle(ageInTicks);
      poseStack.pushPose();
      if (model instanceof HeadedModel headed) {
         headed.getHead().translateAndRotate(poseStack);
      } else {
         ICurioRenderer.translateIfSneaking(poseStack, entity);
         poseStack.translate(0.0, -0.25, 0.0);
      }

      float bob = Mth.sin(ageInTicks * 0.08F) * 0.012F;
      poseStack.translate(0.0, -0.86 + bob, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(angle));
      poseStack.scale(0.9F, -0.9F, -0.9F);
      Minecraft.getInstance()
         .getItemRenderer()
         .renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
      poseStack.popPose();
   }

   private static final class Anim {
      int turns;
      float from;
      float to;
      float start;
      float duration = 20.0F;

      float angle(float time) {
         if (time < this.start) {
            this.start = time;
         }

         float t = Mth.clamp((time - this.start) / this.duration, 0.0F, 1.0F);
         return this.from + (this.to - this.from) * WheelRenderHelper.easeOutBack(t);
      }
   }
}