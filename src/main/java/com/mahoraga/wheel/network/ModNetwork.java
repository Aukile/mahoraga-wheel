package com.mahoraga.wheel.network;

import com.mahoraga.wheel.client.ClientPayloadHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
   private ModNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("2");
      registrar.playToServer(SettingPacket.TYPE, SettingPacket.STREAM_CODEC, SettingPacket::handle);
      registrar.playToServer(ActionPacket.TYPE, ActionPacket.STREAM_CODEC, ActionPacket::handle);
      // Clientbound type+codec must be registered on both sides (so the server can
      // send it); only the handler body is client-only, guarded by a dist check.
      // The ClientPayloadHandler reference lives in a lambda that never executes on
      // a dedicated server, so the client class is never loaded there.
      registrar.playToClient(
         HudMessagePacket.TYPE,
         HudMessagePacket.STREAM_CODEC,
         (msg, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
               ClientPayloadHandler.handleHud(msg, ctx);
            }
         }
      );
   }

   public static void sendToServer(CustomPacketPayload msg) {
      PacketDistributor.sendToServer(msg);
   }

   public static void sendHud(ServerPlayer player, String key, Component text) {
      PacketDistributor.sendToPlayer(player, new HudMessagePacket(key, text));
   }
}
