/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class WheelConfig {
   public static final ModConfigSpec SPEC;
   public static final DoubleValue BASE_HITS;
   public static final DoubleValue REFERENCE_HEALTH;
   public static final DoubleValue HEALTH_EXPONENT;
   public static final DoubleValue MIN_HITS;
   public static final DoubleValue MAX_HITS;
   public static final DoubleValue SLOW_SHARE_MAX;
   public static final DoubleValue SLOW_SHARE_HEALTH;
   public static final DoubleValue ENVIRONMENT_HITS;
   public static final DoubleValue GAIN_RANDOMNESS;
   public static final DoubleValue REDUCTION_START;
   public static final DoubleValue MASTERY_PER_ADAPT;
   public static final DoubleValue MASTERY_MAX;
   public static final BooleanValue ENHANCED_UNCAPPED_MASTERY;
   public static final DoubleValue DAMAGE_REFERENCE;
   public static final DoubleValue DAMAGE_EXPONENT;
   public static final DoubleValue DAMAGE_FACTOR_MIN;
   public static final DoubleValue DAMAGE_FACTOR_MAX;
   public static final BooleanValue HEAL_ON_TURN;
   public static final BooleanValue SHOW_MESSAGES;
   public static final BooleanValue ENABLE_EXISTENCE;
   public static final DoubleValue EXISTENCE_HITS_MULTIPLIER;
   public static final DoubleValue EXISTENCE_DAMAGE_BONUS;
   public static final IntValue MAX_ENTRIES;
   public static final ConfigValue<List<? extends String>> BLACKLIST;
   public static final BooleanValue ENABLE_DARK;
   public static final IntValue DARK_LIGHT_LEVEL;
   public static final DoubleValue DARK_SECONDS;
   public static final DoubleValue DARK_MAX_STRENGTH;
   public static final BooleanValue ENABLE_DEBUFF;
   public static final DoubleValue DEBUFF_SECONDS;
   public static final BooleanValue DEBUFF_ONE_AT_A_TIME;
   public static final ConfigValue<List<? extends String>> DEBUFF_BLACKLIST;
   public static final BooleanValue ENABLE_RUN;
   public static final DoubleValue RUN_BLOCKS_BASE;
   public static final DoubleValue RUN_BLOCKS_GROWTH;
   public static final DoubleValue RUN_SPEED_PER_LEVEL;
   public static final BooleanValue ENABLE_SWIM;
   public static final DoubleValue SWIM_BLOCKS_BASE;
   public static final DoubleValue SWIM_BLOCKS_GROWTH;
   public static final DoubleValue SWIM_SPEED_PER_LEVEL;
   public static final BooleanValue ENABLE_LAVA;
   public static final DoubleValue LAVA_BLOCKS_BASE;
   public static final DoubleValue LAVA_BLOCKS_GROWTH;
   public static final DoubleValue LAVA_SPEED_PER_LEVEL;
   public static final BooleanValue LAVA_REQUIRES_ADAPT;
   public static final BooleanValue ENABLE_GLIDE;
   public static final DoubleValue GLIDE_SECONDS;
   public static final DoubleValue GLIDE_MAX_STRENGTH;
   public static final BooleanValue GLIDE_REQUIRE_SNEAK;
   public static final BooleanValue ENABLE_FLY;
   public static final DoubleValue FLY_SECONDS;
   public static final BooleanValue ENABLE_JUMP;
   public static final DoubleValue JUMP_BLOCKS_BASE;
   public static final DoubleValue JUMP_BLOCKS_GROWTH;
   public static final DoubleValue JUMP_HEIGHT_PER_LEVEL;
   public static final BooleanValue ENABLE_LEAP;
   public static final DoubleValue LEAP_BLOCKS_BASE;
   public static final DoubleValue LEAP_BLOCKS_GROWTH;
   public static final DoubleValue LEAP_SPEED_PER_LEVEL;
   public static final BooleanValue ENABLE_BREATH;
   public static final DoubleValue BREATH_SECONDS;
   public static final BooleanValue ENABLE_MINE;
   public static final DoubleValue MINE_BLOCKS_BASE;
   public static final DoubleValue MINE_BLOCKS_GROWTH;
   public static final DoubleValue MINE_SPEED_PER_LEVEL;
   public static final BooleanValue ENABLE_SATIETY;
   public static final DoubleValue SATIETY_SECONDS;
   public static final BooleanValue ENABLE_KNOCKBACK;
   public static final DoubleValue KNOCKBACK_HITS;
   public static final BooleanValue ENABLE_LAVASIGHT;
   public static final DoubleValue LAVASIGHT_SECONDS;
   public static final BooleanValue ENABLE_CLIMB;
   public static final DoubleValue CLIMB_SECONDS;
   public static final BooleanValue ENABLE_REGEN;
   public static final DoubleValue REGEN_SECONDS;
   public static final DoubleValue REGEN_RATE;
   public static final IntValue REGEN_COMBAT_TICKS;
   public static final BooleanValue ENABLE_WISDOM;
   public static final DoubleValue WISDOM_XP_BASE;
   public static final DoubleValue WISDOM_XP_GROWTH;
   public static final DoubleValue WISDOM_XP_PER_LEVEL;
   public static final BooleanValue ENABLE_STEP;
   public static final DoubleValue STEP_SECONDS;
   public static final IntValue MAX_DURABILITY;
   public static final IntValue DURABILITY_PER_CONVENIENCE;
   public static final IntValue DURABILITY_PER_ADAPT;
   public static final IntValue DURABILITY_PER_HIT;
   public static final ConfigValue<List<? extends String>> EXEMPT_PLAYERS;
   public static final BooleanValue ENABLE_CURIO_SLOT;
   public static final ModConfigSpec CLIENT_SPEC;
   public static final DoubleValue DEGREES_PER_TURN;
   public static final DoubleValue TURN_SECONDS;
   public static final BooleanValue HUD_ENABLED;
   public static final EnumValue<WheelConfig.HudAnchor> HUD_ANCHOR;
   public static final IntValue HUD_OFFSET_X;
   public static final IntValue HUD_OFFSET_Y;
   public static final DoubleValue HUD_SCALE;
   public static final DoubleValue HUD_SECONDS;
   public static final IntValue HUD_MAX_LINES;

   static double safe(DoubleValue v, double def) {
      try {
         return (Double)v.get();
      } catch (IllegalStateException var4) {
         return def;
      }
   }

   public static boolean curioSlotEnabled() {
      try {
         return (Boolean)ENABLE_CURIO_SLOT.get();
      } catch (IllegalStateException var1) {
         return false;
      }
   }

   public static float existenceBonus() {
      try {
         return ((Double)EXISTENCE_DAMAGE_BONUS.get()).floatValue();
      } catch (IllegalStateException var1) {
         return 0.5F;
      }
   }

   public static int maxDurability() {
      try {
         return (Integer)MAX_DURABILITY.get();
      } catch (IllegalStateException var1) {
         return 100;
      }
   }

   public static List<? extends String> exemptPlayers() {
      try {
         return (List<? extends String>)EXEMPT_PLAYERS.get();
      } catch (IllegalStateException var1) {
         return List.of("Ankrya");
      }
   }

   public static float degreesPerTurn() {
      return (float)safe(DEGREES_PER_TURN, 360.0);
   }

   public static float turnTicks() {
      return (float)safe(TURN_SECONDS, 1.05) * 20.0F;
   }

   static {
      Builder b = new Builder();
      b.comment("魔虚罗法阵 - 佩戴位置").push("slot");
      ENABLE_CURIO_SLOT = b.comment(
            new String[]{
               "法阵可以戴在【头盔栏】，或专属的「法阵」饰品栏（Curios）。",
               "「法阵」饰品栏始终存在，只是默认 0 格（开局看不到），可以随时直接添加，例如：",
               "  /curios add mahoraga_wheel <玩家> [数量]      （/curios remove 移除）",
               "  或其他模组 / 数据包给予的饰品栏加成（curios:mahoraga_wheel 属性）。",
               "设为 true = 开局自动给每个玩家 1 格「法阵」饰品栏。修改后需重进世界 / 重启服务器（或 /reload）。"
            }
         )
         .define("enableCurioSlot", false);
      b.pop();
      b.comment(
            new String[]{
               "魔虚罗法阵 - 适应设置",
               "每次受到某种伤害，该伤害的适应度随机增长一点百分比；所需次数按攻击者的最大生命值计算。",
               "适应按【生物种类 × 伤害类型】记录：同种生物共享（所有僵尸一起适应），不同种类分开，同一种生物的不同攻击方式也分开。",
               "适应度超过 reductionStart（默认 80%）后才开始减伤，按进度线性增加，100% 时完全免疫，法阵转动一圈。"
            }
         )
         .push("adaptation");
      BASE_HITS = b.comment("生命值等于 referenceHealth（默认 20，即普通僵尸）的生物，平均需要被攻击多少次才能完全适应").defineInRange("baseHits", 22.0, 1.0, 10000.0);
      REFERENCE_HEALTH = b.comment("参考生命值").defineInRange("referenceHealth", 20.0, 1.0, 100000.0);
      HEALTH_EXPONENT = b.comment(
            new String[]{
               "生命值对所需次数的影响指数：所需次数 = baseHits × (最大生命值 / referenceHealth) ^ 指数", "0.5（默认）：100 血约 49 次，200 血约 70 次，500 血约 110 次；1.0 = 与生命值成正比；0 = 与生命值无关"
            }
         )
         .defineInRange("healthExponent", 0.5, 0.0, 2.0);
      MIN_HITS = b.comment("所需次数下限（低血量生物）").defineInRange("minHits", 8.0, 1.0, 10000.0);
      MAX_HITS = b.comment("所需次数上限（超高血量 Boss）").defineInRange("maxHits", 400.0, 1.0, 100000.0);
      SLOW_SHARE_MAX = b.comment(new String[]{"高血量生物：前 80% 进度适应较快、后 20% 较慢（总次数不变）。", "这里是“后 20% 进度”最多占总次数的比例。普通生物为 0.2（匀速），血量越高越接近此值"})
         .defineInRange("slowShareMax", 0.5, 0.2, 0.95);
      SLOW_SHARE_HEALTH = b.comment("生命值达到多少时，“后 20% 进度”的占比达到 slowShareMax（之间按对数插值）").defineInRange("slowShareHealth", 500.0, 21.0, 100000.0);
      ENVIRONMENT_HITS = b.comment("原版环境伤害（摔落/火焰/岩浆/溺水/仙人掌等）平均需要多少次才能完全适应").defineInRange("environmentHits", 60.0, 1.0, 100000.0);
      GAIN_RANDOMNESS = b.comment("每次增长的随机浮动：0.5 = 在平均值的 50% ~ 150% 之间随机").defineInRange("gainRandomness", 0.5, 0.0, 0.95);
      REDUCTION_START = b.comment("适应度超过多少百分比后开始减伤（之前不减伤）").defineInRange("reductionStart", 80.0, 0.0, 99.0);
      MASTERY_PER_ADAPT = b.comment("法阵熟练度：每完成一次适应，之后所有适应的速度提高多少（0.05 = +5%）").defineInRange("masteryPerAdapt", 0.05, 0.0, 10.0);
      MASTERY_MAX = b.comment("熟练度带来的适应速度加成上限（1.0 = 最多 +100%，即速度翻倍）").defineInRange("masteryMax", 1.0, 0.0, 100.0);
      ENHANCED_UNCAPPED_MASTERY = b.comment("加强版法阵的熟练度是否无上限（true = 不再受 masteryMax 限制，用得越久适应越快；削弱版始终受上限限制）")
         .define("enhancedUncappedMastery", true);
      DAMAGE_REFERENCE = b.comment(new String[]{"受到的伤害也会影响适应速度：每次增长 × (本次伤害 / damageReference) ^ damageExponent。", "默认参考伤害 3（普通难度僵尸一击），伤害越高适应越快、所需次数越少"})
         .defineInRange("damageReference", 3.0, 0.01, 10000.0);
      DAMAGE_EXPONENT = b.comment(new String[]{"伤害对适应速度的影响指数（0 = 不受伤害影响）。默认 0.3（比较温和）：", "铁傀儡（约 15 伤害）快约 1.6 倍，监守者（30）快 2 倍；1 点的小伤害慢约 30%"})
         .defineInRange("damageExponent", 0.3, 0.0, 3.0);
      DAMAGE_FACTOR_MIN = b.comment("伤害系数下限（很低的伤害，如着火每次 1 点）：最多慢到 0.7 倍").defineInRange("damageFactorMin", 0.7, 0.01, 100.0);
      DAMAGE_FACTOR_MAX = b.comment("伤害系数上限（极高的伤害）：最多快到 2 倍，避免一两下就适应").defineInRange("damageFactorMax", 2.0, 0.01, 100.0);
      HEAL_ON_TURN = b.comment("完成适应（法阵转动）时是否治愈本次受到的伤害（原作中魔虚罗转轮后会再生）").define("healOnTurn", false);
      SHOW_MESSAGES = b.comment("是否向佩戴者发送适应提示（显示在屏幕左侧 HUD，位置在客户端配置中调整）").define("showMessages", true);
      MAX_ENTRIES = b.comment("法阵最多记忆多少条适应记录（超过时遗忘最久未触发的记录）").defineInRange("maxEntries", 128, 8, 4096);
      BLACKLIST = b.comment("不会被适应的伤害类型（另外，所有带 minecraft:bypasses_invulnerability 标签的伤害，如虚空和 /kill，也不可适应）")
         .defineListAllowEmpty("blacklist", List.of("minecraft:out_of_world", "minecraft:generic_kill", "minecraft:outside_border"), o -> o instanceof String);
      b.pop();
      b.comment(
            new String[]{
               "魔虚罗法阵 - 二度适应：存在本身", "对某种生物的任意一种攻击完全适应后，开始二度适应「该生物的存在本身」。", "此后每次被该种生物攻击（包括已被无效化的攻击）都会推进二度适应，速度比一度适应慢得多。", "佩戴者对该种生物的伤害随二度适应进度提高；完成时法阵转动一圈。"
            }
         )
         .push("existence");
      ENABLE_EXISTENCE = b.comment("是否启用二度适应").define("enableExistence", true);
      EXISTENCE_HITS_MULTIPLIER = b.comment("二度适应所需次数 = 一度适应所需次数 × 此倍率（僵尸约 55 次）").defineInRange("existenceHitsMultiplier", 2.5, 0.1, 100.0);
      EXISTENCE_DAMAGE_BONUS = b.comment("二度适应进度 100% 时，佩戴者对该种生物的额外伤害比例（0.5 = +50%）；按进度线性增加").defineInRange("existenceDamageBonus", 0.5, 0.0, 100.0);
      b.pop();
      b.comment(new String[]{"魔虚罗法阵 - 便利性适应（黑暗 / 负面效果 / 跑步）", "每项完成适应（跑步为每次升级）时法阵转动一圈并消耗耐久（durabilityPerConvenience）。", "所有已适应项目都可以在法阵界面（右键法阵 / 佩戴时按 G）中单独调整。"})
         .push("convenience");
      ENABLE_DARK = b.comment("适应黑暗：待在昏暗处逐渐看清（类似夜视，强度随适应度增加）").define("enableDarkVision", true);
      DARK_LIGHT_LEVEL = b.comment("眼睛位置的亮度不高于此值时视为“黑暗”（0~15）").defineInRange("darkLightLevel", 7, 0, 15);
      DARK_SECONDS = b.comment("在黑暗中平均累计多少秒完全适应（受熟练度加速）").defineInRange("darkSeconds", 480.0, 1.0, 1000000.0);
      DARK_MAX_STRENGTH = b.comment("完全适应时的夜视强度（1.0 = 与夜视药水相同）").defineInRange("darkMaxStrength", 1.0, 0.0, 1.0);
      ENABLE_DEBUFF = b.comment("适应负面效果（中毒、凋零、缓慢、虚弱、黑暗、失明……每种单独适应）").define("enableDebuff", true);
      DEBUFF_SECONDS = b.comment(new String[]{"身上带着某种负面效果时，平均累计多少秒完全适应（效果等级越高越快；受熟练度加速）。", "超过 reductionStart 后缩短该效果的持续时间，100% 时免疫"})
         .defineInRange("debuffSeconds", 40.0, 1.0, 1000000.0);
      DEBUFF_ONE_AT_A_TIME = b.comment("true = 一个一个适应：同时有多种负面效果时，只推进进度最高的那一种，完成后再适应下一种").define("debuffOneAtATime", true);
      DEBUFF_BLACKLIST = b.comment("不会被适应的效果 ID").defineListAllowEmpty("debuffBlacklist", List.of(), o -> o instanceof String);
      ENABLE_RUN = b.comment("适应跑步：疾跑时逐渐提高疾跑速度，没有上限，但升级极慢、每级只加一点").define("enableRunning", true);
      RUN_BLOCKS_BASE = b.comment("升到第 1 级需要疾跑的方块数").defineInRange("runBlocksBase", 250.0, 1.0, 1000000.0);
      RUN_BLOCKS_GROWTH = b.comment("之后每级所需距离的增长：第 n 级需要 runBlocksBase × (1 + runBlocksGrowth × (n-1)) 格").defineInRange("runBlocksGrowth", 0.1, 0.0, 100.0);
      RUN_SPEED_PER_LEVEL = b.comment("每级提高的疾跑速度（0.01 = +1%）").defineInRange("runSpeedPerLevel", 0.01, 0.0, 10.0);
      ENABLE_SWIM = b.comment("适应游泳（仅加强版）：在水中游动逐渐提高游泳速度，没有上限").define("enableSwim", true);
      SWIM_BLOCKS_BASE = b.comment("游泳升到第 1 级需要游的方块数").defineInRange("swimBlocksBase", 250.0, 1.0, 1000000.0);
      SWIM_BLOCKS_GROWTH = b.comment("之后每级所需距离的增长：第 n 级需要 swimBlocksBase × (1 + swimBlocksGrowth × (n-1)) 格")
         .defineInRange("swimBlocksGrowth", 0.1, 0.0, 100.0);
      SWIM_SPEED_PER_LEVEL = b.comment("每级提高的游泳速度（0.03 = +3%）").defineInRange("swimSpeedPerLevel", 0.03, 0.0, 10.0);
      ENABLE_LAVA = b.comment(
            new String[]{
               "适应岩浆（仅加强版）：在岩浆中活动逐渐提高岩浆中的移动速度，没有上限，并能在岩浆中上浮（按住潜行可下潜）。",
               "默认需要先完成岩浆伤害的一度适应才会开始积累（lavaRequiresAdapt）。"
            }
         ).define("enableLavaSwim", true);
      LAVA_BLOCKS_BASE = b.comment("岩浆畅游升到第 1 级需要在岩浆中移动的方块数").defineInRange("lavaBlocksBase", 150.0, 1.0, 1000000.0);
      LAVA_BLOCKS_GROWTH = b.comment("之后每级所需距离的增长：第 n 级需要 lavaBlocksBase × (1 + lavaBlocksGrowth × (n-1)) 格")
         .defineInRange("lavaBlocksGrowth", 0.1, 0.0, 100.0);
      LAVA_SPEED_PER_LEVEL = b.comment("每级提高的岩浆中移动速度（0.05 = +5%）").defineInRange("lavaSpeedPerLevel", 0.05, 0.0, 10.0);
      LAVA_REQUIRES_ADAPT = b.comment("岩浆畅游是否需要先完成岩浆伤害的一度适应（D|minecraft:lava 达到 100%）").define("lavaRequiresAdapt", true);
      ENABLE_GLIDE = b.comment(
            new String[]{
               "适应滑行（仅加强版）：完成摔落伤害的一度适应后，在空中滞留逐渐学会滑行（类似鞘翅，可俯仰控制）。",
               "滑行需要一定的适应度才有效果，100% 时为完整滑行手感。"
            }
         ).define("enableGlide", true);
      GLIDE_SECONDS = b.comment("累计滞空（下坠）多少秒学会完全滑行（受熟练度加速）").defineInRange("glideSeconds", 300.0, 1.0, 1000000.0);
      GLIDE_MAX_STRENGTH = b.comment("完全滑行时的滑行效率（1.0 = 完整鞘翅手感）").defineInRange("glideMaxStrength", 1.0, 0.0, 1.0);
      GLIDE_REQUIRE_SNEAK = b.comment("滑行是否需要按住潜行（true = 空中按住 Shift 才滑行；false = 下坠超过一段后自动滑行）").define("glideRequireSneak", true);
      ENABLE_FLY = b.comment("适应飞行（仅加强版）：滑行完全学会后，在滑行中继续适应，最终获得创造式飞行（佩戴加强版法阵时生效，取下即失效）")
         .define("enableFly", true);
      FLY_SECONDS = b.comment("累计滑行多少秒学会飞行（受熟练度加速；使用鞘翅滑行同样计入）").defineInRange("flySeconds", 600.0, 1.0, 1000000.0);
      ENABLE_JUMP = b.comment("适应跳跃（仅加强版）：跳跃累计上升高度逐渐提高跳跃高度，没有上限").define("enableJump", true);
      JUMP_BLOCKS_BASE = b.comment("跳跃升到第 1 级需要累计的跳跃上升高度（格）").defineInRange("jumpBlocksBase", 120.0, 1.0, 1000000.0);
      JUMP_BLOCKS_GROWTH = b.comment("之后每级所需高度的增长：第 n 级需要 jumpBlocksBase × (1 + jumpBlocksGrowth × (n-1)) 格")
         .defineInRange("jumpBlocksGrowth", 0.1, 0.0, 100.0);
      JUMP_HEIGHT_PER_LEVEL = b.comment("每级提高的跳跃初速度（0.08 = +8%，约 5 级相当于跳跃提升 I）").defineInRange("jumpHeightPerLevel", 0.08, 0.0, 10.0);
      ENABLE_LEAP = b.comment("适应跑跳（仅加强版）：疾跑跳跃中累计距离，逐渐提高疾跑跳跃时的空中速度，没有上限").define("enableLeap", true);
      LEAP_BLOCKS_BASE = b.comment("跑跳升到第 1 级需要的疾跑腾空距离（格）").defineInRange("leapBlocksBase", 250.0, 1.0, 1000000.0);
      LEAP_BLOCKS_GROWTH = b.comment("之后每级所需距离的增长：第 n 级需要 leapBlocksBase × (1 + leapBlocksGrowth × (n-1)) 格")
         .defineInRange("leapBlocksGrowth", 0.1, 0.0, 100.0);
      LEAP_SPEED_PER_LEVEL = b.comment("每级提高的跑跳空中速度（0.02 = +2%，与疾跑加速叠加）").defineInRange("leapSpeedPerLevel", 0.02, 0.0, 10.0);
      ENABLE_BREATH = b.comment("适应呼吸（仅加强版）：潜水时逐渐减缓氧气消耗，100% 时不再消耗氧气（等同水下呼吸）").define("enableBreath", true);
      BREATH_SECONDS = b.comment("累计潜水（氧气不满）多少秒完全适应（受熟练度加速）").defineInRange("breathSeconds", 300.0, 1.0, 1000000.0);
      ENABLE_MINE = b.comment("适应挖掘（仅加强版）：挖掘方块逐渐提高挖掘速度，没有上限").define("enableMine", true);
      MINE_BLOCKS_BASE = b.comment("挖掘升到第 1 级需要挖掘的方块数").defineInRange("mineBlocksBase", 200.0, 1.0, 1000000.0);
      MINE_BLOCKS_GROWTH = b.comment("之后每级所需方块数的增长：第 n 级需要 mineBlocksBase × (1 + mineBlocksGrowth × (n-1)) 格")
         .defineInRange("mineBlocksGrowth", 0.1, 0.0, 100.0);
      MINE_SPEED_PER_LEVEL = b.comment("每级提高的挖掘速度（0.05 = +5%）").defineInRange("mineSpeedPerLevel", 0.05, 0.0, 10.0);
      ENABLE_SATIETY = b.comment("适应饱食（仅加强版）：饥饿时逐渐减缓饥饿消耗，100% 时饱食度不再下降").define("enableSatiety", true);
      SATIETY_SECONDS = b.comment("累计饥饿（饱食度不满）多少秒完全适应（受熟练度加速）").defineInRange("satietySeconds", 600.0, 1.0, 1000000.0);
      ENABLE_KNOCKBACK = b.comment("适应击退（仅加强版）：被击退逐渐提高韧性，100% 时不再被击退（仅对走击退事件的攻击生效）").define("enableKnockback", true);
      KNOCKBACK_HITS = b.comment("平均被击退多少次完全适应（受熟练度加速）").defineInRange("knockbackHits", 30.0, 1.0, 100000.0);
      ENABLE_LAVASIGHT = b.comment(
            new String[]{
               "适应熔岩视野（仅加强版）：待在岩浆中逐渐看清岩浆内部（类似火焰抗性的熔岩视野），100% 解锁（滑杆可开关）。",
               "默认需要先解锁岩浆畅游（lavaRequiresAdapt 的同一条件）才会开始积累。"
            }
         ).define("enableLavasight", true);
      LAVASIGHT_SECONDS = b.comment("累计待在岩浆中多少秒完全适应（受熟练度加速）").defineInRange("lavasightSeconds", 300.0, 1.0, 1000000.0);
      ENABLE_CLIMB = b.comment("适应攀爬（仅加强版）：贴墙滞空逐渐学会攀爬；贴墙按住潜行上升，松开缓慢下滑").define("enableClimb", true);
      CLIMB_SECONDS = b.comment("累计贴墙滞空多少秒完全适应（受熟练度加速）").defineInRange("climbSeconds", 300.0, 1.0, 1000000.0);
      ENABLE_REGEN = b.comment("适应再生（仅加强版）：脱战且生命不满时逐渐学会再生，之后按适应度持续回血").define("enableRegen", true);
      REGEN_SECONDS = b.comment("累计脱战残血多少秒完全适应（受熟练度加速）").defineInRange("regenSeconds", 600.0, 1.0, 1000000.0);
      REGEN_RATE = b.comment("完全适应时每秒回复的生命值（1.0 = 每秒半颗心，按适应度线性增加）").defineInRange("regenRate", 1.0, 0.0, 100.0);
      REGEN_COMBAT_TICKS = b.comment("脱战判定：距离上次受伤超过多少 tick 视为脱战（20 tick = 1 秒）").defineInRange("regenCombatTicks", 200, 0, 72000);
      ENABLE_WISDOM = b.comment("适应感悟（仅加强版）：拾取经验逐渐提高获得的经验，没有上限").define("enableWisdom", true);
      WISDOM_XP_BASE = b.comment("感悟升到第 1 级需要拾取的经验点数").defineInRange("wisdomXpBase", 400.0, 1.0, 1000000.0);
      WISDOM_XP_GROWTH = b.comment("之后每级所需经验的增长：第 n 级需要 wisdomXpBase × (1 + wisdomXpGrowth × (n-1)) 点")
         .defineInRange("wisdomXpGrowth", 0.15, 0.0, 100.0);
      WISDOM_XP_PER_LEVEL = b.comment("每级提高的经验获取（0.05 = +5%）").defineInRange("wisdomXpPerLevel", 0.05, 0.0, 10.0);
      ENABLE_STEP = b.comment("自动台阶（仅加强版）：站在地面顶着墙时逐渐适应，之后可以自动走上 1 格高的台阶").define("enableStep", true);
      STEP_SECONDS = b.comment("自动台阶完全适应需要的秒数").defineInRange("stepSeconds", 120.0, 1.0, 100000.0);
      b.pop();
      b.comment(new String[]{"魔虚罗法阵 - 耐久设置", "除了下方名单中的玩家，其他任何佩戴者（其他玩家、生物）使用时法阵都有耐久，每次完成适应都会掉耐久，耐久耗尽法阵碎裂。", "可在铁砧中用金锭修复。"}).push("durability");
      MAX_DURABILITY = b.comment("法阵的最大耐久").defineInRange("maxDurability", 100, 1, 100000);
      DURABILITY_PER_ADAPT = b.comment("每完成一次适应（法阵转动）消耗的耐久。默认 10 → 满耐久可完成 10 次适应").defineInRange("durabilityPerAdapt", 10, 0, 100000);
      DURABILITY_PER_HIT = b.comment("每次受击解析（适应度增长）额外消耗的耐久，默认 0 = 不消耗").defineInRange("durabilityPerHit", 0, 0, 100000);
      DURABILITY_PER_CONVENIENCE = b.comment("便利性适应每次完成（黑暗 / 每种负面效果 / 跑步每次升级）消耗的耐久").defineInRange("durabilityPerConvenience", 10, 0, 100000);
      EXEMPT_PLAYERS = b.comment("佩戴时【不消耗耐久】的玩家名（不区分大小写）").defineListAllowEmpty("exemptPlayers", List.of("Ankrya"), o -> o instanceof String);
      b.pop();
      SPEC = b.build();
      Builder c = new Builder();
      c.comment("魔虚罗法阵 - 渲染设置").push("render");
      DEGREES_PER_TURN = c.comment("完成一次适应时法阵转动的角度。360 = 转一整圈；45 = 只转过一根辐条（动画原作的“咔”一下）").defineInRange("degreesPerTurn", 360.0, 1.0, 3600.0);
      TURN_SECONDS = c.comment("一次转动动画的时长（秒），默认与转动音效等长").defineInRange("turnSeconds", 1.05, 0.1, 10.0);
      c.pop();
      c.comment("适应提示 HUD（修改后保存即可实时生效）").push("hud");
      HUD_ENABLED = c.comment("是否显示适应提示").define("enabled", true);
      HUD_ANCHOR = c.comment("锚点位置：TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT, TOP_CENTER, BOTTOM_CENTER, TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT, CENTER")
         .defineEnum("anchor", WheelConfig.HudAnchor.MIDDLE_LEFT);
      HUD_OFFSET_X = c.comment("相对锚点的水平偏移（像素，GUI 坐标，向右为正）").defineInRange("offsetX", 4, -4000, 4000);
      HUD_OFFSET_Y = c.comment("相对锚点的垂直偏移（像素，GUI 坐标，向下为正）").defineInRange("offsetY", -30, -4000, 4000);
      HUD_SCALE = c.comment("文字缩放（1.0 = 原版大小）").defineInRange("scale", 0.6, 0.25, 3.0);
      HUD_SECONDS = c.comment("每条提示显示多少秒（最后 1 秒淡出）").defineInRange("seconds", 4.0, 0.5, 60.0);
      HUD_MAX_LINES = c.comment("最多同时显示几行").defineInRange("maxLines", 5, 1, 20);
      c.pop();
      CLIENT_SPEC = c.build();
   }

   public static enum HudAnchor {
      TOP_LEFT,
      MIDDLE_LEFT,
      BOTTOM_LEFT,
      TOP_CENTER,
      BOTTOM_CENTER,
      TOP_RIGHT,
      MIDDLE_RIGHT,
      BOTTOM_RIGHT,
      CENTER;
   }
}
