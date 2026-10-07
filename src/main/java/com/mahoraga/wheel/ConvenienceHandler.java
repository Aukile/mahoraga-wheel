/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.util.ObfuscationReflectionHelper;

public final class ConvenienceHandler {
   private static final ResourceLocation RUN_SPEED_ID = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "run_speed");
   private static final ResourceLocation SWIM_SPEED_ID = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "swim_speed");
   private static final ResourceLocation LAVA_SPEED_ID = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "lava_speed");
   private static final ResourceLocation LEAP_SPEED_ID = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "leap_speed");
   private static final ResourceLocation STEP_HEIGHT_ID = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "step_height");
   private static final String TAG_MAHORAGA_FLY = "mahoraga_wheel_mayfly";
   private static final Map<Player, double[]> LAST_POS = new WeakHashMap<>();
   private static final Map<Player, double[]> LAST_POS3 = new WeakHashMap<>();
   private static final Map<Player, Double> RUN_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> SWIM_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> LAVA_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> LEAP_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> JUMP_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> MINE_PENDING = new WeakHashMap<>();
   private static final Map<Player, Double> WISDOM_PENDING = new WeakHashMap<>();
   private static final Map<Player, Integer> LAST_FOOD = new WeakHashMap<>();
   private static final Map<LivingEntity, Long> LAST_HURT = new WeakHashMap<>();
   private static final Map<Player, Long> LAST_GLIDE = new WeakHashMap<>();
   private static final Map<LivingEntity, Long> LAST_DENY_FX = new WeakHashMap<>();
   private static Field durationField;

   private ConvenienceHandler() {
   }

   @SubscribeEvent
   public static void onEntityTick(EntityTickEvent.Post event) {
      if (!(event.getEntity() instanceof LivingEntity living)) {
         return;
      }
      boolean client = living.level().isClientSide;
      if (living instanceof Player player) {
         ItemStack wheel = AdaptationHandler.findWheel(player);
         updateRunModifier(player, wheel);
         updateSwimModifier(player, wheel);
         updateLavaModifier(player, wheel);
         updateLeapModifier(player, wheel);
         updateStepModifier(player, wheel);
         tickLavaSwim(player, wheel);
         tickGlideMove(player, wheel);
         syncPoses(player);
         tickClimbMove(player, wheel);
         if (!client) {
            trackRun(player, wheel);
            trackSwimEtc(player, wheel);
            tickFlight(player, wheel);
            tickBreathHold(player, wheel);
            tickSatietyHold(player, wheel);
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

                  if (AdaptationHandler.isEnhanced(wheel)) {
                     commitSwim(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     commitLava(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     commitLeap(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     commitJump(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickGlide(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickFly(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     commitMine(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     commitWisdom(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickBreath(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickSatiety(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickLavasight(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickClimb(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }

                     tickStep(playerx, wheel);
                     if (wheel.isEmpty()) {
                        return;
                     }
                  }
               }

               tickDebuffs(living, wheel);
               tickRegen(living, wheel);
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

   public static double swimBlocksForLevel(int level) {
      return (Double)WheelConfig.SWIM_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.SWIM_BLOCKS_GROWTH.get() * level);
   }

   public static double lavaBlocksForLevel(int level) {
      return (Double)WheelConfig.LAVA_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.LAVA_BLOCKS_GROWTH.get() * level);
   }

   public static double jumpBlocksForLevel(int level) {
      return (Double)WheelConfig.JUMP_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.JUMP_BLOCKS_GROWTH.get() * level);
   }

   public static double leapBlocksForLevel(int level) {
      return (Double)WheelConfig.LEAP_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.LEAP_BLOCKS_GROWTH.get() * level);
   }

   static boolean lavaUnlocked(ItemStack wheel) {
      if (!(Boolean)WheelConfig.LAVA_REQUIRES_ADAPT.get()) {
         return true;
      } else {
         return AdaptationData.getPercent(wheel, "D|minecraft:lava") >= 100.0F;
      }
   }

   static boolean glidingRecently(Player player, long gameTime, long window) {
      Long last = LAST_GLIDE.get(player);
      return last != null && gameTime - last < window;
   }

   static void trackSwimEtc(Player player, ItemStack wheel) {
      double x = player.getX();
      double y = player.getY();
      double z = player.getZ();
      double[] last = LAST_POS3.get(player);
      LAST_POS3.put(player, new double[]{x, y, z});
      if (last == null || wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel) || player.isSpectator() || player.isPassenger()) {
         return;
      } else {
         double dx = Math.sqrt((x - last[0]) * (x - last[0]) + (z - last[2]) * (z - last[2]));
         boolean flying = player.getAbilities().flying;
         if ((Boolean)WheelConfig.ENABLE_SWIM.get() && player.isInWater() && !flying && dx < 2.0) {
            SWIM_PENDING.merge(player, dx, Double::sum);
         }

         if ((Boolean)WheelConfig.ENABLE_LAVA.get() && player.isInLava() && !flying && lavaUnlocked(wheel) && dx < 2.0) {
            LAVA_PENDING.merge(player, dx, Double::sum);
         }

         if ((Boolean)WheelConfig.ENABLE_LEAP.get()
            && player.isSprinting()
            && !player.onGround()
            && !flying
            && !player.isInWater()
            && !player.isInLava()
            && !player.isFallFlying()
            && dx < 3.0) {
            LEAP_PENDING.merge(player, dx, Double::sum);
         }

         if ((Boolean)WheelConfig.ENABLE_JUMP.get()
            && !player.onGround()
            && !flying
            && !player.isInWater()
            && !player.isInLava()
            && !player.isFallFlying()
            && !player.onClimbable()
            && !glidingRecently(player, player.level().getGameTime(), 10L)) {
            double dy = y - last[1];
            if (dy > 0.0 && dy < 2.0) {
               JUMP_PENDING.merge(player, dy, Double::sum);
            }
         }
      }
   }

   private static void commitLevel(
      Player player,
      ItemStack wheel,
      Map<Player, Double> pending,
      String key,
      String damageType,
      String langKey,
      String extraKey,
      ChatFormatting color,
      ChatFormatting dim,
      java.util.function.IntToDoubleFunction needForLevel,
      double perLevel
   ) {
      Double p = pending.remove(player);
      if (p != null && !(p <= 0.0)) {
         double r = (Double)WheelConfig.GAIN_RANDOMNESS.get() * 0.5;
         double amount = p * (1.0 - r + player.getRandom().nextDouble() * 2.0 * r);
         AdaptationData.Entry old = AdaptationData.entry(wheel, key);
         int oldLevel = old == null ? 0 : old.level();
         float oldPercent = old == null ? 0.0F : old.percent();
         int gained = AdaptationData.addLevelProgress(wheel, key, amount, player.level().getGameTime(), needForLevel, damageType);
         int level = oldLevel + gained;
         float value = (float)(level * perLevel);
         AdaptationData.setLimits(wheel, key, value, Math.max(value, 1.0E-4F));
         float percent = AdaptationData.getPercent(wheel, key);
         Component name = Component.translatable(langKey).append(" ").append(Component.translatable("message.mahoraga_wheel.level", new Object[]{level}));
         Component extra = Component.translatable(extraKey, new Object[]{Math.round(value * 100.0F)});
         if (gained > 0) {
            for (int i = 0; i < gained; i++) {
               if (complete(player, key, color, name, extra, false)) {
                  return;
               }
            }
         } else if ((int)(percent / 10.0F) > (int)(oldPercent / 10.0F)) {
            progress(player, key, color, dim, name, oldPercent, percent, extra);
         }
      }
   }

   static void commitSwim(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_SWIM.get()) {
         commitLevel(
            player,
            wheel,
            SWIM_PENDING,
            AdaptationData.KEY_SWIM,
            "swim",
            "mahoraga_wheel.adapt.swim",
            "message.mahoraga_wheel.speed",
            ChatFormatting.BLUE,
            ChatFormatting.DARK_BLUE,
            ConvenienceHandler::swimBlocksForLevel,
            (Double)WheelConfig.SWIM_SPEED_PER_LEVEL.get()
         );
      }
   }

   static void commitLava(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_LAVA.get() && lavaUnlocked(wheel)) {
         commitLevel(
            player,
            wheel,
            LAVA_PENDING,
            AdaptationData.KEY_LAVA,
            "lava",
            "mahoraga_wheel.adapt.lava",
            "message.mahoraga_wheel.speed",
            ChatFormatting.GOLD,
            ChatFormatting.DARK_RED,
            ConvenienceHandler::lavaBlocksForLevel,
            (Double)WheelConfig.LAVA_SPEED_PER_LEVEL.get()
         );
      } else {
         LAVA_PENDING.remove(player);
      }
   }

   static void commitLeap(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_LEAP.get()) {
         commitLevel(
            player,
            wheel,
            LEAP_PENDING,
            AdaptationData.KEY_LEAP,
            "leap",
            "mahoraga_wheel.adapt.leap",
            "message.mahoraga_wheel.speed",
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ConvenienceHandler::leapBlocksForLevel,
            (Double)WheelConfig.LEAP_SPEED_PER_LEVEL.get()
         );
      }
   }

   static void commitJump(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_JUMP.get()) {
         commitLevel(
            player,
            wheel,
            JUMP_PENDING,
            AdaptationData.KEY_JUMP,
            "jump",
            "mahoraga_wheel.adapt.jump",
            "message.mahoraga_wheel.jump",
            ChatFormatting.DARK_GREEN,
            ChatFormatting.GREEN,
            ConvenienceHandler::jumpBlocksForLevel,
            (Double)WheelConfig.JUMP_HEIGHT_PER_LEVEL.get()
         );
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
            if (current == null || Math.abs(current.amount() - amount) > 1.0E-6) {
               if (current != null) {
                  attr.removeModifier(RUN_SPEED_ID);
               }

               attr.addTransientModifier(new AttributeModifier(RUN_SPEED_ID, amount, Operation.ADD_MULTIPLIED_BASE));
            }
         }
      }
   }

   private static void setSpeedModifier(Player player, ResourceLocation id, float amount) {
      AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attr != null) {
         AttributeModifier current = attr.getModifier(id);
         if (amount <= 0.0F) {
            if (current != null) {
               attr.removeModifier(id);
            }
         } else if (current == null || Math.abs(current.amount() - amount) > 1.0E-6) {
            if (current != null) {
               attr.removeModifier(id);
            }

            attr.addTransientModifier(new AttributeModifier(id, amount, Operation.ADD_MULTIPLIED_BASE));
         }
      }
   }

   static void updateStepModifier(Player player, ItemStack wheel) {
      AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
      if (attr != null) {
         float amount = (Boolean)WheelConfig.ENABLE_STEP.get() ? Mth.clamp(entryEffective(wheel, AdaptationData.KEY_STEP), 0.0F, 1.0F) : 0.0F;
         AttributeModifier current = attr.getModifier(STEP_HEIGHT_ID);
         if (amount <= 0.0F) {
            if (current != null) {
               attr.removeModifier(STEP_HEIGHT_ID);
            }
         } else if (current == null || Math.abs(current.amount() - amount) > 1.0E-6) {
            if (current != null) {
               attr.removeModifier(STEP_HEIGHT_ID);
            }

            attr.addTransientModifier(new AttributeModifier(STEP_HEIGHT_ID, amount, Operation.ADD_VALUE));
         }
      }
   }

   private static float entryEffective(ItemStack wheel, String key) {
      if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel)) {
         return 0.0F;
      } else {
         AdaptationData.Entry e = AdaptationData.entry(wheel, key);
         return e == null ? 0.0F : Math.max(0.0F, e.effective());
      }
   }

   static void updateSwimModifier(Player player, ItemStack wheel) {
      float amount = player.isInWater() && !player.getAbilities().flying ? entryEffective(wheel, AdaptationData.KEY_SWIM) : 0.0F;
      setSpeedModifier(player, SWIM_SPEED_ID, amount);
   }

   static void updateLavaModifier(Player player, ItemStack wheel) {
      float amount = 0.0F;
      if (player.isInLava() && !player.getAbilities().flying && !wheel.isEmpty() && lavaUnlocked(wheel)) {
         amount = entryEffective(wheel, AdaptationData.KEY_LAVA);
      }

      setSpeedModifier(player, LAVA_SPEED_ID, amount);
   }

   static void updateLeapModifier(Player player, ItemStack wheel) {
      boolean active = player.isSprinting()
         && !player.onGround()
         && !player.getAbilities().flying
         && !player.isInWater()
         && !player.isInLava()
         && !player.isFallFlying()
         && !player.isPassenger();
      setSpeedModifier(player, LEAP_SPEED_ID, active ? entryEffective(wheel, AdaptationData.KEY_LEAP) : 0.0F);
   }

         static void tickLavaSwim(Player player, ItemStack wheel) {
      if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel) || !(Boolean)WheelConfig.ENABLE_LAVA.get()) {
         LAVA_DASH.remove(player);
         return;
      } else if (!player.isInLava() || player.isPassenger() || player.getAbilities().flying || !lavaUnlocked(wheel)) {
         if (player.isPassenger() || player.getAbilities().flying || !lavaUnlocked(wheel)
            || !recentlyInLava(player, player.level().getGameTime(), 8L)) {
            LAVA_DASH.remove(player);
         }
         return;
      } else {
         // Vanilla lava travel already applied yaw-based swim input (0.02) plus 0.5 drag.
         // Convert the drag to water's 0.8 (ratio 1.6, 1.8 sprinting) so lava swims like
         // water; adaptation levels scale above that pace.
         float swim = Mth.clamp(entryEffective(wheel, AdaptationData.KEY_LAVA), 0.0F, 1.0F);
         LAST_LAVA.put(player, player.level().getGameTime());
         player.fallDistance = 0.0F;
         Vec3 m = player.getDeltaMovement();
         if (player.zza < 0.2F && player.level().getGameTime()
               - LAST_DASH_RX.getOrDefault(player, -100L) > 10L) {
            LAVA_DASH.remove(player);
         }
         double mult = ((player.isSprinting() || LAVA_DASH.contains(player)) ? 1.8 : 1.6) * (1.0 + swim);
         double nx = m.x * mult;
         double nz = m.z * mult;
         double ns = Math.sqrt(nx * nx + nz * nz);
         if (ns > 0.6 && ns > 1.0E-4) {
            nx *= 0.6 / ns;
            nz *= 0.6 / ns;
         }
         double targetY = player.isCrouching() ? -0.18 : 0.04;
         double ny = m.y + (targetY - m.y) * 0.35;
         player.setDeltaMovement(nx, ny, nz);
         player.hasImpulse = true;
         if (shouldLavaSwimPose(player)) {
            player.setSwimming(true);
         }
      }
   }

      private static final java.util.Set<Player> GLIDE_LATCH =
      java.util.Collections.newSetFromMap(new WeakHashMap<>());

   /** Space-mid-air latch request (both sides). Returns true if latched. */
      private static boolean wearingUsableElytra(Player player) {
      ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
      return !chest.isEmpty() && chest.canElytraFly(player);
   }

   public static boolean shouldLavaSwimPose(Player player) {
      if (!(player.isSprinting() || LAVA_DASH.contains(player)) || !recentlyInLava(player, player.level().getGameTime(), 8L) || player.isFallFlying() || player.isPassenger()
         || player.getAbilities().flying || player.isSpectator()) {
         return false;
      }
      ItemStack wheel = AdaptationHandler.findWheel(player);
      return !wheel.isEmpty() && AdaptationHandler.isEnhanced(wheel)
         && (Boolean) WheelConfig.ENABLE_LAVA.get() && lavaUnlocked(wheel);
   }

   private static final java.util.Set<Player> LAVA_DASH =
      java.util.Collections.newSetFromMap(new WeakHashMap<>());
   private static final java.util.Map<Player, Long> LAST_DASH_RX = new WeakHashMap<>();

   /** Double-W dash in lava (both sides). Returns true if dashed. */
   public static void noteDashRx(Player player) {
      LAST_DASH_RX.put(player, player.level().getGameTime());
   }

   public static boolean setLavaDash(Player player) {
      ItemStack wheel = AdaptationHandler.findWheel(player);
      if (wheel.isEmpty()
         || !AdaptationHandler.isEnhanced(wheel)
         || !(Boolean)WheelConfig.ENABLE_LAVA.get()
         || (!player.isInLava() && !recentlyInLava(player, player.level().getGameTime(), 8L))
         || player.isPassenger()
         || player.getAbilities().flying
         || wearingUsableElytra(player)
         || player.isSpectator()
         || !lavaUnlocked(wheel)) {
      return false;
      }
      LAVA_DASH.add(player);
      return true;
   }

      private static final java.util.Map<Player, Long> LAST_LAVA = new WeakHashMap<>();

   private static boolean recentlyInLava(Player player, long now, long window) {
      Long last = LAST_LAVA.get(player);
      return last != null && now - last < window;
   }

public static boolean isLavaDashed(Player player) {
      return LAVA_DASH.contains(player);
   }

   public static boolean shouldGlidePose(Player player) {
      if (player.onGround() || player.isInWater() || player.isInLava() || player.isPassenger()
         || player.getAbilities().flying || player.isSpectator() || wearingUsableElytra(player)) {
         return false;
      }
      if (!GLIDE_LATCH.contains(player) && !glidingActive(player)) {
         return false;
      }
      ItemStack wheel = AdaptationHandler.findWheel(player);
      return !wheel.isEmpty() && AdaptationHandler.isEnhanced(wheel)
         && (Boolean) WheelConfig.ENABLE_GLIDE.get()
         && Mth.clamp(entryEffective(wheel, AdaptationData.KEY_GLIDE), 0.0F, 1.0F) > 0.0F;
   }

   static void syncPoses(Player player) {
      if (wearingUsableElytra(player) && player.isFallFlying()) {
         return;
      }
      if (shouldGlidePose(player)) {
         player.startFallFlying();
         player.setPose(Pose.FALL_FLYING);
      } else if (shouldLavaSwimPose(player)) {
         player.setSwimming(true);
         player.setPose(Pose.SWIMMING);
      } else {
         if (!wearingUsableElytra(player)) {
            player.stopFallFlying();
         }
         if (!player.isInWater()) {
            player.setSwimming(false);
         }
         Pose pose = player.getPose();
         if (pose == Pose.FALL_FLYING && !wearingUsableElytra(player)) {
            player.setPose(Pose.STANDING);
         } else if (pose == Pose.SWIMMING && !player.isInWater()) {
            player.setPose(Pose.STANDING);
         }
      }
   }

public static boolean glidingActive(Player player) {
      Long last = LAST_GLIDE.get(player);
      return last != null && player.level().getGameTime() - last < 6L;
   }

   public static boolean isGlideLatched(Player player) {
      return GLIDE_LATCH.contains(player);
   }

   public static boolean setGlideLatch(Player player) {
      ItemStack wheel = AdaptationHandler.findWheel(player);
      if (wheel.isEmpty()
         || !AdaptationHandler.isEnhanced(wheel)
         || !(Boolean)WheelConfig.ENABLE_GLIDE.get()
                  || player.isPassenger()
         || player.getAbilities().flying
         || player.isInWater()
         || player.isInLava()
         || wearingUsableElytra(player)
         || player.isSpectator()
         || !(Mth.clamp(entryEffective(wheel, AdaptationData.KEY_GLIDE), 0.0F, 1.0F) > 0.0F)) {
      return false;
      }
      GLIDE_LATCH.add(player);
      return true;
   }

static void tickGlideMove(Player player, ItemStack wheel) {
      if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel) || !(Boolean)WheelConfig.ENABLE_GLIDE.get()) {
         GLIDE_LATCH.remove(player);
         return;
      } else if (player.onGround()
         || player.isPassenger()
         || player.getAbilities().flying
         || player.isInWater()
         || player.isInLava()
         || wearingUsableElytra(player)
         || player.isSpectator()) {
         GLIDE_LATCH.remove(player);
         return;
      } else {
         long now = player.level().getGameTime();
         Vec3 m = player.getDeltaMovement();
         boolean latched = GLIDE_LATCH.contains(player);
         boolean falling = m.y < -0.08 && player.fallDistance >= 2.5F;
         if (!latched && !falling && !glidingRecently(player, now, 5L)) {
            return;
         } else if (!latched && (Boolean)WheelConfig.GLIDE_REQUIRE_SNEAK.get() && !player.isCrouching()) {
            return;
         } else {
            float strength = Mth.clamp(entryEffective(wheel, AdaptationData.KEY_GLIDE), 0.0F, 1.0F);
            if (!(strength > 0.0F)) {
               GLIDE_LATCH.remove(player);
               return;
            } else {
               Vec3 look = player.getLookAngle();
               float pitchRad = player.getXRot() * (float)(Math.PI / 180.0);
               double nx = m.x + look.x * 0.028 * strength;
               double nz = m.z + look.z * 0.028 * strength;
               double hx = Math.sqrt(nx * nx + nz * nz);
               double maxH = 0.5 + 1.1 * strength;
               if (hx > maxH && hx > 1.0E-4) {
                  nx *= maxH / hx;
                  nz *= maxH / hx;
                  hx = maxH;
               }

               double targetY = -Math.sin(pitchRad) * hx * 0.85 - 0.10 * (1.0 - strength) - 0.03;
               if (targetY > 0.1) {
                  nx *= 0.985;
                  nz *= 0.985;
               }

               player.setDeltaMovement(nx, Mth.lerp(0.3, m.y, targetY), nz);
               player.hasImpulse = true;
               player.fallDistance = 0.0F;
               LAST_GLIDE.put(player, now);
               player.startFallFlying();
            }
         }
      }
   }

   static void tickGlide(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_GLIDE.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_GLIDE);
         if (!(before >= 100.0F)) {
            if (AdaptationData.getPercent(wheel, "D|minecraft:fall") < 100.0F) {
               return;
            } else if (player.onGround() || player.isInWater() || player.isInLava() || player.getAbilities().flying || player.isPassenger()) {
               return;
            } else if (player.fallDistance < 1.0F && player.getDeltaMovement().y > -0.15) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.GLIDE_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_GLIDE, gain, time, "glide", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               float maxStrength = ((Double)WheelConfig.GLIDE_MAX_STRENGTH.get()).floatValue();
               AdaptationData.setLimits(wheel, AdaptationData.KEY_GLIDE, after / 100.0F * maxStrength, maxStrength);
               Component name = Component.translatable("mahoraga_wheel.adapt.glide");
               if (after >= 100.0F) {
                  complete(
                     player,
                     AdaptationData.KEY_GLIDE,
                     ChatFormatting.WHITE,
                     name,
                     Component.translatable("message.mahoraga_wheel.glide", new Object[]{Math.round(maxStrength * 100.0F)}),
                     true
                  );
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(
                     player,
                     AdaptationData.KEY_GLIDE,
                     ChatFormatting.WHITE,
                     ChatFormatting.GRAY,
                     name,
                     before,
                     after,
                     Component.translatable("message.mahoraga_wheel.glide", new Object[]{Math.round(after / 100.0F * maxStrength * 100.0F)})
                  );
               }
            }
         }
      }
   }

   static void tickFly(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_FLY.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_FLY);
         if (!(before >= 100.0F)) {
            if (AdaptationData.getPercent(wheel, AdaptationData.KEY_GLIDE) < 100.0F) {
               return;
            } else {
               long now = player.level().getGameTime();
               boolean gliding = glidingRecently(player, now, 40L) || player.isFallFlying();
               if (!gliding) {
                  return;
               } else {
                  float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.FLY_SECONDS.get(), 0.2, 1.0);
                  float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_FLY, gain, now, "fly", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
                  AdaptationData.setLimits(wheel, AdaptationData.KEY_FLY, after / 100.0F, 1.0F);
                  Component name = Component.translatable("mahoraga_wheel.adapt.fly");
                  if (after >= 100.0F) {
                     complete(player, AdaptationData.KEY_FLY, ChatFormatting.DARK_PURPLE, name, Component.translatable("message.mahoraga_wheel.fly"), true);
                  } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                     progress(
                        player,
                        AdaptationData.KEY_FLY,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.LIGHT_PURPLE,
                        name,
                        before,
                        after,
                        Component.translatable("message.mahoraga_wheel.flyspeed", new Object[]{Math.round(after)})
                     );
                  }
               }
            }
         }
      }
   }

   static void tickFlight(Player player, ItemStack wheel) {
      if (player.level().isClientSide || !(player instanceof ServerPlayer sp)) {
         return;
      } else {
         boolean want = !wheel.isEmpty()
            && AdaptationHandler.isEnhanced(wheel)
            && (Boolean)WheelConfig.ENABLE_FLY.get()
            && AdaptationData.getPercent(wheel, AdaptationData.KEY_FLY) >= 100.0F;
         boolean granted = sp.getPersistentData().getBoolean(TAG_MAHORAGA_FLY);
         if (want) {
            float eff = Mth.clamp(entryEffective(wheel, AdaptationData.KEY_FLY), 0.0F, 1.0F);
            float speed = 0.02F + 0.08F * eff;
            if (!sp.getAbilities().mayfly || Math.abs(sp.getAbilities().getFlyingSpeed() - speed) > 1.0E-4F) {
               sp.getAbilities().mayfly = true;
               sp.getAbilities().setFlyingSpeed(speed);
               sp.onUpdateAbilities();
            }

            sp.getPersistentData().putBoolean(TAG_MAHORAGA_FLY, true);
         } else if (granted && !sp.isCreative() && !sp.isSpectator()) {
            sp.getAbilities().mayfly = false;
            sp.getAbilities().flying = false;
            sp.getAbilities().setFlyingSpeed(0.05F);
            sp.onUpdateAbilities();
            sp.getPersistentData().putBoolean(TAG_MAHORAGA_FLY, false);
         }
      }
   }

   @SubscribeEvent
   public static void onJump(LivingJumpEvent event) {
      if (event.getEntity() instanceof Player player && (Boolean)WheelConfig.ENABLE_JUMP.get()) {
         ItemStack wheel = AdaptationHandler.findWheel(player);
         float boost = entryEffective(wheel, AdaptationData.KEY_JUMP);
         if (boost > 0.0F) {
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x, m.y * (1.0 + boost), m.z);
         }
      }
   }

   public static boolean adaptable(Holder<MobEffect> effect) {
      if (effect != null && effect.value().getCategory() == MobEffectCategory.HARMFUL && !effect.value().isInstantenous()) {
         ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
         return id != null && !((List)WheelConfig.DEBUFF_BLACKLIST.get()).contains(id.toString());
      } else {
         return false;
      }
   }

   public static String keyOf(Holder<MobEffect> effect) {
      return "P|" + BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
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
                  Holder<MobEffect> effect = instxx.getEffect();
                  String key = keyOf(effect);
                  String id = String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect.value()));
                  float before = AdaptationData.getPercent(wheel, key);
                  double factor = Math.pow(instxx.getAmplifier() + 1, (Double)WheelConfig.DAMAGE_EXPONENT.get());
                  float gain = AdaptationHandler.rollGain(living, wheel, before, (Double)WheelConfig.DEBUFF_SECONDS.get(), 0.2, factor);
                  float after = AdaptationData.addPercent(wheel, key, gain, time, id, null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
                  AdaptationData.setLimits(wheel, key, AdaptationHandler.reductionMax(after), 1.0F);
                  Component name = effect.value().getDisplayName();
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
                  event.setResult(Applicable.Result.DO_NOT_APPLY);
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
            durationField = ObfuscationReflectionHelper.findField(MobEffectInstance.class, "duration");
         }

         durationField.setInt(inst, duration);
      } catch (Exception var3) {
      }
   }

   private static void denyFx(LivingEntity living, Holder<MobEffect> effect) {
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
               .append(AdaptationHandler.label(effect.value().getDisplayName()))
         );
      }
   }

   public static double mineBlocksForLevel(int level) {
      return (Double)WheelConfig.MINE_BLOCKS_BASE.get() * (1.0 + (Double)WheelConfig.MINE_BLOCKS_GROWTH.get() * level);
   }

   public static double wisdomXpForLevel(int level) {
      return (Double)WheelConfig.WISDOM_XP_BASE.get() * (1.0 + (Double)WheelConfig.WISDOM_XP_GROWTH.get() * level);
   }

   static void commitMine(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_MINE.get()) {
         commitLevel(
            player,
            wheel,
            MINE_PENDING,
            AdaptationData.KEY_MINE,
            "mine",
            "mahoraga_wheel.adapt.mine",
            "message.mahoraga_wheel.dig",
            ChatFormatting.GRAY,
            ChatFormatting.DARK_GRAY,
            ConvenienceHandler::mineBlocksForLevel,
            (Double)WheelConfig.MINE_SPEED_PER_LEVEL.get()
         );
      }
   }

   static void commitWisdom(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_WISDOM.get()) {
         commitLevel(
            player,
            wheel,
            WISDOM_PENDING,
            AdaptationData.KEY_WISDOM,
            "wisdom",
            "mahoraga_wheel.adapt.wisdom",
            "message.mahoraga_wheel.xp",
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ConvenienceHandler::wisdomXpForLevel,
            (Double)WheelConfig.WISDOM_XP_PER_LEVEL.get()
         );
      }
   }

   @SubscribeEvent
   public static void onBlockBreak(BlockEvent.BreakEvent event) {
      Player player = event.getPlayer();
      if (!player.level().isClientSide && (Boolean)WheelConfig.ENABLE_MINE.get()) {
         ItemStack wheel = AdaptationHandler.findWheel(player);
         if (!wheel.isEmpty() && AdaptationHandler.isEnhanced(wheel)) {
            MINE_PENDING.merge(player, 1.0, Double::sum);
         }
      }
   }

   @SubscribeEvent
   public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
      if ((Boolean)WheelConfig.ENABLE_MINE.get()) {
         Player player = event.getEntity();
         ItemStack wheel = AdaptationHandler.findWheel(player);
         float boost = entryEffective(wheel, AdaptationData.KEY_MINE);
         if (boost > 0.0F) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + boost));
         }
      }
   }

   @SubscribeEvent
   public static void onPickupXp(PlayerXpEvent.PickupXp event) {
      if (event.getEntity().level().isClientSide || !(Boolean)WheelConfig.ENABLE_WISDOM.get()) {
         return;
      } else {
         Player player = event.getEntity();
         ItemStack wheel = AdaptationHandler.findWheel(player);
         if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel)) {
            return;
         } else {
            ExperienceOrb orb = event.getOrb();
            WISDOM_PENDING.merge(player, (double)Math.max(0, orb.value), Double::sum);
            float bonus = entryEffective(wheel, AdaptationData.KEY_WISDOM);
            if (bonus > 0.0F && orb.value > 0) {
               orb.value += Math.round(orb.value * bonus);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      LivingEntity living = event.getEntity();
      if (living.level().isClientSide || !(Boolean)WheelConfig.ENABLE_KNOCKBACK.get()) {
         return;
      } else {
         ItemStack wheel = AdaptationHandler.findWheel(living);
         if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel)) {
            return;
         } else {
            String key = AdaptationData.KEY_KNOCKBACK;
            float before = AdaptationData.getPercent(wheel, key);
            float reduction = Mth.clamp(entryEffective(wheel, key), 0.0F, 1.0F);
            if (reduction > 0.0F) {
               event.setStrength((float)(event.getStrength() * (1.0 - reduction)));
            }

            if (!(before >= 100.0F)) {
               float gain = AdaptationHandler.rollGain(living, wheel, before, (Double)WheelConfig.KNOCKBACK_HITS.get(), 0.2, 1.0);
               long time = living.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, key, gain, time, "knockback", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, key, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.knockback");
               if (after >= 100.0F) {
                  complete(living, key, ChatFormatting.WHITE, name, Component.translatable("message.mahoraga_wheel.knockback", new Object[]{100}), true);
               } else if ((int)(after / 5.0F) > (int)(before / 5.0F)) {
                  progress(
                     living,
                     key,
                     ChatFormatting.WHITE,
                     ChatFormatting.GRAY,
                     name,
                     before,
                     after,
                     Component.translatable("message.mahoraga_wheel.knockback", new Object[]{Math.round(after)})
                  );
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onHurtTrack(LivingDamageEvent.Pre event) {
      LivingEntity living = event.getEntity();
      if (!living.level().isClientSide) {
         LAST_HURT.put(living, living.level().getGameTime());
      }
   }

   static void tickBreathHold(Player player, ItemStack wheel) {
      if (!(Boolean)WheelConfig.ENABLE_BREATH.get()) {
         return;
      } else {
         float eff = entryEffective(wheel, AdaptationData.KEY_BREATH);
         if (!(eff > 0.0F)) {
            return;
         } else {
            int air = player.getAirSupply();
            int max = player.getMaxAirSupply();
            if (air < max && !player.isSpectator() && player.getRandom().nextFloat() < eff) {
               player.setAirSupply(Math.min(max, air + 1));
            }
         }
      }
   }

   static void tickBreath(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_BREATH.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_BREATH);
         if (!(before >= 100.0F)) {
            if (player.getAirSupply() >= player.getMaxAirSupply() || player.isSpectator()) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.BREATH_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_BREATH, gain, time, "breath", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, AdaptationData.KEY_BREATH, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.breath");
               if (after >= 100.0F) {
                  complete(player, AdaptationData.KEY_BREATH, ChatFormatting.DARK_AQUA, name,
                     Component.translatable("message.mahoraga_wheel.breath", new Object[]{100}), true);
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(player, AdaptationData.KEY_BREATH, ChatFormatting.DARK_AQUA, ChatFormatting.AQUA, name, before, after,
                     Component.translatable("message.mahoraga_wheel.breath", new Object[]{Math.round(after)}));
               }
            }
         }
      }
   }

   static void tickSatietyHold(Player player, ItemStack wheel) {
      if (!(Boolean)WheelConfig.ENABLE_SATIETY.get()) {
         LAST_FOOD.remove(player);
      } else {
         int food = player.getFoodData().getFoodLevel();
         Integer last = LAST_FOOD.put(player, food);
         float eff = entryEffective(wheel, AdaptationData.KEY_SATIETY);
         if (last != null && food < last && eff > 0.0F && player.getRandom().nextFloat() < eff) {
            player.getFoodData().eat(1, 0.0F);
            LAST_FOOD.put(player, food + 1);
         }
      }
   }

   static void tickSatiety(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_SATIETY.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_SATIETY);
         if (!(before >= 100.0F)) {
            if (player.getFoodData().getFoodLevel() >= 20 || player.isSpectator()) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.SATIETY_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_SATIETY, gain, time, "satiety", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, AdaptationData.KEY_SATIETY, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.satiety");
               if (after >= 100.0F) {
                  complete(player, AdaptationData.KEY_SATIETY, ChatFormatting.GOLD, name,
                     Component.translatable("message.mahoraga_wheel.satiety", new Object[]{100}), true);
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(player, AdaptationData.KEY_SATIETY, ChatFormatting.GOLD, ChatFormatting.YELLOW, name, before, after,
                     Component.translatable("message.mahoraga_wheel.satiety", new Object[]{Math.round(after)}));
               }
            }
         }
      }
   }

   public static float lavaSightStrength(Player player) {
      ItemStack wheel = AdaptationHandler.findWheel(player);
      if (wheel.isEmpty()) {
         return 0.0F;
      } else {
         AdaptationData.Entry e = AdaptationData.entry(wheel, AdaptationData.KEY_LAVASIGHT);
         return e == null ? 0.0F : Math.max(0.0F, Math.min(1.0F, e.effective()));
      }
   }

   static void tickLavasight(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_LAVASIGHT.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_LAVASIGHT);
         if (!(before >= 100.0F)) {
            if (!player.isInLava() || !lavaUnlocked(wheel) || player.isSpectator()) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.LAVASIGHT_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_LAVASIGHT, gain, time, "lavasight", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, AdaptationData.KEY_LAVASIGHT, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.lavasight");
               if (after >= 100.0F) {
                  complete(player, AdaptationData.KEY_LAVASIGHT, ChatFormatting.RED, name,
                     Component.translatable("message.mahoraga_wheel.lavasight", new Object[]{100}), true);
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(player, AdaptationData.KEY_LAVASIGHT, ChatFormatting.RED, ChatFormatting.DARK_RED, name, before, after,
                     Component.translatable("message.mahoraga_wheel.lavasight", new Object[]{Math.round(after)}));
               }
            }
         }
      }
   }

   static void tickClimbMove(Player player, ItemStack wheel) {
      if (wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel) || !(Boolean)WheelConfig.ENABLE_CLIMB.get()) {
         return;
      } else if (player.onGround()
         || player.isPassenger()
         || player.getAbilities().flying
         || player.isInWater()
         || player.isInLava()
         || player.isFallFlying()
         || player.isSpectator()) {
         return;
      } else if (!player.horizontalCollision) {
         return;
      } else {
         float strength = Mth.clamp(entryEffective(wheel, AdaptationData.KEY_CLIMB), 0.0F, 1.0F);
         if (!(strength > 0.0F)) {
            return;
         } else {
            Vec3 m = player.getDeltaMovement();
            if (player.isCrouching()) {
               player.setDeltaMovement(m.x, Math.max(m.y, 0.22 * strength), m.z);
            } else {
               player.setDeltaMovement(m.x, Math.max(m.y, -0.15 * (1.0 - strength * 0.8)), m.z);
            }

            player.hasImpulse = true;
            player.fallDistance = 0.0F;
         }
      }
   }

   static void tickClimb(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_CLIMB.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_CLIMB);
         if (!(before >= 100.0F)) {
            if (!player.horizontalCollision || player.onGround() || player.getAbilities().flying || player.isInWater() || player.isInLava() || player.isSpectator()) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.CLIMB_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_CLIMB, gain, time, "climb", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, AdaptationData.KEY_CLIMB, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.climb");
               if (after >= 100.0F) {
                  complete(player, AdaptationData.KEY_CLIMB, ChatFormatting.GREEN, name,
                     Component.translatable("message.mahoraga_wheel.climb", new Object[]{100}), true);
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(player, AdaptationData.KEY_CLIMB, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN, name, before, after,
                     Component.translatable("message.mahoraga_wheel.climb", new Object[]{Math.round(after)}));
               }
            }
         }
      }
   }

   static void tickStep(Player player, ItemStack wheel) {
      if ((Boolean)WheelConfig.ENABLE_STEP.get()) {
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_STEP);
         if (!(before >= 100.0F)) {
            if (!player.horizontalCollision
               || !player.onGround()
               || player.isPassenger()
               || player.getAbilities().flying
               || player.isInWater()
               || player.isInLava()
               || player.isSpectator()) {
               return;
            } else {
               float gain = AdaptationHandler.rollGain(player, wheel, before, (Double)WheelConfig.STEP_SECONDS.get(), 0.2, 1.0);
               long time = player.level().getGameTime();
               float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_STEP, gain, time, "step", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
               AdaptationData.setLimits(wheel, AdaptationData.KEY_STEP, after / 100.0F, 1.0F);
               Component name = Component.translatable("mahoraga_wheel.adapt.step");
               if (after >= 100.0F) {
                  complete(player, AdaptationData.KEY_STEP, ChatFormatting.WHITE, name,
                     Component.translatable("message.mahoraga_wheel.step", new Object[]{100}), true);
               } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
                  progress(player, AdaptationData.KEY_STEP, ChatFormatting.WHITE, ChatFormatting.GRAY, name, before, after,
                     Component.translatable("message.mahoraga_wheel.step", new Object[]{Math.round(after)}));
               }
            }
         }
      }
   }

   static void tickRegen(LivingEntity living, ItemStack wheel) {
      if (!(Boolean)WheelConfig.ENABLE_REGEN.get() || wheel.isEmpty() || !AdaptationHandler.isEnhanced(wheel)) {
         return;
      } else {
         long now = living.level().getGameTime();
         Long lastHurt = LAST_HURT.get(living);
         boolean calm = lastHurt == null || now - lastHurt >= (Integer)WheelConfig.REGEN_COMBAT_TICKS.get();
         float before = AdaptationData.getPercent(wheel, AdaptationData.KEY_REGEN);
         if (!(before >= 100.0F) && calm && living.getHealth() < living.getMaxHealth()) {
            float gain = AdaptationHandler.rollGain(living, wheel, before, (Double)WheelConfig.REGEN_SECONDS.get(), 0.2, 1.0);
            float after = AdaptationData.addPercent(wheel, AdaptationData.KEY_REGEN, gain, now, "regen", null, false, (Integer)WheelConfig.MAX_ENTRIES.get());
            float rate = ((Double)WheelConfig.REGEN_RATE.get()).floatValue();
            AdaptationData.setLimits(wheel, AdaptationData.KEY_REGEN, after / 100.0F * rate, rate);
            Component name = Component.translatable("mahoraga_wheel.adapt.regen");
            if (after >= 100.0F) {
               complete(living, AdaptationData.KEY_REGEN, ChatFormatting.LIGHT_PURPLE, name,
                  Component.translatable("message.mahoraga_wheel.regen", new Object[]{String.format("%.1f", rate)}), true);
            } else if ((int)(after / 2.0F) > (int)(before / 2.0F)) {
               progress(living, AdaptationData.KEY_REGEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, name, before, after,
                  Component.translatable("message.mahoraga_wheel.regen", new Object[]{String.format("%.1f", after / 100.0F * rate)}));
            }
         }

         if (living.getHealth() < living.getMaxHealth() && living.getHealth() > 0.0F) {
            boolean starving = living instanceof Player p && p.getFoodData().getFoodLevel() <= 0;
            if (!starving) {
               float heal = entryEffective(wheel, AdaptationData.KEY_REGEN);
               if (heal > 0.0F) {
                  living.heal(heal);
               }
            }
         }
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

   private static ParticleOptions particleFor(String key) {
      return switch (key) {
         case "C|run" -> ParticleTypes.ELECTRIC_SPARK;
         case "C|dark" -> ParticleTypes.GLOW;
         case "C|lava" -> ParticleTypes.FLAME;
         case "C|swim", "C|breath" -> ParticleTypes.BUBBLE;
         case "C|glide", "C|fly" -> ParticleTypes.CLOUD;
         case "C|regen" -> ParticleTypes.HEART;
         case "C|wisdom" -> ParticleTypes.ENCHANT;
         case "C|knockback" -> ParticleTypes.CRIT;
         default -> ParticleTypes.HAPPY_VILLAGER;
      };
   }

   private static boolean complete(LivingEntity living, String key, ChatFormatting color, Component name, Component extra, boolean mastery) {
      ItemStack wheel = AdaptationHandler.findWheel(living);
      if (AdaptationHandler.damageWheel(living, (Integer)WheelConfig.DURABILITY_PER_CONVENIENCE.get())) {
         return true;
      } else {
         if (mastery && !wheel.isEmpty()) {
            AdaptationData.addMastery(wheel);
         }

         ParticleOptions particle = particleFor(key);
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
