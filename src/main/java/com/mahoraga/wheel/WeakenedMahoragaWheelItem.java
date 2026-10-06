package com.mahoraga.wheel;

import net.minecraft.world.item.Item;

/**
 * Weakened wheel: identical mechanics to the enhanced wheel, one rarity tier
 * lower (RARE instead of EPIC). New enhanced-only features (uncapped mastery,
 * swim / lava / glide / fly / jump / leap adaptations) do not apply to this item.
 * Tooltip and lang entries are shared with the enhanced wheel on purpose.
 */
public class WeakenedMahoragaWheelItem extends MahoragaWheelItem {
   public WeakenedMahoragaWheelItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public boolean isEnhanced() {
      return false;
   }
}