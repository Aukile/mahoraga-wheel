/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.network;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.AdaptationHandler;
import com.mahoraga.wheel.WheelConfig;
import com.mahoraga.wheel.WheelSource;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public record SettingPacket(WheelSource source, String key, byte mode, float value, byte immune) {
   public static void encode(SettingPacket msg, FriendlyByteBuf buf) {
      buf.writeEnum(msg.source);
      buf.writeUtf(msg.key, 512);
      buf.writeByte(msg.mode);
      buf.writeFloat(msg.value);
      buf.writeByte(msg.immune);
   }

   public static SettingPacket decode(FriendlyByteBuf buf) {
      return new SettingPacket((WheelSource)buf.readEnum(WheelSource.class), buf.readUtf(512), buf.readByte(), buf.readFloat(), buf.readByte());
   }

   public static void handle(SettingPacket msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player != null) {
            apply(player, msg);
         }
      });
      ctx.get().setPacketHandled(true);
   }

   public static void apply(ServerPlayer player, SettingPacket msg) {
      ItemStack wheel = msg.source.resolve(player);
      if (!wheel.isEmpty()) {
         AdaptationData.Entry e = AdaptationData.entry(wheel, msg.key);
         if (e != null) {
            float max = currentMax(e);
            AdaptationData.setLimits(wheel, msg.key, max, scale(e, max));
            if (msg.immune >= 0 && e.kind().reduction() && e.complete()) {
               AdaptationData.setImmune(wheel, msg.key, msg.immune == 1);
            }

            if (msg.mode == 2) {
               AdaptationData.setUser(wheel, msg.key, Float.NaN);
            } else if (msg.mode == 1 && Float.isFinite(msg.value)) {
               float v = Math.max(0.0F, msg.value);
               AdaptationData.setUser(wheel, msg.key, v >= max - 1.0E-4F ? Float.NaN : v);
            }
         }
      }
   }

   public static float currentMax(AdaptationData.Entry e) {
      return switch (e.kind()) {
         case ATTACK, ENVIRONMENT, DEBUFF -> AdaptationHandler.reductionMax(e.percent());
         case EXISTENCE -> AdaptationHandler.existenceBonus(e.percent());
         case DARK -> e.percent() / 100.0F * ((Double)WheelConfig.DARK_MAX_STRENGTH.get()).floatValue();
         case RUN -> (float)(e.level() * WheelConfig.RUN_SPEED_PER_LEVEL.get());
      };
   }

   public static float scale(AdaptationData.Entry e, float max) {
      return switch (e.kind()) {
         case EXISTENCE -> WheelConfig.existenceBonus();
         case DARK -> ((Double)WheelConfig.DARK_MAX_STRENGTH.get()).floatValue();
         case RUN -> Math.max(max, 1.0E-4F);
         default -> 1.0F;
      };
   }
}
