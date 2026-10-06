/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.network;

import com.mahoraga.wheel.client.AdaptationHud;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public record HudMessagePacket(String key, Component text) {
   public static void encode(HudMessagePacket msg, FriendlyByteBuf buf) {
      buf.writeUtf(msg.key);
      buf.writeComponent(msg.text);
   }

   public static HudMessagePacket decode(FriendlyByteBuf buf) {
      return new HudMessagePacket(buf.readUtf(), buf.readComponent());
   }

   public static void handle(HudMessagePacket msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AdaptationHud.push(msg.key(), msg.text())));
      ctx.get().setPacketHandled(true);
   }
}
