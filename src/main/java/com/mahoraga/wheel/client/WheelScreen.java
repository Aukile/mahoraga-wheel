/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.AdaptationData;
import com.mahoraga.wheel.AdaptationHandler;
import com.mahoraga.wheel.WheelConfig;
import com.mahoraga.wheel.WheelSource;
import com.mahoraga.wheel.network.ActionPacket;
import com.mahoraga.wheel.network.ModNetwork;
import com.mahoraga.wheel.network.SettingPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WheelScreen extends Screen {
   private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "textures/gui/wheel_screen.png");
   private static final ResourceLocation WG = ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "textures/gui/wheel_widgets.png");
   private static final int TW = 256;
   private static final int TH = 320;
   private static final int W = 256;
   private static final int H = 212;
   private static final int LX0 = 8;
   private static final int LY0 = 55;
   private static final int LX1 = 238;
   private static final int LY1 = 184;
   private static final int ROW_X = 10;
   private static final int ROW_Y = 56;
   private static final int ROW_W = 226;
   private static final int ROW_H = 30;
   private static final int ROW_STEP = 31;
   private static final int SCROLL_X = 240;
   private static final int SCROLL_W = 8;
   private static final int BTN_W = 64;
   private static final int BTN_H = 18;
   private static final int BTN_Y = 190;
   private static final int EQUIP_X = 54;
   private static final int RESET_X = 138;
   private static final int TAB_X = 10;
   private static final int TAB_Y = 40;
   private static final int TAB_W = 52;
   private static final int TAB_H = 15;
   private static final int TAB_STEP = 54;
   private static final String[] TABS = new String[]{
      "gui.mahoraga_wheel.tab.all", "gui.mahoraga_wheel.tab.attack", "gui.mahoraga_wheel.tab.existence", "gui.mahoraga_wheel.tab.convenience"
   };
   private WheelSource source;
   private int left;
   private int top;
   private int tab;
   private float scroll;
   private float scrollTarget;
   private int ticks;
   private float openTime = -1.0F;
   private final Map<String, Float> pendingValue = new HashMap<>();
   private final Map<String, Boolean> pendingImmune = new HashMap<>();
   private final Map<String, Integer> pendingExpire = new HashMap<>();
   private String dragKey;
   private boolean draggingScroll;
   private int resetConfirmUntil;
   private int lastTurns = -1;
   private float spinFrom;
   private float spinTo;
   private float spinStart = -100.0F;
   private List<AdaptationData.Entry> rows = new ArrayList<>();

   public WheelScreen(WheelSource source) {
      super(Component.translatable("gui.mahoraga_wheel.title"));
      this.source = source;
   }

   public static void open(WheelSource source) {
      Minecraft.getInstance().setScreen(new WheelScreen(source));
   }

   protected void init() {
      this.left = (this.width - 256) / 2;
      this.top = Math.max(2, (this.height - 212) / 2);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private ItemStack stack() {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return ItemStack.EMPTY;
      } else {
         ItemStack s = this.source.resolve(player);
         if (s.isEmpty() && this.source != WheelSource.WORN) {
            ItemStack worn = WheelSource.WORN.resolve(player);
            if (!worn.isEmpty()) {
               this.source = WheelSource.WORN;
               return worn;
            }
         }

         return s;
      }
   }

   private boolean inTab(AdaptationData.Entry e) {
      return switch (this.tab) {
         case 1 -> e.kind() == AdaptationData.Kind.ATTACK || e.kind() == AdaptationData.Kind.ENVIRONMENT;
         case 2 -> e.kind() == AdaptationData.Kind.EXISTENCE;
         case 3 -> e.kind().convenience();
         default -> true;
      };
   }

   private static float maxOf(AdaptationData.Entry e) {
      if (!Float.isNaN(e.max())) {
         return e.max();
      } else {
         return switch (e.kind()) {
            case ATTACK, ENVIRONMENT, DEBUFF -> AdaptationHandler.reductionMax(e.percent());
            case EXISTENCE -> AdaptationHandler.existenceBonus(e.percent());
            case DARK -> e.percent() / 100.0F;
            case RUN -> e.level() * 0.01F;
            case SWIM -> e.level() * 0.03F;
            case LAVA -> e.level() * 0.05F;
            case GLIDE -> e.percent() / 100.0F;
            case FLY -> e.percent() / 100.0F;
            case JUMP -> e.level() * 0.08F;
            case LEAP -> e.level() * 0.02F;
            case BREATH -> e.percent() / 100.0F;
            case MINE -> e.level() * 0.05F;
            case SATIETY -> e.percent() / 100.0F;
            case KNOCKBACK -> e.percent() / 100.0F;
            case LAVASIGHT -> e.percent() / 100.0F;
            case CLIMB -> e.percent() / 100.0F;
            case REGEN -> e.percent() / 100.0F;
            case WISDOM -> e.level() * 0.05F;
            case STEP -> e.percent() / 100.0F;
         };
      }
   }

   private static float scaleOf(AdaptationData.Entry e) {
      if (!Float.isNaN(e.max())) {
         return Math.max(e.scale(), 1.0E-4F);
      } else {
         return switch (e.kind()) {
            case EXISTENCE -> WheelConfig.existenceBonus();
            case RUN, SWIM, LAVA, JUMP, LEAP, MINE, WISDOM -> Math.max(maxOf(e), 1.0E-4F);
            default -> 1.0F;
         };
      }
   }

   private float valueOf(AdaptationData.Entry e) {
      float max = maxOf(e);
      Float p = this.pendingValue.get(e.key());
      float user = p != null ? p : e.user();
      return Float.isNaN(user) ? max : Math.min(user, max);
   }

   private boolean isAuto(AdaptationData.Entry e) {
      Float p = this.pendingValue.get(e.key());
      return Float.isNaN(p != null ? p : e.user());
   }

   private boolean immuneOf(AdaptationData.Entry e) {
      Boolean p = this.pendingImmune.get(e.key());
      return p != null ? p : e.immune();
   }

   private boolean sliderLockedByImmune(AdaptationData.Entry e) {
      return e.kind().reduction() && e.complete() && this.immuneOf(e);
   }

   public void tick() {
      this.ticks++;
      this.pendingExpire.entrySet().removeIf(en -> {
         if (en.getValue() <= this.ticks && !en.getKey().equals(this.dragKey)) {
            this.pendingValue.remove(en.getKey());
            this.pendingImmune.remove(en.getKey());
            return true;
         } else {
            return false;
         }
      });
      if (this.stack().isEmpty()) {
         this.onClose();
      }
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(g, mouseX, mouseY, partialTick);
      ItemStack stack = this.stack();
      if (!stack.isEmpty()) {
         float time = this.ticks + partialTick;
         if (this.openTime < 0.0F) {
            this.openTime = time;
         }

         float open = easeOutCubic(Mth.clamp((time - this.openTime) / 7.0F, 0.0F, 1.0F));
         this.rows = new ArrayList<>();

         for (AdaptationData.Entry e : AdaptationData.entries(stack)) {
            if (this.inTab(e)) {
               this.rows.add(e);
            }
         }

         float maxScroll = Math.max(0, this.rows.size() * 31 - 1 - 127);
         this.scrollTarget = Mth.clamp(this.scrollTarget, 0.0F, maxScroll);
         this.scroll = Mth.lerp(0.35F, this.scroll, this.scrollTarget);
         if (Math.abs(this.scroll - this.scrollTarget) < 0.2F) {
            this.scroll = this.scrollTarget;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, open);
         g.blit(BG, this.left, this.top, 0.0F, 0.0F, 256, 212, 256, 256);
         this.renderHeader(g, stack, time, open);
         this.renderTabs(g, mouseX, mouseY);
         this.renderList(g, mouseX, mouseY, time, maxScroll);
         this.renderButtons(g, mouseX, mouseY, time);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.renderTooltips(g, mouseX, mouseY);
      }
   }

   private void renderHeader(GuiGraphics g, ItemStack stack, float time, float open) {
      int cx = this.left + 24;
      int cy = this.top + 20;
      float pulse = 0.35F + 0.2F * Mth.sin(time * 0.12F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, pulse * open);
      wb(g, cx - 22, cy - 22, 44, 44, 0.0F, 224.0F, 64, 64, 256, 320);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, open);
      int turns = AdaptationData.getTurns(stack);
      float spin = this.currentSpin(time);
      if (this.lastTurns < 0) {
         this.lastTurns = turns;
      } else if (turns != this.lastTurns) {
         this.spinFrom = spin;
         this.spinTo = spin + 45.0F * Math.min(8, Math.abs(turns - this.lastTurns));
         this.spinStart = time;
         this.lastTurns = turns;
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F));
      }

      float intro = (1.0F - easeOutCubic(Mth.clamp((time - this.openTime) / 30.0F, 0.0F, 1.0F))) * 220.0F;
      float angle = time * 0.6F + this.currentSpin(time) - intro;
      PoseStack pose = g.pose();
      pose.pushPose();
      pose.translate(cx, cy, 0.0F);
      pose.mulPose(Axis.ZP.rotationDegrees(angle));
      wb(g, -16, -16, 32, 32, 64.0F, 224.0F, 64, 64, 256, 320);
      pose.popPose();
      int alpha = alphaBits(open);
      g.drawString(this.font, this.title, this.left + 46, this.top + 8, 15255146 | alpha, true);
      int mastery = AdaptationData.getMastery(stack);

      int speed;
      try {
         speed = Math.round((AdaptationHandler.masteryMultiplier(stack) - 1.0F) * 100.0F);
      } catch (Exception var21) {
         speed = 0;
      }

      Component info = Component.translatable("gui.mahoraga_wheel.info", new Object[]{mastery, speed});
      g.drawString(this.font, info, this.left + 46, this.top + 21, 12102089 | alpha, false);
      Component turnsText = Component.translatable("gui.mahoraga_wheel.turns", new Object[]{turns});
      g.drawString(this.font, turnsText, this.left + 256 - 10 - this.font.width(turnsText), this.top + 21, 9207712 | alpha, false);
      Player player = Minecraft.getInstance().player;
      boolean exempt = player != null && WheelConfig.exemptPlayers().stream().anyMatch(n -> n.equalsIgnoreCase(player.getGameProfile().getName()));
      Component dura;
      if (!exempt && stack.isDamageableItem()) {
         dura = Component.translatable("gui.mahoraga_wheel.durability", new Object[]{stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()});
      } else {
         dura = Component.translatable("gui.mahoraga_wheel.durability_inf");
      }

      g.drawString(this.font, dura, this.left + 256 - 10 - this.font.width(dura), this.top + 8, 13214282 | alpha, false);
   }

   private float currentSpin(float time) {
      float t = Mth.clamp((time - this.spinStart) / 22.0F, 0.0F, 1.0F);
      return this.spinFrom + (this.spinTo - this.spinFrom) * easeOutBack(t);
   }

   private void renderTabs(GuiGraphics g, int mx, int my) {
      for (int i = 0; i < TABS.length; i++) {
         int x = this.left + 10 + i * 54;
         int y = this.top + 40;
         boolean hover = in(mx, my, x, y, 52, 15);
         int v = this.tab == i ? 30 : (hover ? 15 : 0);
         wb(g, x, y, 64, v, 52, 15, 256, 320);
         Component label = Component.translatable(TABS[i]);
         int color = this.tab == i ? -928390 : (hover ? -1647118 : -6648658);
         g.drawString(this.font, label, x + (52 - this.font.width(label)) / 2, y + 4, color, this.tab == i);
      }
   }

   private void renderList(GuiGraphics g, int mx, int my, float time, float maxScroll) {
      int x0 = this.left + 8;
      int y0 = this.top + 55;
      int x1 = this.left + 238;
      int y1 = this.top + 184;
      g.enableScissor(x0, y0, x1, y1);
      PoseStack pose = g.pose();
      pose.pushPose();
      pose.translate((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, 0.0F);
      pose.mulPose(Axis.ZP.rotationDegrees(-time * 0.25F));
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.07F);
      wb(g, -60, -60, 120, 120, 64.0F, 224.0F, 64, 64, 256, 320);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.popPose();
      if (this.rows.isEmpty()) {
         Component none = Component.translatable("gui.mahoraga_wheel.empty");
         g.drawString(this.font, none, (x0 + x1 - this.font.width(none)) / 2, (y0 + y1) / 2 - 4, -8556654, false);
      }

      boolean mouseInList = in(mx, my, x0, y0, x1 - x0, y1 - y0);

      for (int i = 0; i < this.rows.size(); i++) {
         int ry = this.rowY(i);
         if (ry + 30 >= y0 && ry <= y1) {
            float slide = 1.0F - easeOutCubic(Mth.clamp((time - this.openTime - i * 1.5F) / 8.0F, 0.0F, 1.0F));
            int rx = this.left + 10 + Math.round(slide * 30.0F);
            this.renderRow(g, this.rows.get(i), i, rx, ry, mx, my, mouseInList, time);
         }
      }

      g.disableScissor();
      if (maxScroll > 0.0F) {
         int trackH = 128;
         int thumbH = 16;
         int ty = this.top + 55 + Math.round((trackH - thumbH) * (this.scroll / maxScroll));
         int tx = this.left + 240 + 1;
         boolean hover = this.draggingScroll || in(mx, my, tx, ty, 6, thumbH);
         wb(g, tx, ty, hover ? 196 : 190, 0, 6, 16, 256, 320);
      }
   }

   private int rowY(int i) {
      return this.top + 56 + i * 31 - Math.round(this.scroll);
   }

   private void renderRow(GuiGraphics g, AdaptationData.Entry e, int index, int rx, int ry, int mx, int my, boolean mouseInList, float time) {
      boolean hover = mouseInList && in(mx, my, rx, ry, 226, 30);
      boolean done = e.complete();
      int v = hover ? 158 : (done ? 188 : 128);
      wb(g, rx, ry, 0, v, 226, 30, 256, 320);
      String name = AdaptationHandler.describe(e).getString();
      if (e.kind().levelBased()) {
         name = name + "  Lv." + e.level();
      }

      String shown = this.font.plainSubstrByWidth(name, 112);
      if (!shown.equals(name)) {
         shown = this.font.plainSubstrByWidth(name, 106) + "…";
      }

      g.drawString(this.font, shown, rx + 6, ry + 4, nameColor(e), true);
      int bx = rx + 124;
      int by = ry + 6;
      wb(g, bx, by, 120, 0, 50, 5, 256, 320);
      int fill = Math.round(Mth.clamp(e.percent() / 100.0F, 0.0F, 1.0F) * 50.0F);
      byte var10000;
      if (done) {
         var10000 = 10;
      } else {
         switch (e.kind()) {
            case DEBUFF:
               var10000 = 25;
               break;
            case EXISTENCE:
               var10000 = 15;
               break;
            case DARK:
               var10000 = 20;
               break;
            case SWIM:
               var10000 = 20;
               break;
            case LAVA:
               var10000 = 25;
               break;
            case GLIDE:
               var10000 = 15;
               break;
            case FLY:
               var10000 = 15;
               break;
            case JUMP:
               var10000 = 25;
               break;
            case BREATH:
               var10000 = 20;
               break;
            case MINE:
               var10000 = 5;
               break;
            case SATIETY:
               var10000 = 25;
               break;
            case KNOCKBACK:
               var10000 = 5;
               break;
            case LAVASIGHT:
               var10000 = 20;
               break;
            case CLIMB:
               var10000 = 25;
               break;
            case REGEN:
               var10000 = 15;
               break;
            case WISDOM:
               var10000 = 15;
               break;
            case STEP:
               var10000 = 15;
               break;
            default:
               var10000 = 5;
         }
      }

      int barV = var10000;
      if (fill > 0) {
         if (done) {
            wb(g, bx, by, 120, barV, fill, 5, 256, 320);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.25F + 0.25F * Mth.sin(time * 0.15F + index));
            wb(g, bx, by, 176, 0, 10, 5, 256, 320);
            wb(g, bx + 40, by, 176, 0, 10, 5, 256, 320);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            wb(g, bx, by, 120, barV, fill, 5, 256, 320);
            g.enableScissor(bx, by, bx + fill, by + 5);
            float sweep = (time * 1.4F + index * 17.0F) % (fill + 30.0F) - 10.0F;
            wb(g, bx + Math.round(sweep), by, 176, 0, 10, 5, 256, 320);
            g.disableScissor();
         }
      }

      String pct = e.kind() == AdaptationData.Kind.RUN ? String.format("%.1f%%", e.percent()) : (done ? "100%" : String.format("%.1f%%", e.percent()));
      g.drawString(this.font, pct, rx + 220 - this.font.width(pct), ry + 4, done ? -33941 : -4675127, false);
      g.drawString(this.font, Component.translatable(labelKey(e)), rx + 6, ry + 18, -6648658, false);
      int sx = rx + 34;
      int sy = ry + 19;
      float scale = scaleOf(e);
      float max = maxOf(e);
      float maxFrac = Mth.clamp(max / scale, 0.0F, 1.0F);
      boolean lockedImmune = this.sliderLockedByImmune(e);
      boolean enabled = max > 0.0F && !lockedImmune;
      float value = lockedImmune ? scale : this.valueOf(e);
      float frac = Mth.clamp(value / scale, 0.0F, 1.0F);
      wb(g, sx, sy, 0, 80, 100, 7, 256, 320);
      int lockFrom = Math.round(maxFrac * 100.0F);
      if (!lockedImmune && lockFrom < 100) {
         wb(g, sx + lockFrom, sy, lockFrom, 115, 100 - lockFrom, 7, 256, 320);
      }

      int fw = Math.round(frac * 100.0F);
      if (fw > 0) {
         int fv = switch (e.kind()) {
            case DEBUFF -> 108;
            case EXISTENCE -> 94;
            case DARK -> 101;
            case SWIM -> 101;
            case LAVA -> 94;
            case GLIDE -> 94;
            case FLY -> 108;
            case JUMP -> 108;
            case BREATH -> 101;
            case MINE -> 87;
            case SATIETY -> 108;
            case KNOCKBACK -> 87;
            case LAVASIGHT -> 101;
            case CLIMB -> 108;
            case REGEN -> 94;
            case WISDOM -> 94;
            case STEP -> 94;
            default -> 87;
         };
         if (lockedImmune) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
         }

         wb(g, sx, sy, 0, fv, fw, 7, 256, 320);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      boolean knobHover = enabled && (e.key().equals(this.dragKey) || mouseInList && in(mx, my, sx - 3, sy - 3, 106, 13));
      int knobU = !enabled ? 118 : (knobHover ? 111 : 104);
      wb(g, sx + fw - 3, sy - 2, knobU, 80, 7, 11, 256, 320);
      if (knobHover) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F + 0.5F * Mth.sin(time * 0.4F));
         wb(g, sx + fw - 5, sy + 1, 176, 0, 10, 5, 256, 320);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      Component valueText;
      int valueColor;
      if (lockedImmune) {
         valueText = Component.translatable("gui.mahoraga_wheel.immune");
         valueColor = -33941;
      } else if (max <= 0.0F) {
         valueText = Component.translatable(e.kind().reduction() ? "gui.mahoraga_wheel.locked_80" : "gui.mahoraga_wheel.locked");
         valueColor = -10595216;
      } else {
         String fmt = e.kind() != AdaptationData.Kind.EXISTENCE && !e.kind().levelBased() ? "" : "+";
         int cur = Math.round(value * 100.0F);
         int top = Math.round(max * 100.0F);
         valueText = Component.literal(!this.isAuto(e) && cur < top ? fmt + cur + "/" + top + "%" : fmt + cur + "%");
         valueColor = this.isAuto(e) ? -1647118 : -10138;
      }

      g.drawString(this.font, valueText, rx + 139, ry + 18, valueColor, false);
      if (e.kind().reduction()) {
         int tx = rx + 200;
         int ty = ry + 17;
         int tv = !done ? 102 : (this.immuneOf(e) ? 91 : 80);
         wb(g, tx, ty, 130, tv, 22, 11, 256, 320);
         if (done && this.immuneOf(e)) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.3F + 0.3F * Mth.sin(time * 0.2F + index));
            wb(g, tx + 3, ty + 3, 176, 0, 10, 5, 256, 320);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private static int nameColor(AdaptationData.Entry e) {
      return switch (e.kind()) {
         case DEBUFF -> -8462198;
         case EXISTENCE -> -3695617;
         case DARK -> -9447184;
         case RUN -> -10138;
         case SWIM -> -16744193;
         case LAVA -> -1647616;
         case GLIDE -> -1;
         case FLY -> -7667451;
         case JUMP -> -10318735;
         case LEAP -> -10138;
         case BREATH -> -16744193;
         case MINE -> -10551295;
         case SATIETY -> -39322;
         case KNOCKBACK -> -1186059;
         case LAVASIGHT -> -1647616;
         case CLIMB -> -10318735;
         case REGEN -> -3780833;
         case WISDOM -> -1175168;
         case STEP -> -1;
         default -> e.complete() ? -33941 : -1186059;
      };
   }

   private static String labelKey(AdaptationData.Entry e) {
      return switch (e.kind()) {
         case DEBUFF -> "gui.mahoraga_wheel.label.shorten";
         case EXISTENCE -> "gui.mahoraga_wheel.label.bonus";
         case DARK -> "gui.mahoraga_wheel.label.vision";
         case RUN -> "gui.mahoraga_wheel.label.speed";
         case SWIM -> "gui.mahoraga_wheel.label.swim";
         case LAVA -> "gui.mahoraga_wheel.label.lava";
         case GLIDE -> "gui.mahoraga_wheel.label.glide";
         case FLY -> "gui.mahoraga_wheel.label.fly";
         case JUMP -> "gui.mahoraga_wheel.label.jump";
         case LEAP -> "gui.mahoraga_wheel.label.leap";
         case BREATH -> "gui.mahoraga_wheel.label.breath";
         case MINE -> "gui.mahoraga_wheel.label.mine";
         case SATIETY -> "gui.mahoraga_wheel.label.satiety";
         case KNOCKBACK -> "gui.mahoraga_wheel.label.knockback";
         case LAVASIGHT -> "gui.mahoraga_wheel.label.lavasight";
         case CLIMB -> "gui.mahoraga_wheel.label.climb";
         case REGEN -> "gui.mahoraga_wheel.label.regen";
         case WISDOM -> "gui.mahoraga_wheel.label.wisdom";
         case STEP -> "gui.mahoraga_wheel.label.step";
         default -> "gui.mahoraga_wheel.label.reduction";
      };
   }

   private void renderButtons(GuiGraphics g, int mx, int my, float time) {
      boolean canEquip = this.source != WheelSource.WORN;
      this.button(
         g,
         this.left + 54,
         this.top + 190,
         Component.translatable(canEquip ? "gui.mahoraga_wheel.equip" : "gui.mahoraga_wheel.equipped"),
         canEquip,
         false,
         mx,
         my,
         time
      );
      boolean confirm = this.ticks < this.resetConfirmUntil;
      this.button(
         g,
         this.left + 138,
         this.top + 190,
         Component.translatable(confirm ? "gui.mahoraga_wheel.reset_confirm" : "gui.mahoraga_wheel.reset"),
         true,
         confirm,
         mx,
         my,
         time
      );
   }

   private void button(GuiGraphics g, int x, int y, Component label, boolean enabled, boolean danger, int mx, int my, float time) {
      boolean hover = enabled && in(mx, my, x, y, 64, 18);
      int v = !enabled ? 36 : (danger ? 54 : 0);
      wb(g, x, y, 0, v, 64, 18, 256, 320);
      if (hover && !danger) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.55F + 0.45F * Mth.sin(time * 0.3F));
         wb(g, x, y, 0, 18, 64, 18, 256, 320);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      if (danger) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.3F + 0.3F * Mth.sin(time * 0.6F));
         wb(g, x + 27, y + 6, 176, 0, 10, 5, 256, 320);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      int color = !enabled ? -9804174 : (danger ? -7976 : (hover ? -3648 : -1522070));
      g.drawString(this.font, label, x + (64 - this.font.width(label)) / 2, y + 5, color, enabled);
   }

   private void renderTooltips(GuiGraphics g, int mx, int my) {
      if (this.dragKey == null) {
         if (in(mx, my, this.left + 138, this.top + 190, 64, 18)) {
            g.renderComponentTooltip(this.font, List.of(Component.translatable("gui.mahoraga_wheel.reset_tip").withStyle(ChatFormatting.GRAY)), mx, my);
         } else if (in(mx, my, this.left + 54, this.top + 190, 64, 18) && this.source != WheelSource.WORN) {
            g.renderComponentTooltip(this.font, List.of(Component.translatable("gui.mahoraga_wheel.equip_tip").withStyle(ChatFormatting.GRAY)), mx, my);
         } else if (in(mx, my, this.left + 8, this.top + 55, 230, 129)) {
            for (int i = 0; i < this.rows.size(); i++) {
               AdaptationData.Entry e = this.rows.get(i);
               int rx = this.left + 10;
               int ry = this.rowY(i);
               if (in(mx, my, rx, ry, 226, 30)) {
                  List<Component> lines = new ArrayList<>();
                  if (e.kind().reduction() && in(mx, my, rx + 200, ry + 17, 22, 11)) {
                     lines.add(Component.translatable("gui.mahoraga_wheel.immune_tip").withStyle(ChatFormatting.GOLD));
                     lines.add(
                        Component.translatable(
                              e.complete()
                                 ? (this.immuneOf(e) ? "gui.mahoraga_wheel.immune_on" : "gui.mahoraga_wheel.immune_off")
                                 : "gui.mahoraga_wheel.immune_need"
                           )
                           .withStyle(ChatFormatting.GRAY)
                     );
                  } else if (in(mx, my, rx + 30, ry + 15, 108, 14)) {
                     lines.add(Component.translatable(labelKey(e) + ".tip").withStyle(ChatFormatting.GOLD));
                     lines.add(Component.translatable("gui.mahoraga_wheel.slider_tip").withStyle(ChatFormatting.GRAY));
                  } else if (in(mx, my, rx, ry, 120, 14)) {
                     lines.add(AdaptationHandler.describe(e).copy().withStyle(ChatFormatting.WHITE));
                     lines.add(Component.translatable("gui.mahoraga_wheel.kind." + e.kind().name().toLowerCase()).withStyle(ChatFormatting.DARK_GRAY));
                  }

                  if (!lines.isEmpty()) {
                     g.renderComponentTooltip(this.font, lines, mx, my);
                  }

                  return;
               }
            }
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         int mx = (int)mouseX;
         int my = (int)mouseY;

         for (int i = 0; i < TABS.length; i++) {
            if (in(mx, my, this.left + 10 + i * 54, this.top + 40, 52, 15)) {
               if (this.tab != i) {
                  this.tab = i;
                  this.scroll = this.scrollTarget = 0.0F;
                  this.openTime = this.ticks - 4;
                  click(1.2F);
               }

               return true;
            }
         }

         if (in(mx, my, this.left + 54, this.top + 190, 64, 18) && this.source != WheelSource.WORN) {
            ModNetwork.sendToServer(new ActionPacket(this.source, (byte)0));
            click(1.0F);
            return true;
         } else if (in(mx, my, this.left + 138, this.top + 190, 64, 18)) {
            if (this.ticks < this.resetConfirmUntil) {
               ModNetwork.sendToServer(new ActionPacket(this.source, (byte)1));
               this.resetConfirmUntil = 0;
               this.pendingValue.clear();
               this.pendingImmune.clear();
            } else {
               this.resetConfirmUntil = this.ticks + 60;
            }

            click(0.8F);
            return true;
         } else if (in(mx, my, this.left + 240, this.top + 55, 8, 129)) {
            this.draggingScroll = true;
            this.dragScrollTo(my);
            return true;
         } else {
            if (in(mx, my, this.left + 8, this.top + 55, 230, 129)) {
               for (int ix = 0; ix < this.rows.size(); ix++) {
                  AdaptationData.Entry e = this.rows.get(ix);
                  int rx = this.left + 10;
                  int ry = this.rowY(ix);
                  if (in(mx, my, rx, ry, 226, 30)) {
                     if (e.kind().reduction() && in(mx, my, rx + 200, ry + 17, 22, 11)) {
                        if (e.complete()) {
                           boolean next = !this.immuneOf(e);
                           this.pendingImmune.put(e.key(), next);
                           this.pendingExpire.put(e.key(), this.ticks + 40);
                           ModNetwork.sendToServer(new SettingPacket(this.source, e.key(), (byte)0, 0.0F, (byte)(next ? 1 : 0)));
                           click(next ? 1.3F : 0.9F);
                        }

                        return true;
                     }

                     int sx = rx + 34;
                     int sy = ry + 19;
                     if (in(mx, my, sx - 3, sy - 3, 106, 13) && maxOf(e) > 0.0F && !this.sliderLockedByImmune(e)) {
                        this.dragKey = e.key();
                        this.dragTo(e, mx, sx);
                        return true;
                     }

                     return true;
                  }
               }
            }

            return super.mouseClicked(mouseX, mouseY, button);
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
      if (this.draggingScroll) {
         this.dragScrollTo((int)mouseY);
         return true;
      } else {
         if (this.dragKey != null) {
            for (int i = 0; i < this.rows.size(); i++) {
               AdaptationData.Entry e = this.rows.get(i);
               if (e.key().equals(this.dragKey)) {
                  this.dragTo(e, (int)mouseX, this.left + 10 + 34);
                  return true;
               }
            }
         }

         return super.mouseDragged(mouseX, mouseY, button, dx, dy);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingScroll = false;
      if (this.dragKey != null) {
         String key = this.dragKey;
         this.dragKey = null;
         Float v = this.pendingValue.get(key);
         if (v != null) {
            boolean auto = Float.isNaN(v);
            ModNetwork.sendToServer(new SettingPacket(this.source, key, (byte)(auto ? 2 : 1), auto ? 0.0F : v, (byte)-1));
            this.pendingExpire.put(key, this.ticks + 40);
            click(1.5F);
         }

         return true;
      } else {
         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      this.scrollTarget -= (float)delta * 24.0F;
      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (WheelKeys.OPEN.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void dragTo(AdaptationData.Entry e, int mx, int sx) {
      float scale = scaleOf(e);
      float max = maxOf(e);
      float maxFrac = Mth.clamp(max / scale, 0.0F, 1.0F);
      float frac = Mth.clamp((mx - sx) / 100.0F, 0.0F, maxFrac);
      float value = frac >= maxFrac - 0.005F ? Float.NaN : frac * scale;
      this.pendingValue.put(e.key(), value);
      this.pendingExpire.put(e.key(), this.ticks + 40);
   }

   private void dragScrollTo(int my) {
      float maxScroll = Math.max(0, this.rows.size() * 31 - 1 - 127);
      float t = Mth.clamp((my - (this.top + 55) - 8) / 112.0F, 0.0F, 1.0F);
      this.scrollTarget = this.scroll = t * maxScroll;
   }

   private static void click(float pitch) {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
   }

   private static void wb(GuiGraphics g, int x, int y, int u, int v, int w, int h, int tw, int th) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      g.blit(WG, x, y, u, v, w, h, tw, th);
   }

   private static void wb(GuiGraphics g, int x, int y, int w, int h, float u, float v, int uw, int vh, int tw, int th) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      g.blit(WG, x, y, w, h, u, v, uw, vh, tw, th);
   }

   private static boolean in(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private static int alphaBits(float a) {
      return Math.max(5, Math.round(a * 255.0F)) << 24;
   }

   private static float easeOutCubic(float t) {
      float x = 1.0F - t;
      return 1.0F - x * x * x;
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      float x = t - 1.0F;
      return 1.0F + c3 * x * x * x + c1 * x * x;
   }
}
