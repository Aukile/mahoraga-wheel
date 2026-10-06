/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntToDoubleFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class AdaptationData {
   public static final String TAG_TURNS = "Turns";
   public static final String TAG_ADAPT = "Adapt";
   public static final float FULL = 100.0F;
   public static final String KEY_DARK = "C|dark";
   public static final String KEY_RUN = "C|run";
   public static final String KEY_SWIM = "C|swim";
   public static final String KEY_LAVA = "C|lava";
   public static final String KEY_GLIDE = "C|glide";
   public static final String KEY_FLY = "C|fly";
   public static final String KEY_JUMP = "C|jump";
   public static final String KEY_LEAP = "C|leap";
   public static final String KEY_BREATH = "C|breath";
   public static final String KEY_MINE = "C|mine";
   public static final String KEY_SATIETY = "C|satiety";
   public static final String KEY_KNOCKBACK = "C|knockback";
   public static final String KEY_LAVASIGHT = "C|lavasight";
   public static final String KEY_CLIMB = "C|climb";
   public static final String KEY_REGEN = "C|regen";
   public static final String KEY_WISDOM = "C|wisdom";
   public static final String KEY_STEP = "C|step";
   public static final String TAG_MASTERY = "Mastery";

   private AdaptationData() {
   }

   public static boolean hasAnyComplete(ItemStack stack, String identity) {
      CompoundTag tag = ModDataComponents.get(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");
         String prefix = identity + "|";

         for (String k : adapt.getAllKeys()) {
            if (k.startsWith(prefix) && adapt.getCompound(k).getFloat("A") >= 100.0F) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static long getLastTime(ItemStack stack, String key) {
      CompoundTag tag = ModDataComponents.get(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");
         return adapt.contains(key, 10) ? adapt.getCompound(key).getLong("T") : Long.MIN_VALUE;
      } else {
         return Long.MIN_VALUE;
      }
   }

   public static int getMastery(ItemStack stack) {
      CompoundTag tag = ModDataComponents.get(stack);
      return tag.getInt("Mastery");
   }

   public static void addMastery(ItemStack stack) {
      CompoundTag tag = ModDataComponents.copy(stack);
      tag.putInt("Mastery", tag.getInt("Mastery") + 1);
      ModDataComponents.set(stack, tag);
   }

   public static int getTurns(ItemStack stack) {
      CompoundTag tag = ModDataComponents.get(stack);
      return tag.getInt("Turns");
   }

   public static float getPercent(ItemStack stack, String key) {
      CompoundTag tag = ModDataComponents.get(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");
         return !adapt.contains(key, 10) ? 0.0F : adapt.getCompound(key).getFloat("A");
      } else {
         return 0.0F;
      }
   }

   public static float addPercent(ItemStack stack, String key, float gain, long gameTime, String damageType, String nameJson, boolean entity, int maxEntries) {
      return addPercent(stack, key, gain, gameTime, damageType, nameJson, entity, false, maxEntries);
   }

   public static float addPercent(
      ItemStack stack, String key, float gain, long gameTime, String damageType, String nameJson, boolean entity, boolean existence, int maxEntries
   ) {
      CompoundTag tag = ModDataComponents.copy(stack);
      CompoundTag adapt = tag.getCompound("Adapt");
      CompoundTag e = adapt.getCompound(key);
      float before = e.getFloat("A");
      float after = Math.min(100.0F, Math.round((before + gain) * 100.0F) / 100.0F);
      e.putFloat("A", after);
      e.putLong("T", gameTime);
      e.putString("D", damageType);
      if (nameJson != null) {
         e.putString("N", nameJson);
      }

      e.putBoolean("E", entity);
      if (existence) {
         e.putBoolean("S", true);
      }

      adapt.put(key, e);
      trim(adapt, maxEntries, key);
      tag.put("Adapt", adapt);
      if (before < 100.0F && after >= 100.0F) {
         tag.putInt("Turns", tag.getInt("Turns") + 1);
      }

      ModDataComponents.set(stack, tag);
      return after;
   }

   private static void trim(CompoundTag adapt, int maxEntries, String keep) {
      while (adapt.size() > maxEntries) {
         String oldest = null;
         long oldestTime = Long.MAX_VALUE;
         boolean oldestComplete = true;

         for (String k : adapt.getAllKeys()) {
            if (!k.equals(keep) && !k.startsWith("C|")) {
               CompoundTag c = adapt.getCompound(k);
               boolean complete = c.getFloat("A") >= 100.0F;
               long t = c.getLong("T");
               if (oldest == null || oldestComplete && !complete || complete == oldestComplete && t < oldestTime) {
                  oldest = k;
                  oldestTime = t;
                  oldestComplete = complete;
               }
            }
         }

         if (oldest == null) {
            break;
         }

         adapt.remove(oldest);
      }
   }

   public static void purgeLegacy(ItemStack stack) {
      CompoundTag tag = ModDataComponents.copy(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");
         List<String> legacy = new ArrayList<>();

         for (String k : adapt.getAllKeys()) {
            if (k.startsWith("E|") || k.startsWith("X|E|")) {
               legacy.add(k);
            }
         }

         legacy.forEach(adapt::remove);
         tag.put("Adapt", adapt);
         ModDataComponents.set(stack, tag);
      }
   }

   public static List<AdaptationData.Entry> entries(ItemStack stack) {
      List<AdaptationData.Entry> list = new ArrayList<>();
      CompoundTag tag = ModDataComponents.get(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");

         for (String k : adapt.getAllKeys()) {
            CompoundTag e = adapt.getCompound(k);
            list.add(entryOf(k, e));
         }

         list.sort(
            Comparator.<AdaptationData.Entry>comparingInt(en -> en.kind().levelBased() ? 0 : 1)
               .thenComparingDouble(en -> -en.percent())
               .thenComparingLong(en -> -en.lastTime())
         );
         return list;
      } else {
         return list;
      }
   }

   private static AdaptationData.Entry entryOf(String k, CompoundTag e) {
      AdaptationData.Kind kind = AdaptationData.Kind.of(k);
      return new AdaptationData.Entry(
         k,
         e.getFloat("A"),
         e.getLong("T"),
         e.getString("D"),
         e.contains("N") ? e.getString("N") : null,
         e.getBoolean("E"),
         e.getBoolean("S"),
         kind,
         e.getInt("L"),
         e.contains("U") ? e.getFloat("U") : Float.NaN,
         !e.getBoolean("NI"),
         e.contains("M") ? e.getFloat("M") : Float.NaN,
         e.contains("K") ? e.getFloat("K") : 1.0F
      );
   }

   public static AdaptationData.Entry entry(ItemStack stack, String key) {
      CompoundTag adapt = adaptTag(stack);
      return adapt != null && adapt.contains(key, 10) ? entryOf(key, adapt.getCompound(key)) : null;
   }

   private static CompoundTag adaptTag(ItemStack stack) {
      CompoundTag tag = ModDataComponents.get(stack);
      return tag.contains("Adapt", 10) ? tag.getCompound("Adapt") : null;
   }

   private static boolean edit(ItemStack stack, String key, Consumer<CompoundTag> fn) {
      CompoundTag tag = ModDataComponents.copy(stack);
      if (tag.contains("Adapt", 10)) {
         CompoundTag adapt = tag.getCompound("Adapt");
         if (adapt.contains(key, 10)) {
            CompoundTag e = adapt.getCompound(key);
            fn.accept(e);
            adapt.put(key, e);
            tag.put("Adapt", adapt);
            ModDataComponents.set(stack, tag);
            return true;
         }
      }

      return false;
   }

   public static void setLimits(ItemStack stack, String key, float max, float scale) {
      edit(stack, key, e -> {
         e.putFloat("M", max);
         e.putFloat("K", scale);
      });
   }

   public static void setUser(ItemStack stack, String key, float value) {
      edit(stack, key, e -> {
         if (Float.isNaN(value)) {
            e.remove("U");
         } else {
            e.putFloat("U", value);
         }
      });
   }

   public static void setImmune(ItemStack stack, String key, boolean immune) {
      edit(stack, key, e -> {
         if (immune) {
            e.remove("NI");
         } else {
            e.putBoolean("NI", true);
         }
      });
   }

   public static int addRunProgress(ItemStack stack, double blocks, long gameTime, IntToDoubleFunction blocksForLevel) {
      return addLevelProgress(stack, KEY_RUN, blocks, gameTime, blocksForLevel, "run");
   }

   public static int addLevelProgress(
      ItemStack stack, String key, double amount, long gameTime, IntToDoubleFunction needForLevel, String damageType
   ) {
      CompoundTag tag = ModDataComponents.copy(stack);
      CompoundTag adapt = tag.getCompound("Adapt");
      CompoundTag e = adapt.getCompound(key);
      int level = e.getInt("L");
      double percent = e.getFloat("A");
      int gained = 0;
      double remaining = amount;

      while (remaining > 0.0 && gained < 100) {
         double need = needForLevel.applyAsDouble(level) * (100.0 - percent) / 100.0;
         if (remaining >= need) {
            remaining -= need;
            level++;
            gained++;
            percent = 0.0;
         } else {
            percent += remaining / needForLevel.applyAsDouble(level) * 100.0;
            remaining = 0.0;
         }
      }

      e.putFloat("A", (float)(Math.round(percent * 100.0) / 100.0));
      e.putInt("L", level);
      e.putLong("T", gameTime);
      e.putString("D", damageType);
      adapt.put(key, e);
      tag.put("Adapt", adapt);
      if (gained > 0) {
         tag.putInt("Turns", tag.getInt("Turns") + gained);
      }

      ModDataComponents.set(stack, tag);
      return gained;
   }

   public static void reset(ItemStack stack) {
      CompoundTag tag = ModDataComponents.copy(stack);
      if (tag.contains("Adapt")) {
         tag.remove("Adapt");
         ModDataComponents.set(stack, tag);
      }
   }

   public record Entry(
      String key,
      float percent,
      long lastTime,
      String damageType,
      String nameJson,
      boolean entity,
      boolean existence,
      AdaptationData.Kind kind,
      int level,
      float user,
      boolean immune,
      float max,
      float scale
   ) {
      public boolean complete() {
         return !this.kind.levelBased() && this.percent >= 100.0F;
      }

      public float effective() {
         float m = Float.isNaN(this.max) ? 0.0F : this.max;
         return Float.isNaN(this.user) ? m : Math.min(this.user, m);
      }

      public boolean immuneActive() {
         return this.kind.reduction() && this.complete() && this.immune;
      }
   }

   public static enum Kind {
      ATTACK,
      ENVIRONMENT,
      EXISTENCE,
      DEBUFF,
      DARK,
      RUN,
      SWIM,
      LAVA,
      GLIDE,
      FLY,
      JUMP,
      LEAP,
      BREATH,
      MINE,
      SATIETY,
      KNOCKBACK,
      LAVASIGHT,
      CLIMB,
      REGEN,
      WISDOM,
      STEP;

      public static AdaptationData.Kind of(String key) {
         if (key.startsWith("X|")) {
            return EXISTENCE;
         } else if (key.startsWith("P|")) {
            return DEBUFF;
         } else if (key.equals("C|dark")) {
            return DARK;
         } else if (key.equals("C|run")) {
            return RUN;
         } else if (key.equals("C|swim")) {
            return SWIM;
         } else if (key.equals("C|lava")) {
            return LAVA;
         } else if (key.equals("C|glide")) {
            return GLIDE;
         } else if (key.equals("C|fly")) {
            return FLY;
         } else if (key.equals("C|jump")) {
            return JUMP;
         } else if (key.equals("C|leap")) {
            return LEAP;
         } else if (key.equals("C|breath")) {
            return BREATH;
         } else if (key.equals("C|mine")) {
            return MINE;
         } else if (key.equals("C|satiety")) {
            return SATIETY;
         } else if (key.equals("C|knockback")) {
            return KNOCKBACK;
         } else if (key.equals("C|lavasight")) {
            return LAVASIGHT;
         } else if (key.equals("C|climb")) {
            return CLIMB;
         } else if (key.equals("C|regen")) {
            return REGEN;
         } else if (key.equals("C|wisdom")) {
            return WISDOM;
         } else if (key.equals("C|step")) {
            return STEP;
         } else {
            return key.startsWith("D|") ? ENVIRONMENT : ATTACK;
         }
      }

      public boolean reduction() {
         return this == ATTACK || this == ENVIRONMENT || this == DEBUFF;
      }

      public boolean convenience() {
         return this == DEBUFF
            || this == DARK
            || this == RUN
            || this == SWIM
            || this == LAVA
            || this == GLIDE
            || this == FLY
            || this == JUMP
            || this == LEAP
            || this == BREATH
            || this == MINE
            || this == SATIETY
            || this == KNOCKBACK
            || this == LAVASIGHT
            || this == CLIMB
            || this == REGEN
            || this == WISDOM
            || this == STEP;
      }

      public boolean levelBased() {
         return this == RUN || this == SWIM || this == LAVA || this == JUMP || this == LEAP || this == MINE || this == WISDOM;
      }
   }
}
