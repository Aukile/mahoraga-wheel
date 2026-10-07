package com.mahoraga.wheel.client;

import com.mahoraga.wheel.ConvenienceHandler;
import com.mahoraga.wheel.WheelSource;
import com.mahoraga.wheel.network.ActionPacket;
import com.mahoraga.wheel.network.ModNetwork;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WheelKeys {
   public static final KeyMapping OPEN = new KeyMapping("key.mahoraga_wheel.open", KeyConflictContext.IN_GAME, Type.KEYSYM, 71, "key.categories.mahoraga_wheel");

   private static boolean prevJumpDown = false;
   private static boolean prevFwdDown = false;
   private static long lastWTap = -1L;
   private static long lastSpaceTap = 0L;

   private WheelKeys() {
   }

   static void register(RegisterKeyMappingsEvent event) {
      event.register(OPEN);
   }

   @SubscribeEvent
   public static void onRenderGlidePose(RenderLivingEvent.Pre<?, ?> event) {
      if (event.getEntity() instanceof Player player) {
         if (ConvenienceHandler.shouldGlidePose(player)) {
            player.startFallFlying();
            player.setPose(Pose.FALL_FLYING);
         } else if (ConvenienceHandler.shouldLavaSwimPose(player)) {
            player.setSwimming(true);
            player.setPose(Pose.SWIMMING);
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         boolean jumpDown = mc.options.keyJump.isDown();
         if (jumpDown && !prevJumpDown && mc.player != null && mc.screen == null) {
            long now = mc.player.level().getGameTime();
            if (lastSpaceTap > 0L && now - lastSpaceTap < 20L
               && ConvenienceHandler.setGlideLatch(mc.player)) {
               ModNetwork.sendToServer(new ActionPacket(WheelSource.WORN, ActionPacket.GLIDE));
            }
            lastSpaceTap = now;
         }
         prevJumpDown = jumpDown;

         if (mc.player != null && mc.screen == null) {
            boolean fwdDown = mc.player.input.forwardImpulse >= 0.8F;
            if (fwdDown && !prevFwdDown) {
               long now = mc.player.level().getGameTime();
               if (lastWTap > 0L && now - lastWTap < 20L
                  && ConvenienceHandler.setLavaDash(mc.player)) {
                  ModNetwork.sendToServer(new ActionPacket(WheelSource.WORN, ActionPacket.DASH));
               }
               lastWTap = now;
            }
            prevFwdDown = fwdDown;
         }

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
