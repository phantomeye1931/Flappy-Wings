package dev.armand.monarch_wings;

public interface DoubleJumper {
    void monarchWings$setLastDoubleJumpTick(int tick);

    boolean monarchWings$hasDoubleJumped();

    int monarchWings$getTicksSinceDoubleJump();
}
