/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.WheelSource;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class WheelKeys {
   public static final KeyMapping OPEN = new KeyMapping("key.mahoraga_wheel.open", KeyConflictContext.IN_GAME, Type.KEYSYM, 71, "key.categories.mahoraga_wheel");

   private WheelKeys() {
   }

   static void register(RegisterKeyMappingsEvent event) {
      event.register(OPEN);
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      {
         Minecraft mc = Minecraft.getInstance();

         while (OPEN.consumeClick()) {
            if (mc.player != null && mc.screen == null) {
               if (!WheelSource.WORN.resolve(mc.player).isEmpty()) {
                  WheelScreen.open(WheelSource.WORN);
               } else if (!WheelSource.MAIN_HAND.resolve(mc.player).isEmpty()) {
                  WheelScreen.open(WheelSource.MAIN_HAND);
               } else if (!WheelSource.OFF_HAND.resolve(mc.player).isEmpty()) {
                  WheelScreen.open(WheelSource.OFF_HAND);
               } else {
                  mc.player.displayClientMessage(Component.translatable("message.mahoraga_wheel.no_wheel").withStyle(ChatFormatting.GRAY), true);
               }
            }
         }
      }
   }
}