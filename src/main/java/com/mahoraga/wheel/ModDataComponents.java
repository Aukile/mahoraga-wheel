package com.mahoraga.wheel;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom data component holding the wheel's adaptation payload. Stored as one
 * CompoundTag with the same inner layout the Forge version kept in raw NBT
 * ("Adapt" compound, "Turns" and "Mastery" ints), so AdaptationData barely
 * changes. Registered persistent (saved to disk) and network-synchronized
 * (sent to clients for tooltips / HUD / screen).
 */
public final class ModDataComponents {
   public static final DeferredRegister<DataComponentType<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MahoragaWheelMod.MODID);
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ADAPT = TYPES.register(
      "adaptation",
      () -> DataComponentType.<CompoundTag>builder()
         .persistent(CompoundTag.CODEC)
         .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
         .build()
   );

   private ModDataComponents() {
   }

   /** Read-only view of the payload; never mutate the returned tag in place. */
      public static CompoundTag get(net.minecraft.world.item.ItemStack stack) {
      CompoundTag tag = stack.get(ADAPT.get());
      return tag == null ? new CompoundTag() : tag;
   }

   public static CompoundTag get(CompoundTag fallback, net.minecraft.world.item.ItemStack stack) {
      CompoundTag tag = stack.get(ADAPT.get());
      return tag == null ? fallback : tag;
   }

   /** Writable copy of the payload; call {@link #set} after mutating it. */
   public static CompoundTag copy(net.minecraft.world.item.ItemStack stack) {
      CompoundTag tag = stack.get(ADAPT.get());
      return tag == null ? new CompoundTag() : tag.copy();
   }

   public static void set(net.minecraft.world.item.ItemStack stack, CompoundTag tag) {
      if (!stack.isEmpty()) {
         stack.set(ADAPT.get(), tag);
      }
   }

}
