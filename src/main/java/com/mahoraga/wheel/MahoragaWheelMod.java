package com.mahoraga.wheel;

import com.mahoraga.wheel.client.ClientSetup;
import com.mahoraga.wheel.network.ModNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod("mahoraga_wheel")
public class MahoragaWheelMod {
   public static final String MODID = "mahoraga_wheel";

   public MahoragaWheelMod(IEventBus modBus, ModContainer modContainer) {
      ModItems.ITEMS.register(modBus);
      ModSounds.SOUNDS.register(modBus);
      ModDataComponents.TYPES.register(modBus);
      CurioSlotCondition.CONDITIONS.register(modBus);
      modBus.addListener(this::addCreative);
      
        modBus.addListener(ModNetwork::register);
        modBus.addListener(CurioValidators::onCommonSetup);
      modContainer.registerConfig(ModConfig.Type.COMMON, WheelConfig.SPEC);
      modContainer.registerConfig(ModConfig.Type.CLIENT, WheelConfig.CLIENT_SPEC);
      NeoForge.EVENT_BUS.register(AdaptationHandler.class);
      NeoForge.EVENT_BUS.register(ConvenienceHandler.class);
      NeoForge.EVENT_BUS.register(DebugCurioCommand.class); // TEMP-DEBUG
      if (FMLEnvironment.dist == Dist.CLIENT) {
         ClientSetup.init(modBus);
      }
   }

   private void addCreative(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() == CreativeModeTabs.COMBAT || event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
         event.accept(ModItems.MAHORAGA_WHEEL);
         event.accept(ModItems.MAHORAGA_WHEEL_WEAKENED);
      }
   }
}
