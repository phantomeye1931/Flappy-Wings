package dev.armand.flappy_wings;

import net.minecraft.world.entity.player.Player;

public interface DoubleJumper {
    void flappyWings$setLastDoubleJumpTick(int tick);

    boolean flappyWings$hasDoubleJumped();
    void flappyWings$setHasDoubleJumped(boolean hasDoubleJumped);

    int flappyWings$getTicksSinceDoubleJump();

    void flappyWings$startDoubleJumping(Player player);
}
