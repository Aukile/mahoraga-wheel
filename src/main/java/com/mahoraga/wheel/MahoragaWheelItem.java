/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import com.mahoraga.wheel.client.WheelScreen;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICurio.SoundInfo;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class MahoragaWheelItem extends Item implements ICurioItem, Equipable {
   public static final String CURIO_SLOT = "mahoraga_wheel";

   public MahoragaWheelItem(Properties properties) {
      super(properties);
   }

   public boolean isEnhanced() {
      return true;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   public Holder<SoundEvent> getEquipSound() {
      return SoundEvents.ARMOR_EQUIP_NETHERITE;
   }

   public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
      return armorType != EquipmentSlot.HEAD ? false : !(entity instanceof LivingEntity living && AdaptationHandler.hasWheelInCurios(living));
   }

   public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
      return canEquip(stack, armorType, (Entity)entity);
   }

   public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
      return false;
   }

   public SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
      return new SoundInfo(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.0F, 0.8F);
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return !(slotContext.entity().getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof MahoragaWheelItem);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide) {
         WheelScreen.open(WheelSource.of(hand));
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
   }

   public boolean equipFromHand(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!stack.is(this)) {
         return false;
      } else if (AdaptationHandler.hasWheelInCurios(player) || player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof MahoragaWheelItem) {
         return false;
      } else {
         return this.tryEquipToCurio(player, stack) ? true : this.swapWithEquipmentSlot(this, player.level(), player, hand).getResult().consumesAction();
      }
   }

   public int getMaxDamage(ItemStack stack) {
      return WheelConfig.maxDurability();
   }

   public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
      return repair.is(Items.GOLD_INGOT);
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   private boolean tryEquipToCurio(Player player, ItemStack stack) {
      if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof MahoragaWheelItem) {
         return false;
      } else {
         Optional<ICurioStacksHandler> handler = CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.getStacksHandler("mahoraga_wheel"));
         if (handler.isEmpty()) {
            return false;
         } else {
            IDynamicStackHandler stacks = handler.get().getStacks();

            for (int i = 0; i < stacks.getSlots(); i++) {
               if (stacks.getStackInSlot(i).isEmpty()) {
                  if (!player.level().isClientSide) {
                     stacks.setStackInSlot(i, stack.copy());
                     player.level()
                        .playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 1.0F, 0.8F);
                  }

                  player.awardStat(Stats.ITEM_USED.get(this));
                  stack.shrink(1);
                  return true;
               }
            }

            return false;
         }
      }
   }

   public boolean isFoil(ItemStack stack) {
      return false;
   }

   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("tooltip.mahoraga_wheel.lore").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC}));
      tooltip.add(Component.translatable("tooltip.mahoraga_wheel.desc").withStyle(ChatFormatting.GRAY));
      tooltip.add(
         Component.translatable("tooltip.mahoraga_wheel.summary", new Object[]{AdaptationData.getTurns(stack), AdaptationData.getMastery(stack)})
            .withStyle(ChatFormatting.GOLD)
      );
      List<AdaptationData.Entry> entries = AdaptationData.entries(stack);
      if (!entries.isEmpty()) {
         boolean shift = FMLEnvironment.dist == Dist.CLIENT && MahoragaWheelItem.ClientOnly.hasShift();
         int limit = shift ? 40 : 4;
         int i = 0;

         for (AdaptationData.Entry e : entries) {
            if (i++ >= limit) {
               break;
            }

            boolean done = e.complete();

            ChatFormatting color = switch (e.kind()) {
               case EXISTENCE -> ChatFormatting.LIGHT_PURPLE;
               case DEBUFF -> ChatFormatting.GREEN;
               case DARK -> ChatFormatting.AQUA;
               case RUN -> ChatFormatting.YELLOW;
               case SWIM -> ChatFormatting.BLUE;
               case LAVA -> ChatFormatting.GOLD;
               case GLIDE -> ChatFormatting.WHITE;
               case FLY -> ChatFormatting.DARK_PURPLE;
               case JUMP -> ChatFormatting.DARK_GREEN;
               case LEAP -> ChatFormatting.YELLOW;
               case BREATH -> ChatFormatting.DARK_AQUA;
               case MINE -> ChatFormatting.GRAY;
               case SATIETY -> ChatFormatting.GOLD;
               case KNOCKBACK -> ChatFormatting.WHITE;
               case LAVASIGHT -> ChatFormatting.RED;
               case CLIMB -> ChatFormatting.GREEN;
               case REGEN -> ChatFormatting.LIGHT_PURPLE;
               case WISDOM -> ChatFormatting.YELLOW;
               case STEP -> ChatFormatting.WHITE;
               default -> done ? ChatFormatting.RED : ChatFormatting.WHITE;
            };
            boolean special = e.existence() || e.kind().convenience();
            MutableComponent line = Component.literal(" ")
               .append(AdaptationHandler.bar(e.percent(), special ? color : null))
               .append(Component.literal(" "))
               .append(AdaptationHandler.describe(e).copy().withStyle(color));
            if (e.kind().levelBased()) {
               line.append(Component.literal(" Lv." + e.level()).withStyle(ChatFormatting.YELLOW));
            } else if (done) {
               line.append(Component.literal(" ✔").withStyle(color));
            }

            tooltip.add(line);
         }

         if (entries.size() > limit) {
            tooltip.add(Component.translatable("tooltip.mahoraga_wheel.more", new Object[]{entries.size() - limit}).withStyle(ChatFormatting.DARK_GRAY));
         }
      }

      tooltip.add(Component.translatable("tooltip.mahoraga_wheel.reset_hint").withStyle(ChatFormatting.DARK_GRAY));
   }

   private static final class ClientOnly {
      static boolean hasShift() {
         return Screen.hasShiftDown();
      }
   }
}
