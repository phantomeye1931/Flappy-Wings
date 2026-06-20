package dev.armand.flappy_wings;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FlappyWingsSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, FlappyWings.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ELYTRA_FLAP = SOUND_EVENTS.register(
            "item.elytra.flap",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FlappyWings.MOD_ID, "item.elytra.flap"))
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
