/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "mahoraga_wheel");
   public static final RegistryObject<SoundEvent> WHEEL_TURN = reg("wheel_turn");
   public static final RegistryObject<SoundEvent> WHEEL_ADAPTED = reg("wheel_adapted");
   public static final RegistryObject<SoundEvent> WHEEL_NULLIFY = reg("wheel_nullify");

   private static RegistryObject<SoundEvent> reg(String name) {
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mahoraga_wheel", name)));
   }
}
