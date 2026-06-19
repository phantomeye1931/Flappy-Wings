package dev.armand.monarch_wings;

import net.minecraft.world.entity.player.Player;

public interface DoubleJumper {
    void monarchWings$setLastDoubleJumpTick(int tick);

    boolean monarchWings$hasDoubleJumped();
    void monarchWings$setHasDoubleJumped(boolean hasDoubleJumped);

    int monarchWings$getTicksSinceDoubleJump();

    void monarchWings$startDoubleJumping(Player player);
}
