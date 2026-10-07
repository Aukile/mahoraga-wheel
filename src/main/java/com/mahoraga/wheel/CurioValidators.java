package com.mahoraga.wheel;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Registers the tag-independent slot validator predicate. */
public final class CurioValidators {
    public static final ResourceLocation WHEEL_ITEM =
        new ResourceLocation("mahoraga_wheel", "wheel_item");

    private CurioValidators() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurioPredicate(
            WHEEL_ITEM, sr -> sr.stack().getItem() instanceof MahoragaWheelItem));
    }
}
