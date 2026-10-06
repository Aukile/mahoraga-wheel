package com.mahoraga.wheel.network;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.AdaptationHandler;
import com.mahoraga.wheel.WheelConfig;
import com.mahoraga.wheel.WheelSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SettingPacket(WheelSource source, String key, byte mode, float value, byte immune) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<SettingPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "setting")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SettingPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {
         buf.writeEnum(msg.source);
         buf.writeUtf(msg.key, 512);
         buf.writeByte(msg.mode);
         buf.writeFloat(msg.value);
         buf.writeByte(msg.immune);
      },
      buf -> new SettingPacket(buf.readEnum(WheelSource.class), buf.readUtf(512), buf.readByte(), buf.readFloat(), buf.readByte())
   );

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(SettingPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer player) {
         apply(player, msg);
      }
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
         case SWIM -> (float)(e.level() * WheelConfig.SWIM_SPEED_PER_LEVEL.get());
         case LAVA -> (float)(e.level() * WheelConfig.LAVA_SPEED_PER_LEVEL.get());
         case GLIDE -> e.percent() / 100.0F * ((Double)WheelConfig.GLIDE_MAX_STRENGTH.get()).floatValue();
         case FLY -> e.percent() / 100.0F;
         case JUMP -> (float)(e.level() * WheelConfig.JUMP_HEIGHT_PER_LEVEL.get());
         case LEAP -> (float)(e.level() * WheelConfig.LEAP_SPEED_PER_LEVEL.get());
         case BREATH -> e.percent() / 100.0F;
         case MINE -> (float)(e.level() * WheelConfig.MINE_SPEED_PER_LEVEL.get());
         case SATIETY -> e.percent() / 100.0F;
         case KNOCKBACK -> e.percent() / 100.0F;
         case LAVASIGHT -> e.percent() / 100.0F;
         case CLIMB -> e.percent() / 100.0F;
         case REGEN -> e.percent() / 100.0F * ((Double)WheelConfig.REGEN_RATE.get()).floatValue();
         case WISDOM -> (float)(e.level() * WheelConfig.WISDOM_XP_PER_LEVEL.get());
         case STEP -> e.percent() / 100.0F;
      };
   }

   public static float scale(AdaptationData.Entry e, float max) {
      return switch (e.kind()) {
         case EXISTENCE -> WheelConfig.existenceBonus();
         case DARK -> ((Double)WheelConfig.DARK_MAX_STRENGTH.get()).floatValue();
         case RUN, SWIM, LAVA, JUMP, LEAP, MINE, WISDOM -> Math.max(max, 1.0E-4F);
         case GLIDE -> ((Double)WheelConfig.GLIDE_MAX_STRENGTH.get()).floatValue();
         case REGEN -> ((Double)WheelConfig.REGEN_RATE.get()).floatValue();
         case FLY, BREATH, SATIETY, KNOCKBACK, LAVASIGHT, CLIMB, STEP -> 1.0F;
         default -> 1.0F;
      };
   }
}
