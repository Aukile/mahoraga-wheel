/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.network;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
   private static final String VERSION = "2";
   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation("mahoraga_wheel", "main"), () -> "2", "2"::equals, "2"::equals
   );

   private ModNetwork() {
   }

   public static void register() {
      CHANNEL.registerMessage(
         0, HudMessagePacket.class, HudMessagePacket::encode, HudMessagePacket::decode, HudMessagePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
      );
      CHANNEL.registerMessage(
         1, SettingPacket.class, SettingPacket::encode, SettingPacket::decode, SettingPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER)
      );
      CHANNEL.registerMessage(
         2, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER)
      );
   }

   public static void sendToServer(Object msg) {
      CHANNEL.sendToServer(msg);
   }

   public static void sendHud(ServerPlayer player, String key, Component text) {
      CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new HudMessagePacket(key, text));
   }
}
