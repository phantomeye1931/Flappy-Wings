package dev.armand.monarch_wings.network;

import dev.armand.monarch_wings.MonarchWings;
import dev.armand.monarch_wings.DoubleJumper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ServerboundDoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<ServerboundDoubleJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MonarchWings.MOD_ID, "double_jump"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundDoubleJumpPayload> CODEC =
            StreamCodec.unit(new ServerboundDoubleJumpPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Handles the packet on the server network thread
    public static void handle(final ServerboundDoubleJumpPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Access the unique field in your mixin accessor style to sync the tick
                ((DoubleJumper) serverPlayer).monarchWings$setLastDoubleJumpTick(serverPlayer.tickCount);
            }
        });
    }
}
