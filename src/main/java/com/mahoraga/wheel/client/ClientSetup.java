/* Recovered with Vineflower 1.11.2; Minecraft names restored to official 1.20.1 mappings. */
package com.mahoraga.wheel.client;

import com.mahoraga.wheel.ModItems;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class ClientSetup {
   private ClientSetup() {
   }

   public static void init(IEventBus modBus) {
      modBus.addListener(ClientSetup::onClientSetup);
      modBus.addListener(ClientSetup::onAddLayers);
      modBus.addListener(ClientSetup::onRegisterOverlays);
      modBus.addListener(WheelKeys::register);
      MinecraftForge.EVENT_BUS.register(WheelKeys.class);
   }

   private static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         CuriosRendererRegistry.register((Item)ModItems.MAHORAGA_WHEEL.get(), WheelCurioRenderer::new);
         CuriosRendererRegistry.register((Item)ModItems.MAHORAGA_WHEEL_WEAKENED.get(), WheelCurioRenderer::new);
      });
   }

   private static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("adaptation_hud", AdaptationHud.INSTANCE);
   }

   private static void onAddLayers(AddLayers event) {
      for (String skin : event.getSkins()) {
         LivingEntityRenderer renderer = event.getSkin(skin);
         if (renderer != null) {
            renderer.addLayer(new WheelHeadLayer(renderer));
         }
      }

      for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
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
