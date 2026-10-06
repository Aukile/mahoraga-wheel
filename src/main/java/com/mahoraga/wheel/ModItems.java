package com.mahoraga.wheel;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MahoragaWheelMod.MODID);
   public static final DeferredItem<MahoragaWheelItem> MAHORAGA_WHEEL = ITEMS.register(
      "mahoraga_wheel", loc -> new MahoragaWheelItem(new Properties().durability(100).rarity(Rarity.EPIC).fireResistant())
   );
   public static final DeferredItem<WeakenedMahoragaWheelItem> MAHORAGA_WHEEL_WEAKENED = ITEMS.register(
      "mahoraga_wheel_weakened", loc -> new WeakenedMahoragaWheelItem(new Properties().durability(100).rarity(Rarity.RARE).fireResistant())
   );
}
