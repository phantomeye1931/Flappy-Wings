package dev.armand.monarch_wings;

import com.mojang.logging.LogUtils;
import dev.armand.monarch_wings.network.ClientboundDoubleJumpPayload;
import dev.armand.monarch_wings.network.ServerboundDoubleJumpPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(MonarchWings.MOD_ID)
public class MonarchWings {
    public static final String MOD_ID = "monarch_wings";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MonarchWings(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Monarch Wings initialized. Vanilla elytra flight disabled.");

        MonarchWingsSounds.register(modEventBus);
        modEventBus.addListener(this::registerPackets);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onLoad);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        // Register the payload, its codec, and the server-side handler execution path
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