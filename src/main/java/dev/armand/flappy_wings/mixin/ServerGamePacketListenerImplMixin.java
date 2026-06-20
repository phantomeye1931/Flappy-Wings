package dev.armand.flappy_wings.mixin;

import dev.armand.flappy_wings.util.FlightResetAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements FlightResetAccessor {

    @Shadow public ServerPlayer player;
    @Shadow private int aboveGroundTickCount;
    @Shadow private int aboveGroundVehicleTickCount;

    @Unique
    public void flappyWings$resetFlightAntiCheat() {
        this.aboveGroundTickCount = 0;
        this.aboveGroundVehicleTickCount = 0;
    }
}