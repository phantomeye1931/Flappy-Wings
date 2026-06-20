package dev.armand.flappy_wings.network;

import dev.armand.flappy_wings.DoubleJumper;
import dev.armand.flappy_wings.FlappyWings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ServerboundDoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<ServerboundDoubleJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FlappyWings.MOD_ID, "double_jump"));

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
                DoubleJumper jumper = (DoubleJumper) serverPlayer;

//                jumper.flappyWings$setLastDoubleJumpTick(serverPlayer.tickCount);
//                jumper.flappyWings$setHasDoubleJumped(true);

                jumper.flappyWings$startDoubleJumping(serverPlayer);

                // Tell all surrounding players to run the animation locally
                PacketDistributor.sendToPlayersTrackingEntity(
                        serverPlayer,
                        new ClientboundDoubleJumpPayload(serverPlayer.getId())
                );
            }
        });
    }
}
