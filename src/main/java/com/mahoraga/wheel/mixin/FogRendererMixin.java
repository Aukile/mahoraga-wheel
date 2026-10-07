package com.mahoraga.wheel.mixin;

import com.mahoraga.wheel.ConvenienceHandler;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lava sight: vanilla lifts the dense lava fog when the camera entity has Fire
 * Resistance. Fully lava-adapted wheel wearers get the same vision without the effect.
 * Other hasEffect checks in setupFog (blindness, darkness, ...) pass through untouched.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
   @Redirect(
      method = "setupFog",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z")
   )
   private static boolean mahoraga_wheel$lavasight(LivingEntity entity, MobEffect effect) {
      boolean real = entity.hasEffect(effect);
      if (!real && effect == MobEffects.FIRE_RESISTANCE && entity instanceof Player player) {
         return ConvenienceHandler.lavaSightStrength(player) >= 0.99F;
      } else {
         return real;
      }
   }
}
