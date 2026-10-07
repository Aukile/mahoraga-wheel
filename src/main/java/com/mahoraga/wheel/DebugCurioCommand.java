package com.mahoraga.wheel;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;

// TEMPORARY placement-debug command. Delete file + unregister after diagnosis.
// Usage: hold the wheel in main hand, run /mahoraga debugcurio
public final class DebugCurioCommand {
   private DebugCurioCommand() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      dispatcher.register(Commands.literal("mahoraga").then(Commands.literal("debugcurio").executes(context -> run(context.getSource()))));
   }

   private static int run(CommandSourceStack src) {
      try {
         Player player = src.getPlayerOrException();
         ItemStack held = player.getMainHandItem();
         out(src, "held=" + idOf(held));
         TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "mahoraga_wheel"));
         out(src, "inTag=" + safe(() -> held.is(tag)));
         try {
            Map<String, ISlotType> slots = CuriosApi.getEntitySlots(player);
            out(src, "entitySlots=" + slots.keySet());
            ISlotType ours = slots.get("mahoraga_wheel");
            out(src, "validators=" + (ours == null ? "NULLTYPE" : safe(() -> ours.getValidators().toString())));
         } catch (Throwable t) {
            out(src, "entitySlots EX=" + shortEx(t));
         }
         SlotContext sctx = new SlotContext("mahoraga_wheel", player, 0, false, true);
         out(src, "isStackValid=" + safe(() -> CuriosApi.isStackValid(sctx, held)));
         out(src, "curioCap=" + safe(() -> CuriosApi.getCurio(held).isPresent()));
         out(src, "canEquip=" + safe(() -> CuriosApi.getCurio(held).map(c -> c.canEquip(sctx)).orElse(true)));
         out(src, "head=" + idOf(player.getItemBySlot(EquipmentSlot.HEAD)));
         out(src, "invHandler=" + safe(() -> CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.getStacksHandler("mahoraga_wheel")).map(h -> "slots=" + h.getStacks().getSlots()).orElse("ABSENT")));
      } catch (Throwable t) {
         src.sendFailure(Component.literal("debugcurio EX: " + shortEx(t)));
      }
      return 1;
   }

   private static String idOf(ItemStack stack) {
      return stack.isEmpty() ? "EMPTY" : String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
   }

   private static String safe(Sup s) {
      try {
         return String.valueOf(s.get());
      } catch (Throwable t) {
         return "EX:" + shortEx(t);
      }
   }

   private static String shortEx(Throwable t) {
      return t.getClass().getSimpleName() + "(" + t.getMessage() + ")";
   }

   private static void out(CommandSourceStack src, String line) {
      src.sendSuccess(() -> Component.literal("[DBG] " + line), false);
   }

   @FunctionalInterface
   private interface Sup {
      Object get();
   }
}
