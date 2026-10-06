package com.mahoraga.wheel;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data condition backing {@code data/mahoraga_wheel_default/curios/slots/mahoraga_wheel.json}:
 * the bonus starting curio slot only loads when the config allows it.
 */
public record CurioSlotCondition() implements ICondition {
   public static final MapCodec<CurioSlotCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.stable(new CurioSlotCondition()));
   public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
      NeoForgeRegistries.CONDITION_SERIALIZERS.key(), MahoragaWheelMod.MODID
   );

   static {
      CONDITIONS.register("curio_slot_enabled", () -> CODEC);
   }

   @Override
   public boolean test(IContext context) {
      return WheelConfig.curioSlotEnabled();
   }

   @Override
   public MapCodec<? extends ICondition> codec() {
      return CODEC;
   }
}
