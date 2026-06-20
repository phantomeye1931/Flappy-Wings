package dev.armand.flappy_wings.network;

import dev.armand.flappy_wings.Config;
import dev.armand.flappy_wings.DoubleJumper;
import dev.armand.flappy_wings.FlappyWings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClientboundDoubleJumpPayload(int playerId) implements CustomPacketPayload {
    public static final Type<ClientboundDoubleJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FlappyWings.MOD_ID, "double_jump_anim"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundDoubleJumpPayload> CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    ClientboundDoubleJumpPayload::playerId,
                    ClientboundDoubleJumpPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundDoubleJumpPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side tracking update
            if (Minecraft.getInstance().level != null &&
                    Minecraft.getInstance().level.getEntity(payload.playerId()) instanceof Player player) {

                DoubleJumper jumper = (DoubleJumper) player;
                jumper.flappyWings$startDoubleJumping(player);

                ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
                player.getCooldowns().addCooldown(chestItem.getItem(), Config.cooldownTicks);
            }
        });
    }
}
