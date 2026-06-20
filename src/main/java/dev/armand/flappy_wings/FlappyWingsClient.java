package dev.armand.flappy_wings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = FlappyWings.MOD_ID, value = Dist.CLIENT)
public class FlappyWingsClient {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FlappyWingsParticles.ELYTRA_WIND.get(), (sprites) -> new ElytraWindParticle.Provider(sprites, false));
        event.registerSpriteSet(FlappyWingsParticles.ELYTRA_POOF.get(), (sprites) -> new ElytraWindParticle.Provider(sprites, true));
    }
}