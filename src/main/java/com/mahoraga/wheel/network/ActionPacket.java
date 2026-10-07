/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.network;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.AdaptationHandler;
import com.mahoraga.wheel.MahoragaWheelItem;
import com.mahoraga.wheel.WheelSource;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public record ActionPacket(WheelSource source, byte action) {
   public static final byte EQUIP = 0;
   public static final byte RESET = 1;
   public static final byte GLIDE = 2;
   public static final byte DASH = 3;

   public static void encode(ActionPacket msg, FriendlyByteBuf buf) {
      buf.writeEnum(msg.source);
      buf.writeByte(msg.action);
   }

   public static ActionPacket decode(FriendlyByteBuf buf) {
      return new ActionPacket((WheelSource)buf.readEnum(WheelSource.class), buf.readByte());
   }

   public static void handle(ActionPacket msg, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               ServerPlayer player = ctx.get().getSender();
               if (player != null) {
                  ItemStack wheel = msg.source.resolve(player);
                  if (!wheel.isEmpty()) {
                     if (msg.action == 0 && msg.source != WheelSource.WORN && wheel.getItem() instanceof MahoragaWheelItem item) {
                        item.equipFromHand(player, msg.source.hand());
                     } else if (msg.action == 2) {
                        com.mahoraga.wheel.ConvenienceHandler.setGlideLatch(player);
                     } else if (msg.action == 3) {
                        com.mahoraga.wheel.ConvenienceHandler.noteDashRx(player);
                        com.mahoraga.wheel.ConvenienceHandler.setLavaDash(player);
                     } else if (msg.action == 1) {
                        AdaptationData.reset(wheel);
                        player.level()
                           .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
                        AdaptationHandler.hud(player, "reset", Component.translatable("message.mahoraga_wheel.reset").withStyle(ChatFormatting.GRAY));
                     }
                  }
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }
}
