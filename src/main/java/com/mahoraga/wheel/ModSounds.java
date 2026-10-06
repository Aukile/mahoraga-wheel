package com.mahoraga.wheel;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MahoragaWheelMod.MODID);
   public static final DeferredHolder<SoundEvent, SoundEvent> WHEEL_TURN = reg("wheel_turn");
   public static final DeferredHolder<SoundEvent, SoundEvent> WHEEL_ADAPTED = reg("wheel_adapted");
   public static final DeferredHolder<SoundEvent, SoundEvent> WHEEL_NULLIFY = reg("wheel_nullify");

   private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", name)));
   }
}
