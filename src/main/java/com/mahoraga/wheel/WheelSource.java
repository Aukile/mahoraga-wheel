/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum WheelSource {
   MAIN_HAND,
   OFF_HAND,
   WORN;

   public ItemStack resolve(Player player) {
      ItemStack stack = switch (this) {
         case MAIN_HAND -> player.getMainHandItem();
         case OFF_HAND -> player.getOffhandItem();
         case WORN -> AdaptationHandler.findWheel(player);
      };
      return stack.getItem() instanceof MahoragaWheelItem ? stack : ItemStack.EMPTY;
   }

   public InteractionHand hand() {
      return this == OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   public static WheelSource of(InteractionHand hand) {
      return hand == InteractionHand.OFF_HAND ? OFF_HAND : MAIN_HAND;
   }
}