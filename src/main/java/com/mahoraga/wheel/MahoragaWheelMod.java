/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel;

import com.mahoraga.wheel.client.ClientSetup;
import com.mahoraga.wheel.network.ModNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@Mod("mahoraga_wheel")
public class MahoragaWheelMod {
   public static final String MODID = "mahoraga_wheel";

   public MahoragaWheelMod() {
      IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
      ModItems.ITEMS.register(modBus);
      ModSounds.SOUNDS.register(modBus);
      modBus.addListener(this::addCreative);
      modBus.addListener((net.minecraftforge.registries.RegisterEvent e) -> {
         if (e.getRegistryKey().equals(Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(CurioSlotCondition.Serializer.INSTANCE);
         }
      });
      ModLoadingContext.get().registerConfig(Type.COMMON, WheelConfig.SPEC);
      ModLoadingContext.get().registerConfig(Type.CLIENT, WheelConfig.CLIENT_SPEC);
      MinecraftForge.EVENT_BUS.register(AdaptationHandler.class);
      MinecraftForge.EVENT_BUS.register(ConvenienceHandler.class);
      ModNetwork.register();
      modBus.addListener(CurioValidators::onCommonSetup);
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.init(modBus));
   }

   private void addCreative(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() == CreativeModeTabs.COMBAT || event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
         event.accept(ModItems.MAHORAGA_WHEEL);
         event.accept(ModItems.MAHORAGA_WHEEL_WEAKENED);
      }
   }
}
