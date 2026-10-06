package com.mahoraga.wheel.client;

import com.mahoraga.wheel.network.HudMessagePacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
   private ClientPayloadHandler() {
   }

   public static void handleHud(HudMessagePacket msg, IPayloadContext ctx) {
      AdaptationHud.push(msg.key(), msg.text());
   }
}
