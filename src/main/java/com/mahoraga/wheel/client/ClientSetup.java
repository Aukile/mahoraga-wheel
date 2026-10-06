/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.ModItems;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class ClientSetup {
   private ClientSetup() {
   }

   public static void init(IEventBus modBus) {
      modBus.addListener(ClientSetup::onClientSetup);
      modBus.addListener(ClientSetup::onAddLayers);
      modBus.addListener(ClientSetup::onRegisterLayers);
      modBus.addListener(WheelKeys::register);
      NeoForge.EVENT_BUS.register(WheelKeys.class);
   }

   private static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         CuriosRendererRegistry.register((Item)ModItems.MAHORAGA_WHEEL.get(), WheelCurioRenderer::new);
         CuriosRendererRegistry.register((Item)ModItems.MAHORAGA_WHEEL_WEAKENED.get(), WheelCurioRenderer::new);
      });
   }

   private static void onRegisterLayers(RegisterGuiLayersEvent event) {
      event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("mahoraga_wheel", "adaptation_hud"), AdaptationHud.INSTANCE);
   }

   private static void onAddLayers(AddLayers event) {
      for (PlayerSkin.Model skin : event.getSkins()) {
         LivingEntityRenderer renderer = event.getSkin(skin);
         if (renderer != null) {
            renderer.addLayer(new WheelHeadLayer(renderer));
         }
      }

      for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
         EntityRenderer<?> r;
         try {
            // Forge's API has a LivingEntity bound, but the registry also contains
            // non-living types. Keep the original runtime cast/skip behavior.
            @SuppressWarnings("unchecked")
            EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>)type;
            r = event.getRenderer(livingType);
         } catch (ClassCastException var5) {
            continue;
         }

         if (r instanceof LivingEntityRenderer living && living.getModel() instanceof HeadedModel) {
            living.addLayer(new WheelHeadLayer(living));
         }
      }
   }
}