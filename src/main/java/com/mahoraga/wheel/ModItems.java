/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "mahoraga_wheel");
   public static final RegistryObject<Item> MAHORAGA_WHEEL = ITEMS.register(
      "mahoraga_wheel", () -> new MahoragaWheelItem(new Properties().durability(100).rarity(Rarity.EPIC).fireResistant())
   );
   public static final RegistryObject<Item> MAHORAGA_WHEEL_WEAKENED = ITEMS.register(
      "mahoraga_wheel_weakened", () -> new WeakenedMahoragaWheelItem(new Properties().durability(100).rarity(Rarity.RARE).fireResistant())
   );
}
