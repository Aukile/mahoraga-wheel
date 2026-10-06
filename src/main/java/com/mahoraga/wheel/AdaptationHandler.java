/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import com.mahoraga.wheel.network.ModNetwork;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class AdaptationHandler {
   private static final Map<LivingEntity, Long> LAST_NULLIFY_FX = new WeakHashMap<>();
   private static final Map<LivingEntity, Float> PENDING_HEAL = new WeakHashMap<>();
   private static final int EXISTENCE_COOLDOWN = 10;

   private AdaptationHandler() {
   }

   public static ItemStack findWheel(LivingEntity living) {
      return findWheelSlot(living).map(AdaptationHandler.WheelSlot::stack).orElse(ItemStack.EMPTY);
   }

   public static boolean isEnhanced(ItemStack stack) {
      return stack.getItem() instanceof MahoragaWheelItem wheel && wheel.isEnhanced();
   }

   public static Optional<AdaptationHandler.WheelSlot> findWheelSlot(LivingEntity living) {
      ItemStack head = living.getItemBySlot(EquipmentSlot.HEAD);
      return head.getItem() instanceof MahoragaWheelItem
         ? Optional.of(new AdaptationHandler.WheelSlot(head, () -> living.onEquippedItemBroken(head.getItem(), EquipmentSlot.HEAD)))
         : findCurioSlot(living).map(r -> new AdaptationHandler.WheelSlot(r.stack(), () -> CuriosApi.broadcastCurioBreakEvent(r.slotContext())));
   }

   private static Optional<SlotResult> findCurioSlot(LivingEntity living) {
      var invOpt = CuriosApi.getCuriosInventory(living);
      if (invOpt.isEmpty()) {
         return Optional.empty();
      } else {
         var inv = invOpt.get();
         for (Item wheel : new Item[]{(Item)ModItems.MAHORAGA_WHEEL.get(), (Item)ModItems.MAHORAGA_WHEEL_WEAKENED.get()}) {
            Optional<SlotResult> r = inv.findFirstCurio(wheel);
            if (r.isPresent()) {
               return r;
            }
         }

         return Optional.empty();
      }
   }

   public static boolean hasWheelInCurios(LivingEntity living) {
      return findCurioSlot(living).isPresent();
   }

   public static boolean isExempt(LivingEntity living) {
      if (living instanceof Player player) {
         String name = player.getGameProfile().getName();

         for (String s : WheelConfig.exemptPlayers()) {
            if (s.equalsIgnoreCase(name)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean damageWheel(LivingEntity living, int amount) {
      if (amount > 0 && !isExempt(living)) {
         Optional<AdaptationHandler.WheelSlot> slot = findWheelSlot(living);
         if (slot.isEmpty()) {
            return false;
         } else {
            ItemStack wheel = slot.get().stack();
            if (!wheel.isDamageableItem()) {
               return false;
            } else {
               if (!(living.level() instanceof ServerLevel serverLevel)) {
                  return false;
               }

               boolean[] broke = new boolean[1];
               wheel.hurtAndBreak(amount, serverLevel, living, item -> {
                  broke[0] = true;
                  slot.get().onBreak().run();
               });
               if (broke[0]) {
                  if (!wheel.isEmpty()) {
                     wheel.shrink(1);
                  }

                  wheel.setDamageValue(0);
                  hud(
                     living,
                     "broken",
                     Component.literal("☸ ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("message.mahoraga_wheel.broken").withStyle(ChatFormatting.DARK_RED))
                  );
               }

               return broke[0];
            }
         }
      } else {
         return false;
      }
   }

   public static String damageTypeId(DamageSource source) {
      return source.typeHolder().unwrapKey().map(k -> k.location().toString()).orElse("minecraft:" + source.getMsgId());
   }

   public static boolean isAdaptable(DamageSource source) {
      return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? false : !((List)WheelConfig.BLACKLIST.get()).contains(damageTypeId(source));
   }

   public static String identityOf(Entity entity) {
      ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return "T|" + typeId;
   }

   private static String nameJsonOf(Entity entity) {
      return Serializer.toJson(entity.getType().getDescription(), RegistryAccess.EMPTY);
   }

   public static AdaptationHandler.AdaptKey keyFor(DamageSource source, LivingEntity victim) {
      String type = damageTypeId(source);
      Entity attacker = source.getEntity();
      if (attacker == null) {
         attacker = source.getDirectEntity();
      }

      if (attacker != null && attacker != victim) {
         String identity = identityOf(attacker);
         float health = attacker instanceof LivingEntity le ? le.getMaxHealth() : ((Double)WheelConfig.REFERENCE_HEALTH.get()).floatValue();
         return new AdaptationHandler.AdaptKey(identity + "|" + type, identity, type, true, nameJsonOf(attacker), health);
      } else {
         return new AdaptationHandler.AdaptKey("D|" + type, null, type, false, null, 0.0F);
      }
   }

   public static float reductionFor(float percent) {
      if (percent >= 100.0F) {
         return 1.0F;
      } else {
         float start;
         try {
            start = ((Double)WheelConfig.REDUCTION_START.get()).floatValue();
         } catch (IllegalStateException var3) {
            start = 80.0F;
         }

         return percent <= start ? 0.0F : Mth.clamp((percent - start) / (100.0F - start), 0.0F, 1.0F);
      }
   }

   public static double expectedHits(float health) {
      double ref = (Double)WheelConfig.REFERENCE_HEALTH.get();
      double hp = Math.max(1.0, (double)health);
      double n = (Double)WheelConfig.BASE_HITS.get() * Math.pow(hp / ref, (Double)WheelConfig.HEALTH_EXPONENT.get());
      return Mth.clamp(n, (Double)WheelConfig.MIN_HITS.get(), (Double)WheelConfig.MAX_HITS.get());
   }

   public static double slowShare(float health) {
      double ref = (Double)WheelConfig.REFERENCE_HEALTH.get();
      double cap = Math.max(ref + 1.0, (Double)WheelConfig.SLOW_SHARE_HEALTH.get());
      if (health <= ref) {
         return 0.2;
      } else {
         double t = Mth.clamp(Math.log(health / ref) / Math.log(cap / ref), 0.0, 1.0);
         return 0.2 + ((Double)WheelConfig.SLOW_SHARE_MAX.get() - 0.2) * t;
      }
   }

   public static float masteryMultiplier(ItemStack wheel) {
      double raw = (Double)WheelConfig.MASTERY_PER_ADAPT.get() * AdaptationData.getMastery(wheel);
      boolean uncapped = isEnhanced(wheel) && (Boolean)WheelConfig.ENHANCED_UNCAPPED_MASTERY.get();
      double bonus = uncapped ? raw : Math.min((Double)WheelConfig.MASTERY_MAX.get(), raw);
      return (float)(1.0 + bonus);
   }

   static float rollGain(LivingEntity living, ItemStack wheel, float before, double hits, double slowShare, double factor) {
      double avg = before < 80.0F ? 80.0 / (hits * (1.0 - slowShare)) : 20.0 / (hits * slowShare);
      double r = (Double)WheelConfig.GAIN_RANDOMNESS.get();
      double roll = 1.0 - r + living.getRandom().nextDouble() * 2.0 * r;
      return (float)(avg * roll * factor * masteryMultiplier(wheel));
   }

   public static double damageFactor(float amount) {
      double ref = (Double)WheelConfig.DAMAGE_REFERENCE.get();
      double f = Math.pow(Math.max(0.0, (double)amount) / ref, (Double)WheelConfig.DAMAGE_EXPONENT.get());
      return Mth.clamp(f, (Double)WheelConfig.DAMAGE_FACTOR_MIN.get(), (Double)WheelConfig.DAMAGE_FACTOR_MAX.get());
   }

   public static float reductionMax(float percent) {
      return percent >= 100.0F ? 1.0F : reductionFor(percent);
   }

   public static float effectiveReduction(ItemStack wheel, String key, float percent) {
      float max = reductionMax(percent);
      AdaptationData.Entry e = AdaptationData.entry(wheel, key);
      return e != null && !Float.isNaN(e.user()) ? Math.min(e.user(), max) : max;
   }

   public static boolean immuneActive(ItemStack wheel, String key) {
      AdaptationData.Entry e = AdaptationData.entry(wheel, key);
      return e != null && e.immuneActive();
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onIncomingDamage(LivingIncomingDamageEvent event) {
      LivingEntity living = event.getEntity();
      if (!living.level().isClientSide) {
         DamageSource source = event.getSource();
         if (isAdaptable(source)) {
            ItemStack wheel = findWheel(living);
            if (!wheel.isEmpty()) {
               AdaptationData.purgeLegacy(wheel);
               AdaptationHandler.AdaptKey key = keyFor(source, living);
               if (immuneActive(wheel, key.id())) {
                  event.setCanceled(true);
                  nullifyEffect(living, key);
                  progressExistence(living, wheel, key, event.getAmount());
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onDamagePre(LivingDamageEvent.Pre event) {
      LivingEntity living = event.getEntity();
      if (!living.level().isClientSide) {
         DamageSource source = event.getSource();
         if (isAdaptable(source)) {
            ItemStack wheel = findWheel(living);
            if (!wheel.isEmpty()) {
               AdaptationData.purgeLegacy(wheel);
               AdaptationHandler.AdaptKey key = keyFor(source, living);
               float before = AdaptationData.getPercent(wheel, key.id());
               float rawAmount = event.getNewDamage();
               float reduction = effectiveReduction(wheel, key.id(), before);
               if (reduction >= 1.0F && immuneActive(wheel, key.id())) {
                  event.setNewDamage(0.0F);
               } else {
                  float finalAmount = rawAmount * (1.0F - reduction);
                  event.setNewDamage(finalAmount);
                  if (before < 100.0F) {
                     double factor = damageFactor(rawAmount);
                     float gain = key.entity()
                        ? rollGain(living, wheel, before, expectedHits(key.health()), slowShare(key.health()), factor)
                        : rollGain(living, wheel, before, (Double)WheelConfig.ENVIRONMENT_HITS.get(), 0.2, factor);
                     if (gain > 0.0F) {
                        long time = living.level().getGameTime();
                        float after = AdaptationData.addPercent(
                           wheel, key.id(), gain, time, key.damageType(), key.nameJson(), key.entity(), (Integer)WheelConfig.MAX_ENTRIES.get()
                        );
                        AdaptationData.setLimits(wheel, key.id(), reductionMax(after), 1.0F);
                        boolean completed = after >= 100.0F;
                        int cost = (Integer)WheelConfig.DURABILITY_PER_HIT.get() + (completed ? (Integer)WheelConfig.DURABILITY_PER_ADAPT.get() : 0);
                        if (damageWheel(living, cost)) {
                           return;
                        }

                        if (completed) {
                           AdaptationData.addMastery(wheel);
                           onAdaptComplete(living, key, false);
                           if ((Boolean)WheelConfig.HEAL_ON_TURN.get()) {
                              PENDING_HEAL.merge(living, finalAmount, Float::sum);
                           }
                        } else {
                           hud(
                              living,
                              key.id(),
                              Component.literal("☸ ")
                                 .withStyle(ChatFormatting.GOLD)
                                 .append(Component.translatable("message.mahoraga_wheel.analyzing").withStyle(ChatFormatting.GOLD))
                                 .append(label(describe(key.entity(), key.damageType(), key.nameJson())))
                                 .append(bar(after))
                                 .append(Component.literal(String.format(" %.1f%%", after)).withStyle(ChatFormatting.YELLOW))
                                 .append(Component.literal(String.format(" +%.1f", after - before)).withStyle(ChatFormatting.GRAY))
                                 .append(reductionHint(after))
                           );
                        }
                     }
                  }

                  progressExistence(living, wheel, key, rawAmount);
               }
            }
         }
      }
   }

   public static float existenceBonus(float percent) {
      return Mth.clamp(percent / 100.0F, 0.0F, 1.0F) * WheelConfig.existenceBonus();
   }

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void onDealDamage(LivingDamageEvent.Pre event) {
      LivingEntity target = event.getEntity();
      if (!target.level().isClientSide) {
         if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != target) {
            ItemStack wheel = findWheel(attacker);
            if (!wheel.isEmpty()) {
               String xid = "X|" + identityOf(target);
               float bonus = existenceBonus(AdaptationData.getPercent(wheel, xid));
               AdaptationData.Entry xe = AdaptationData.entry(wheel, xid);
               if (xe != null && !Float.isNaN(xe.user())) {
                  bonus = Math.min(bonus, xe.user());
               }

               if (bonus > 0.0F) {
                  event.setNewDamage(event.getNewDamage() * (1.0F + bonus));
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onEntityTick(EntityTickEvent.Post event) {
      if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide && !PENDING_HEAL.isEmpty()) {
         Float heal = PENDING_HEAL.remove(living);
         if (heal != null && heal > 0.0F && living.isAlive()) {
            living.heal(heal);
         }
      }
   }

   private static void progressExistence(LivingEntity living, ItemStack wheel, AdaptationHandler.AdaptKey key, float amount) {
      if ((Boolean)WheelConfig.ENABLE_EXISTENCE.get() && key.entity() && key.identity() != null) {
         if (!wheel.isEmpty() && findWheel(living).equals(wheel)) {
            String xid = key.existenceId();
            float before = AdaptationData.getPercent(wheel, xid);
            if (!(before >= 100.0F)) {
               if (AdaptationData.hasAnyComplete(wheel, key.identity())) {
                  long time = living.level().getGameTime();
                  long last = AdaptationData.getLastTime(wheel, xid);
                  if (last == Long.MIN_VALUE || time - last >= 10L) {
                     float gain = rollGain(
                        living,
                        wheel,
                        before,
                        expectedHits(key.health()) * (Double)WheelConfig.EXISTENCE_HITS_MULTIPLIER.get(),
                        slowShare(key.health()),
                        damageFactor(amount)
                     );
                     if (!(gain <= 0.0F)) {
                        float after = AdaptationData.addPercent(wheel, xid, gain, time, "", key.nameJson(), true, true, (Integer)WheelConfig.MAX_ENTRIES.get());
                        AdaptationData.setLimits(wheel, xid, existenceBonus(after), WheelConfig.existenceBonus());
                        boolean completed = after >= 100.0F;
                        if (!completed || !damageWheel(living, (Integer)WheelConfig.DURABILITY_PER_ADAPT.get())) {
                           if (completed) {
                              onAdaptComplete(living, key, true);
                           } else {
                              hud(
                                 living,
                                 xid,
                                 Component.literal("☸ ")
                                    .withStyle(ChatFormatting.DARK_PURPLE)
                                    .append(Component.translatable("message.mahoraga_wheel.existence_analyzing").withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .append(purpleLabel(nameFromJson(key.nameJson())))
                                    .append(bar(after, ChatFormatting.LIGHT_PURPLE))
                                    .append(Component.literal(String.format(" %.1f%%", after)).withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .append(Component.literal(String.format(" +%.1f", after - before)).withStyle(ChatFormatting.DARK_PURPLE))
                                    .append(
                                       Component.literal("  ")
                                          .append(
                                             Component.translatable("message.mahoraga_wheel.bonus", new Object[]{Math.round(existenceBonus(after) * 100.0F)})
                                          )
                                          .withStyle(ChatFormatting.LIGHT_PURPLE)
                                    )
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void playTurn(LivingEntity living, ParticleOptions particle, float pitch) {
      ServerLevel level = (ServerLevel)living.level();
      level.playSound(null, living.getX(), living.getY(), living.getZ(), (SoundEvent)ModSounds.WHEEL_TURN.get(), SoundSource.PLAYERS, 1.0F, pitch);
      double cy = living.getY() + living.getBbHeight() + 0.45;
      int n = 32;
      double r = particle == ParticleTypes.END_ROD ? 0.5 : 0.65;

      for (int i = 0; i < n; i++) {
         double a = (Math.PI * 2) * i / n;
         level.sendParticles(particle, living.getX() + Math.cos(a) * r, cy, living.getZ() + Math.sin(a) * r, 1, 0.0, 0.02, 0.0, 0.01);
      }
   }

   private static void onAdaptComplete(LivingEntity living, AdaptationHandler.AdaptKey key, boolean existence) {
      playTurn(living, existence ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD, existence ? 0.8F : 1.0F);
      if (existence) {
         int bonus = Math.round(WheelConfig.existenceBonus() * 100.0F);
         hud(
            living,
            key.existenceId(),
            Component.literal("☸ ")
               .withStyle(ChatFormatting.DARK_PURPLE)
               .append(Component.translatable("message.mahoraga_wheel.existence_adapted").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(purpleLabel(nameFromJson(key.nameJson())))
               .append(bar(100.0F, ChatFormatting.LIGHT_PURPLE))
               .append(
                  Component.literal("  ")
                     .append(Component.translatable("message.mahoraga_wheel.bonus", new Object[]{bonus}))
                     .withStyle(ChatFormatting.LIGHT_PURPLE)
               )
         );
      } else {
         hud(
            living,
            key.id(),
            Component.literal("☸ ")
               .withStyle(ChatFormatting.GOLD)
               .append(Component.translatable("message.mahoraga_wheel.adapted").withStyle(ChatFormatting.RED))
               .append(label(describe(key.entity(), key.damageType(), key.nameJson())))
               .append(bar(100.0F))
               .append(Component.literal(" 100%").withStyle(ChatFormatting.RED))
               .append(
                  Component.literal("  ")
                     .append(Component.translatable("message.mahoraga_wheel.mastery", new Object[]{AdaptationData.getMastery(findWheel(living))}))
                     .withStyle(ChatFormatting.GOLD)
               )
         );
      }
   }

   private static void nullifyEffect(LivingEntity living, AdaptationHandler.AdaptKey key) {
      long now = living.level().getGameTime();
      Long last = LAST_NULLIFY_FX.get(living);
      if (last == null || now - last >= 10L) {
         LAST_NULLIFY_FX.put(living, now);
         ServerLevel level = (ServerLevel)living.level();
         level.playSound(
            null, living.getX(), living.getY(), living.getZ(), (SoundEvent)ModSounds.WHEEL_NULLIFY.get(), SoundSource.PLAYERS, 0.6F, 1.0F
         );
         level.sendParticles(ParticleTypes.ENCHANT, living.getX(), living.getY() + living.getBbHeight() + 0.4, living.getZ(), 8, 0.3, 0.05, 0.3, 0.2);
         hud(
            living,
            key.id(),
            Component.literal("☸ ")
               .withStyle(ChatFormatting.GOLD)
               .append(Component.translatable("message.mahoraga_wheel.nullified").withStyle(ChatFormatting.AQUA))
               .append(label(describe(key.entity(), key.damageType(), key.nameJson())))
         );
      }
   }

   public static void hud(LivingEntity living, String key, Component text) {
      if (living instanceof ServerPlayer sp && (Boolean)WheelConfig.SHOW_MESSAGES.get()) {
         ModNetwork.sendHud(sp, key, text);
      }
   }

   static Component reductionHint(float percent) {
      float r = reductionFor(percent);
      return r <= 0.0F
         ? Component.empty()
         : Component.literal("  ")
            .append(Component.translatable("message.mahoraga_wheel.reduction", new Object[]{Math.round(r * 100.0F)}))
            .withStyle(ChatFormatting.AQUA);
   }

   private static Component purpleLabel(Component inner) {
      return Component.literal(" 【")
         .withStyle(ChatFormatting.LIGHT_PURPLE)
         .append(inner.copy().withStyle(ChatFormatting.LIGHT_PURPLE))
         .append(Component.literal("】 ").withStyle(ChatFormatting.LIGHT_PURPLE));
   }

   static Component label(Component inner) {
      return Component.literal(" 【")
         .withStyle(ChatFormatting.WHITE)
         .append(inner.copy().withStyle(ChatFormatting.WHITE))
         .append(Component.literal("】 ").withStyle(ChatFormatting.WHITE));
   }

   public static Component damageName(String damageType) {
      String langKey = "mahoraga_wheel.damage." + damageType.replace(':', '.');
      return Component.translatableWithFallback(langKey, damageType);
   }

   public static Component nameFromJson(String nameJson) {
      Component name = null;
      if (nameJson != null) {
         try {
            name = Serializer.fromJson(nameJson, RegistryAccess.EMPTY);
         } catch (Exception var3) {
         }
      }

      return (Component)(name == null ? Component.literal("?") : name);
   }

   public static Component describe(boolean entity, String damageType, String nameJson) {
      Component dmg = damageName(damageType);
      return (Component)(entity && nameJson != null
         ? Component.empty().append(nameFromJson(nameJson)).append(Component.literal(" · ")).append(dmg)
         : dmg);
   }

   public static Component describeExistence(String nameJson) {
      return nameFromJson(nameJson);
   }

   public static Component describe(AdaptationData.Entry e) {
      return (Component)(switch (e.kind()) {
         case EXISTENCE -> describeExistence(e.nameJson());
         case DEBUFF -> effectName(e.damageType());
         case DARK -> Component.translatable("mahoraga_wheel.adapt.dark");
         case RUN -> Component.translatable("mahoraga_wheel.adapt.run");
         case SWIM -> Component.translatable("mahoraga_wheel.adapt.swim");
         case LAVA -> Component.translatable("mahoraga_wheel.adapt.lava");
         case GLIDE -> Component.translatable("mahoraga_wheel.adapt.glide");
         case FLY -> Component.translatable("mahoraga_wheel.adapt.fly");
         case JUMP -> Component.translatable("mahoraga_wheel.adapt.jump");
         case LEAP -> Component.translatable("mahoraga_wheel.adapt.leap");
         case BREATH -> Component.translatable("mahoraga_wheel.adapt.breath");
         case MINE -> Component.translatable("mahoraga_wheel.adapt.mine");
         case SATIETY -> Component.translatable("mahoraga_wheel.adapt.satiety");
         case KNOCKBACK -> Component.translatable("mahoraga_wheel.adapt.knockback");
         case LAVASIGHT -> Component.translatable("mahoraga_wheel.adapt.lavasight");
         case CLIMB -> Component.translatable("mahoraga_wheel.adapt.climb");
         case REGEN -> Component.translatable("mahoraga_wheel.adapt.regen");
         case WISDOM -> Component.translatable("mahoraga_wheel.adapt.wisdom");
         case STEP -> Component.translatable("mahoraga_wheel.adapt.step");
         default -> describe(e.entity(), e.damageType(), e.nameJson());
      });
   }

   public static Component effectName(String effectId) {
      ResourceLocation id = ResourceLocation.tryParse(effectId);
      MobEffect effect = id == null ? null : BuiltInRegistries.MOB_EFFECT.getOptional(id).orElse(null);
      return (Component)(effect == null ? Component.literal(effectId) : effect.getDisplayName());
   }

   public static Component bar(float percent) {
      return bar(percent, null);
   }

   public static Component bar(float percent, ChatFormatting color) {
      MutableComponent c = Component.empty();
      int segments = 10;
      int filled = (int)Math.floor(percent / 100.0F * segments + 1.0E-4F);
      boolean complete = percent >= 100.0F;
      ChatFormatting on = color != null ? color : (complete ? ChatFormatting.RED : ChatFormatting.YELLOW);

      for (int i = 0; i < segments; i++) {
         c.append(Component.literal("■").withStyle(i < filled ? on : ChatFormatting.DARK_GRAY));
      }

      return c;
   }

   public record AdaptKey(String id, String identity, String damageType, boolean entity, String nameJson, float health) {
      public String existenceId() {
         return this.identity == null ? null : "X|" + this.identity;
      }
   }

   public record WheelSlot(ItemStack stack, Runnable onBreak) {
   }
}
