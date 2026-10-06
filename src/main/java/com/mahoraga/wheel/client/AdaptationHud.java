/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.WheelConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;

public final class AdaptationHud implements LayeredDraw.Layer {
   public static final AdaptationHud INSTANCE = new AdaptationHud();
   private static final List<AdaptationHud.Line> LINES = new ArrayList<>();

   private AdaptationHud() {
   }

   public static void push(String key, Component text) {
      long now = Util.getMillis();
      LINES.removeIf(l -> l.key.equals(key));
      LINES.add(0, new AdaptationHud.Line(key, text, now));
      int max = (Integer)WheelConfig.HUD_MAX_LINES.get();

      while (LINES.size() > max) {
         LINES.remove(LINES.size() - 1);
      }
   }

   @Override
   public void render(GuiGraphics g, DeltaTracker deltaTracker) {
      Minecraft mc = Minecraft.getInstance();
      int screenWidth = g.guiWidth();
      int screenHeight = g.guiHeight();
      if (!LINES.isEmpty() && !mc.options.hideGui && mc.player != null) {
         if ((Boolean)WheelConfig.HUD_ENABLED.get()) {
            long now = Util.getMillis();
            long life = (long)((Double)WheelConfig.HUD_SECONDS.get() * 1000.0);
            long fade = Math.min(1000L, life / 2L);
            Iterator<AdaptationHud.Line> it = LINES.iterator();

            while (it.hasNext()) {
               if (now - it.next().time > life) {
                  it.remove();
               }
            }

            int max = (Integer)WheelConfig.HUD_MAX_LINES.get();

            while (LINES.size() > max) {
               LINES.remove(LINES.size() - 1);
            }

            if (!LINES.isEmpty()) {
               Font font = mc.font;
               float scale = ((Double)WheelConfig.HUD_SCALE.get()).floatValue();
               int lineH = 9 + 1;
               int maxW = 0;

               for (AdaptationHud.Line l : LINES) {
                  maxW = Math.max(maxW, font.width(l.text));
               }

               float blockW = maxW * scale;
               float blockH = LINES.size() * lineH * scale;
               WheelConfig.HudAnchor anchor = (WheelConfig.HudAnchor)WheelConfig.HUD_ANCHOR.get();
               boolean alignRight = false;
               float x;
               float y;
               switch (anchor) {
                  case TOP_LEFT:
                     x = 0.0F;
                     y = 0.0F;
                     break;
                  case MIDDLE_LEFT:
                     x = 0.0F;
                     y = (screenHeight - blockH) / 2.0F;
                     break;
                  case BOTTOM_LEFT:
                     x = 0.0F;
                     y = screenHeight - blockH;
                     break;
                  case TOP_CENTER:
                     x = (screenWidth - blockW) / 2.0F;
                     y = 0.0F;
                     break;
                  case BOTTOM_CENTER:
                     x = (screenWidth - blockW) / 2.0F;
                     y = screenHeight - blockH;
                     break;
                  case TOP_RIGHT:
                     x = screenWidth - blockW;
                     y = 0.0F;
                     alignRight = true;
                     break;
                  case MIDDLE_RIGHT:
                     x = screenWidth - blockW;
                     y = (screenHeight - blockH) / 2.0F;
                     alignRight = true;
                     break;
                  case BOTTOM_RIGHT:
                     x = screenWidth - blockW;
                     y = screenHeight - blockH;
                     alignRight = true;
                     break;
                  default:
                     x = (screenWidth - blockW) / 2.0F;
                     y = (screenHeight - blockH) / 2.0F;
               }

               x += ((Integer)WheelConfig.HUD_OFFSET_X.get()).intValue();
               y += ((Integer)WheelConfig.HUD_OFFSET_Y.get()).intValue();
               RenderSystem.enableBlend();
               g.pose().pushPose();
               g.pose().translate(x, y, 0.0F);
               g.pose().scale(scale, scale, 1.0F);
               int row = 0;

               for (AdaptationHud.Line l : LINES) {
                  long age = now - l.time;
                  float alpha = age > life - fade ? (float)(life - age) / (float)fade : 1.0F;
                  int a = (int)(alpha * 255.0F);
                  if (a <= 8) {
                     row++;
                  } else {
                     int lx = alignRight ? maxW - font.width(l.text) : 0;
                     g.drawString(font, l.text, lx, row * lineH, a << 24 | 16777215, true);
                     row++;
                  }
               }

               g.pose().popPose();
               RenderSystem.disableBlend();
            }
         }
      }
   }

   private static final class Line {
      final String key;
      Component text;
      long time;

      Line(String key, Component text, long time) {
         this.key = key;
         this.text = text;
         this.time = time;
      }
   }
}