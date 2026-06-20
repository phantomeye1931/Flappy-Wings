package dev.armand.flappy_wings;

import com.mojang.logging.LogUtils;
import dev.armand.flappy_wings.network.ClientboundDoubleJumpPayload;
import dev.armand.flappy_wings.network.ServerboundDoubleJumpPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(FlappyWings.MOD_ID)
public class FlappyWings {
    public static final String MOD_ID = "flappy_wings";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FlappyWings(IEventBus modEventBus, ModContainer modContainer) {
        FlappyWingsSounds.register(modEventBus);
        FlappyWingsParticles.register(modEventBus);
        modEventBus.addListener(this::registerPackets);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onLoad);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToServer(
                ServerboundDoubleJumpPayload.TYPE,
                ServerboundDoubleJumpPayload.CODEC,
                ServerboundDoubleJumpPayload::handle
        );
        registrar.playToClient(
                ClientboundDoubleJumpPayload.TYPE,
                ClientboundDoubleJumpPayload.CODEC,
                ClientboundDoubleJumpPayload::handle
        );
    }
}