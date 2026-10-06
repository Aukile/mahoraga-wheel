/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

public class CurioSlotCondition implements ICondition {
   public static final ResourceLocation ID = new ResourceLocation("mahoraga_wheel", "curio_slot_enabled");

   public ResourceLocation getID() {
      return ID;
   }

   public boolean test(IContext context) {
      return WheelConfig.curioSlotEnabled();
   }

   public static final class Serializer implements IConditionSerializer<CurioSlotCondition> {
      public static final CurioSlotCondition.Serializer INSTANCE = new CurioSlotCondition.Serializer();

      public void write(JsonObject json, CurioSlotCondition value) {
      }

      public CurioSlotCondition read(JsonObject json) {
         return new CurioSlotCondition();
      }

      public ResourceLocation getID() {
         return CurioSlotCondition.ID;
      }
   }
}
