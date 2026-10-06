/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.mixin;

import com.mahoraga.wheel.ConvenienceHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
   @Redirect(
      method = "updateLightTexture",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z")
   )
   private boolean mahoraga_wheel$hasEffect(LocalPlayer player, MobEffect effect) {
      boolean real = player.hasEffect(effect);
      return !real && effect == MobEffects.NIGHT_VISION ? ConvenienceHandler.darkVisionStrength(player) > 0.0F : real;
   }

   @Redirect(
      method = "updateLightTexture",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F")
   )
   private float mahoraga_wheel$nightVisionScale(LivingEntity entity, float partialTicks) {
      float vanilla = entity.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale(entity, partialTicks) : 0.0F;
      float adapted = entity instanceof Player p ? ConvenienceHandler.darkVisionStrength(p) : 0.0F;
      return Math.max(vanilla, adapted);
   }
}
