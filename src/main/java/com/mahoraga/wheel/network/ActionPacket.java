package com.mahoraga.wheel.network;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.AdaptationHandler;
import com.mahoraga.wheel.MahoragaWheelItem;
import com.mahoraga.wheel.WheelSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActionPacket(WheelSource source, byte action) implements CustomPacketPayload {
   public static final byte EQUIP = 0;
   public static final byte RESET = 1;
   public static final CustomPacketPayload.Type<ActionPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "action")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ActionPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {
         buf.writeEnum(msg.source);
         buf.writeByte(msg.action);
      },
      buf -> new ActionPacket(buf.readEnum(WheelSource.class), buf.readByte())
   );

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ActionPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer player) {
         ItemStack wheel = msg.source.resolve(player);
         if (!wheel.isEmpty()) {
            if (msg.action == 0 && msg.source != WheelSource.WORN && wheel.getItem() instanceof MahoragaWheelItem item) {
               item.equipFromHand(player, msg.source.hand());
            } else if (msg.action == 1) {
               AdaptationData.reset(wheel);
               player.level()
                  .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
               AdaptationHandler.hud(player, "reset", Component.translatable("message.mahoraga_wheel.reset").withStyle(ChatFormatting.GRAY));
            }
         }
      }
   }
}
