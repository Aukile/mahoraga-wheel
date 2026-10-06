/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent.Added;
import net.minecraftforge.event.entity.living.MobEffectEvent.Applicable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

public final class ConvenienceHandler {
   private static final UUID RUN_SPEED_ID = UUID.fromString("6d61686f-7261-4761-a275-6e2d73706565");
   private static final Map<Player, double[]> LAST_POS = new WeakHashMap<>();
   private static final Map<Player, Double> RUN_PENDING = new WeakHashMap<>();
   private static final Map<LivingEntity, Long> LAST_DENY_FX = new WeakHashMap<>();
   private static Field durationField;

   private ConvenienceHandler() {
   }

   @SubscribeEvent
   public static void onLivingTick(LivingTickEvent event) {
      LivingEntity living = event.getEntity();
      boolean client = living.level().isClientSide;
      if (living instanceof Player player) {
         ItemStack wheel = AdaptationHandler.findWheel(player);
         updateRunModifier(player, wheel);
         if (!client) {
            trackRun(player, wheel);
         }
      }

      if (!client) {
         if ((living.tickCount + living.getId()) % 20 == 0) {
            ItemStack wheel = AdaptationHandler.findWheel(living);
            if (!wheel.isEmpty()) {
               if (living instanceof Player playerx) {
                  tickDark(playerx, wheel);
                  if (wheel.isEmpty()) {
                     return;
                  }

                  commitRun(playerx, wheel);
                  if (wheel.isEmpty()) {
                     return;
                  }
               }

               tickDebuffs(living, wheel);
            }
         }
      }
   }

   static void tickDark(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_DARK.get()) {
         float before = AdaptationData.getPercent(wheel, "C|dark");
         if (!(before >= 100.0F)) {
            BlockPos eye = BlockPos.containing(player.getEyePosition());
            int light = player.level().getMaxLocalRawBrightness(eye);
            if (light <= (Integer)WheelConfig.DARK_LIGHT_LEVEL.get()) {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.DARK_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, "C|dark", gain, time, "dark", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               float maxStrength = ((Double)WheelConfig.DARK_MAX_STRENGTH.get()).floatValue();
               AdaptationData.setLimits(wheel, "C|dark", after / 100.0F * maxStrength, maxStrength);
               Component name = Component.translatable("mahoraga_wheel.adapt.dark");
               if (after >= 100.0F) {
                  if (complete(
                     player,
                     "C|dark",
                     ChatFormatting.AQUA,
                     name,
                     Component.translatable("message.mahoraga_wheel.vision", new Object[]{Math.round(maxStrength * 100.0F)}),
                     true
                  )) {
                     return;
                  }
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(
                     player,
                     "C|dark",
                     ChatFormatting.AQUA,
                     ChatFormatting.DARK_AQUA,
                     name,
                     before,
                     after,
                     Component.translatable("message.mahoraga_wheel.vision", new Object[]{Math.round(after / 100.0F * maxStrength * 100.0F)})
                  );
               }
            }
         }
      }
   }

   public static float darkVisionStrength(Player player) {
      ItemStack wheel = AdaptationHandler.findWheel(player);
      if (wheel.isEmpty()) {
         return 0.0F;
      } else {
         AdaptationData.Entry e = AdaptationData.entry(wheel, "C|dark");
         return e == null ? 0.0F : Math.max(0.0F, Math.min(1.0F, e.effective()));
      }
   }

   public static double blocksForLevel(int level) {
      return (Double)WheelConfig.RUN_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.RUN_BLOCKS_GROWTH.get() * level);
   }

   static void trackRun(Player player, ItemStack wheel) {
      double x = player.getX();
      double z = player.getZ();
      double[] last = LAST_POS.get(player);
      if (last != null
         && !wheel.isEmpty()
         && player.isSprinting()
         && !player.isPassenger()
         && !player.getAbilities().flying
         && !player.isSpectator()
         && (Boolean)WheelConfig.ENABLE_RUN.get()) {
         double d = Math.sqrt((x - last[0]) * (x - last[0]) + (z - last[1]) * (z - last[1]));
         if (d < 2.0) {
            RUN_PENDING.merge(player, d, Double::sum);
         }
      }

      LAST_POS.put(player, new double[]{x, z});
   }

   static void commitRun(Player player, ItemStack wheel) {
      Double pending = RUN_PENDING.remove(player);
      if (pending != null && !(pending <= 0.0) && (Boolean)WheelConfig.ENABLE_RUN.get()) {
         double r = (Double)WheelConfig.GAIN_RANDOMNESS.get() * 0.5;
         double blocks = pending * (1.0 - r + player.getRandom().nextDouble() * 2.0 * r);
         AdaptationData.Entry old = AdaptationData.entry(wheel, "C|run");
         int oldLevel = old == null ? 0 : old.level();
         float oldPercent = old == null ? 0.0F : old.percent();
         int gained = AdaptationData.addRunProgress(wheel, blocks, player.level().getGameTime(), ConvenienceHandler::blocksForLevel);
         int level = oldLevel + gained;
         float speed = (float)(level * (Double)WheelConfig.RUN_SPEED_PER_LEVEL.get());
         AdaptationData.setLimits(wheel, "C|run", speed, Math.max(speed, 1.0E-4F));
         float percent = AdaptationData.getPercent(wheel, "C|run");
         Component name = Component.translatable("mahoraga_wheel.adapt.run")
            .append(" ")
            .append(Component.translatable("message.mahoraga_wheel.level", new Object[]{level}));
         if (gained > 0) {
            for (int i = 0; i < gained; i++) {
               if (complete(
                  player,
                  "C|run",
                  ChatFormatting.YELLOW,
                  name,
                  Component.translatable("message.mahoraga_wheel.speed", new Object[]{Math.round(speed * 100.0F)}),
                  false
               )) {
                  return;
               }
            }
         } else if ((int)(percent / 10.0F) > (int)(oldPercent / 10.0F)) {
            progress(
               player,
               "C|run",
               ChatFormatting.YELLOW,
               ChatFormatting.GOLD,
               name,
               oldPercent,
               percent,
               Component.translatable("message.mahoraga_wheel.speed", new Object[]{Math.round(speed * 100.0F)})
            );
         }
      }
   }

   static void updateRunModifier(Player player, ItemStack wheel) {
      AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attr != null) {
         float amount = 0.0F;
         if (player.isSprinting() && !wheel.isEmpty()) {
            AdaptationData.Entry e = AdaptationData.entry(wheel, "C|run");
            if (e != null) {
               amount = e.effective();
            }
         }

         AttributeModifier current = attr.getModifier(RUN_SPEED_ID);
         if (amount <= 0.0F) {
            if (current != null) {
               attr.removeModifier(RUN_SPEED_ID);
            }
         } else {
            if (current == null || Math.abs(current.getAmount() - amount) > 1.0E-6) {
               if (current != null) {
                  attr.removeModifier(RUN_SPEED_ID);
               }

               attr.addTransientModifier(new AttributeModifier(RUN_SPEED_ID, "Mahoraga run adaptation", amount, Operation.MULTIPLY_BASE));
            }
         }
      }
   }

   public static boolean adaptable(MobEffect effect) {
      if (effect != null && effect.getCategory() == MobEffectCategory.HARMFUL && !effect.isInstantenous()) {
         ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
         return id != null && !((List)WheelConfig.DEBUFF_BLACKLIST.get()).contains(id.toString());
      } else {
         return false;
      }
   }

   public static String keyOf(MobEffect effect) {
      return "P|" + ForgeRegistries.MOB_EFFECTS.getKey(effect);
   }

   static void tickDebuffs(LivingEntity living, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_DEBUFF.get()) {
         List<MobEffectInstance> active = new ArrayList<>();

         for (MobEffectInstance inst : living.getActiveEffects()) {
            if (adaptable(inst.getEffect())) {
               active.add(inst);
            }
         }

         if (!active.isEmpty()) {
            List<MobEffectInstance> candidates = new ArrayList<>();

            for (MobEffectInstance instx : active) {
               String key = keyOf(instx.getEffect());
               if (AdaptationHandler.immuneActive(wheel, key)) {
                  living.removeEffect(instx.getEffect());
                  denyFx(living, instx.getEffect());
               } else if (AdaptationData.getPercent(wheel, key) < 100.0F) {
                  candidates.add(instx);
               }
            }

            if (!candidates.isEmpty()) {
               if ((Boolean)WheelConfig.DEBUFF_ONE_AT_A_TIME.get()) {
                  MobEffectInstance focus = candidates.stream()
                     .max(Comparator.comparingDouble(i -> AdaptationData.getPercent(wheel, keyOf(i.getEffect()))))
                     .orElseThrow();
                  candidates = List.of(focus);
               }

               long time = living.level().getGameTime();

               for (MobEffectInstance instxx : candidates) {
                  MobEffect effect = instxx.getEffect();
                  String key = keyOf(effect);
                  String id = String.valueOf(ForgeRegistries.MOB_EFFECTS.getKey(effect));
                  float before = AdaptationData.getPercent(wheel, key);
                  double factor = Math.pow(instxx.getAmplifier() + 1, (Double)WheelConfig.DAMAGE_EXPONENT.get());
                  float gain = AdaptationHandler.rollGain(living, wheel, before, (Double)WheelConfig.DEBUFF_SECONDS.get(), 0.2, factor);
                  float after = AdaptationData.addPercent(wheel, key, gain, time, id, null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
                  AdaptationData.setLimits(wheel, key, AdaptationHandler.reductionMax(after), 1.0F);
                  Component name = effect.getDisplayName();
                  if (after >= 100.0F) {
                     if (complete(living, key, ChatFormatting.GREEN, name, null, true)) {
                        return;
                     }

                     if (AdaptationHandler.immuneActive(wheel, key)) {
                        living.removeEffect(effect);
                     }
                  } else {
                     float r = AdaptationHandler.reductionFor(after);
                     progress(
                        living,
                        key,
                        ChatFormatting.GREEN,
                        ChatFormatting.DARK_GREEN,
                        name,
                        before,
                        after,
                        r > 0.0F ? Component.translatable("message.mahoraga_wheel.shorten", new Object[]{Math.round(r * 100.0F)}) : null
                     );
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onEffectApplicable(Applicable event) {
      LivingEntity living = event.getEntity();
      if (!living.level().isClientSide && (Boolean)WheelConfig.ENABLE_DEBUFF.get()) {
         MobEffectInstance inst = event.getEffectInstance();
         if (inst != null && adaptable(inst.getEffect())) {
            ItemStack wheel = AdaptationHandler.findWheel(living);
            if (!wheel.isEmpty()) {
               if (AdaptationHandler.immuneActive(wheel, keyOf(inst.getEffect()))) {
                  event.setResult(Result.DENY);
                  denyFx(living, inst.getEffect());
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onEffectAdded(Added event) {
      LivingEntity living = event.getEntity();
      if (!living.level().isClientSide && (Boolean)WheelConfig.ENABLE_DEBUFF.get()) {
         MobEffectInstance inst = event.getEffectInstance();
         if (inst != null && adaptable(inst.getEffect()) && !inst.isInfiniteDuration()) {
            ItemStack wheel = AdaptationHandler.findWheel(living);
            if (!wheel.isEmpty()) {
               String key = keyOf(inst.getEffect());
               float r = AdaptationHandler.effectiveReduction(wheel, key, AdaptationData.getPercent(wheel, key));
               if (!(r <= 0.0F)) {
                  int duration = inst.getDuration();
                  setDuration(inst, Math.max(1, Math.round(duration * (1.0F - r))));
               }
            }
         }
      }
   }

   private static void setDuration(MobEffectInstance inst, int duration) {
      try {
         if (durationField == null) {
            durationField = ObfuscationReflectionHelper.findField(MobEffectInstance.class, "f_19503_");
         }

         durationField.setInt(inst, duration);
      } catch (Exception var3) {
      }
   }

   private static void denyFx(LivingEntity living, MobEffect effect) {
      long now = living.level().getGameTime();
      Long last = LAST_DENY_FX.get(living);
      if (last == null || now - last >= 20L) {
         LAST_DENY_FX.put(living, now);
         ServerLevel level = (ServerLevel)living.level();
         level.playSound(
            null, living.getX(), living.getY(), living.getZ(), (SoundEvent)ModSounds.WHEEL_NULLIFY.get(), SoundSource.PLAYERS, 0.4F, 1.3F
         );
         AdaptationHandler.hud(
            living,
            keyOf(effect),
            Component.literal("☸ ")
               .withStyle(ChatFormatting.GOLD)
               .append(Component.translatable("message.mahoraga_wheel.nullified").withStyle(ChatFormatting.AQUA))
               .append(AdaptationHandler.label(effect.getDisplayName()))
         );
      }
   }

   private static void progress(
      LivingEntity living, String key, ChatFormatting color, ChatFormatting dim, Component name, float before, float after, Component extra
   ) {
      MutableComponent text = Component.literal("☸ ")
         .withStyle(color)
         .append(Component.translatable("message.mahoraga_wheel.existence_analyzing").withStyle(color))
         .append(colored(name, color))
         .append(AdaptationHandler.bar(after, color))
         .append(Component.literal(String.format(" %.1f%%", after)).withStyle(color))
         .append(Component.literal(String.format(" +%.1f", after - before)).withStyle(dim));
      if (extra != null) {
         text.append(Component.literal("  ").append(extra).withStyle(ChatFormatting.AQUA));
      }

      AdaptationHandler.hud(living, key, text);
   }

   private static boolean complete(LivingEntity living, String key, ChatFormatting color, Component name, Component extra, boolean mastery) {
      ItemStack wheel = AdaptationHandler.findWheel(living);
      if (AdaptationHandler.damageWheel(living, (Integer)WheelConfig.DURABILITY_PER_CONVENIENCE.get())) {
         return true;
      } else {
         if (mastery && !wheel.isEmpty()) {
            AdaptationData.addMastery(wheel);
         }

         ParticleOptions particle = key.equals("C|run") ? ParticleTypes.ELECTRIC_SPARK : (key.equals("C|dark") ? ParticleTypes.GLOW : ParticleTypes.HAPPY_VILLAGER);
         AdaptationHandler.playTurn(living, particle, 1.1F);
         MutableComponent text = Component.literal("☸ ")
            .withStyle(color)
            .append(Component.translatable("message.mahoraga_wheel.existence_adapted").withStyle(color))
            .append(colored(name, color))
            .append(AdaptationHandler.bar(100.0F, color));
         if (extra != null) {
            text.append(Component.literal("  ").append(extra).withStyle(color));
         }

         AdaptationHandler.hud(living, key, text);
         return false;
      }
   }

   private static Component colored(Component inner, ChatFormatting color) {
      return Component.literal(" 【").withStyle(color).append(inner.copy().withStyle(color)).append(Component.literal("】 ").withStyle(color));
   }
}
