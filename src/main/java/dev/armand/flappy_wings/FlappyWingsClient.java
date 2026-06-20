package dev.armand.flappy_wings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = FlappyWings.MOD_ID, value = Dist.CLIENT)
public class FlappyWingsClient {

    public FlappyWingsClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FlappyWingsParticles.ELYTRA_WIND.get(), (sprites) -> new ElytraWindParticle.Provider(sprites, false));
        event.registerSpriteSet(FlappyWingsParticles.ELYTRA_POOF.get(), (sprites) -> new ElytraWindParticle.Provider(sprites, true));
    }
}