package com.mahoraga.wheel.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HudMessagePacket(String key, Component text) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<HudMessagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "hud")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, HudMessagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {
         buf.writeUtf(msg.key);
         ComponentSerialization.STREAM_CODEC.encode(buf, msg.text);
      },
      buf -> new HudMessagePacket(buf.readUtf(), ComponentSerialization.STREAM_CODEC.decode(buf))
   );

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
