package dev.armand.flappy_wings.mixin;

import dev.armand.flappy_wings.Config;
import dev.armand.flappy_wings.DoubleJump;
import dev.armand.flappy_wings.DoubleJumper;
import dev.armand.flappy_wings.network.ServerboundDoubleJumpPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin implements DoubleJumper {

    @Unique private boolean flappyWings$hasDoubleJumped = false;
    @Unique private int flappyWings$doubleJumpsRemaining = Config.doubleJumpCount;
    @Unique private int flappyWings$lastDoubleJumpTick = 0;
    @Unique private Vec3 flappyWings$movementBeforeLaunching = new Vec3(0, 0, 0);
    @Unique private double flappyWings$minHeightAfterLaunching = 0;
    @Unique private double flappyWings$topHeightAfterLaunching = 0;

    @Unique
    private boolean flappyWings$mayFlyHere(Player player) {
        ResourceLocation dimension = player.level().dimension().location();
        return Config.flyingDimensions.contains(dimension.toString())
                || Config.flyingDimensions.contains(dimension.getPath());
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void preventElytraFlight(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // If we're in a dimension where flying is enabled, SKIP
        if (flappyWings$mayFlyHere(player)) return;

        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (!player.onGround() && !player.isFallFlying() && !player.isInWater() && !player.isInLava()
                && !player.hasEffect(MobEffects.LEVITATION) && !player.onClimbable()
                && !player.getCooldowns().isOnCooldown(itemStack.getItem())
                && this.flappyWings$doubleJumpsRemaining > 0) {

            if (itemStack.canElytraFly(player)) {
                flappyWings$startDoubleJumping(player);

                this.flappyWings$doubleJumpsRemaining--;

                if (player.level().isClientSide()) {
                    // Send a packet immediately so server and client animation tick targets align
                    PacketDistributor.sendToServer(new ServerboundDoubleJumpPayload());
                }
            }
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleDelayedJumpLaunch(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!this.flappyWings$hasDoubleJumped) return;

        if (player.isInWater() || player.isInLava() || player.onClimbable() || player.isSpectator()) {
            this.flappyWings$hasDoubleJumped = false;
            this.flappyWings$doubleJumpsRemaining = Config.doubleJumpCount;
        }

        int ticksSinceJump = player.tickCount - this.flappyWings$lastDoubleJumpTick;

        this.flappyWings$topHeightAfterLaunching = Math.max(this.flappyWings$topHeightAfterLaunching, player.position().y());

        if (this.flappyWings$hasDoubleJumped && ticksSinceJump < DoubleJump.LAUNCH_DELAY_TICKS) {
            this.flappyWings$minHeightAfterLaunching = Math.min(this.flappyWings$minHeightAfterLaunching, player.position().y());

            DoubleJump.launchPlayer(player, this.flappyWings$movementBeforeLaunching, ticksSinceJump);
        }

    }

    // Intercept fall distance math right before damage calculation
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float decreaseDoubleJumpFallDamage(float fallDistance) {
        Player player = (Player) (Object) this;

        if (!this.flappyWings$hasDoubleJumped) return fallDistance;

        this.flappyWings$hasDoubleJumped = false; // Reset because we hit the ground
        this.flappyWings$doubleJumpsRemaining = Config.doubleJumpCount;

        double safeFallHeight = this.flappyWings$minHeightAfterLaunching - player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        boolean landingAboveJump = player.getY() > safeFallHeight;

        if (landingAboveJump && fallDistance > player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) {
            DoubleJump.landingParticles(player); // Epic landing particles

            return 0f;
        }

        double jumpHeight = this.flappyWings$topHeightAfterLaunching - this.flappyWings$minHeightAfterLaunching;

        return Math.max(0f, (float) (fallDistance - jumpHeight));
    }

    @Override
    public void flappyWings$startDoubleJumping(Player player) {
        this.flappyWings$hasDoubleJumped = true;
        this.flappyWings$lastDoubleJumpTick = player.tickCount;
        this.flappyWings$movementBeforeLaunching = player.getDeltaMovement();

        this.flappyWings$topHeightAfterLaunching = player.position().y();
        this.flappyWings$minHeightAfterLaunching = player.position().y();
    }

    @Override
    public void flappyWings$setLastDoubleJumpTick(int tick) {
        this.flappyWings$lastDoubleJumpTick = tick;
    }

    @Override
    public boolean flappyWings$hasDoubleJumped() {
        return this.flappyWings$hasDoubleJumped;
    }

    @Override
    public void flappyWings$setHasDoubleJumped(boolean hasDoubleJumped) {
        this.flappyWings$hasDoubleJumped = hasDoubleJumped;
    }

    @Override
    public int flappyWings$getTicksSinceDoubleJump() {
        Player player = (Player) (Object) this;

        return player.tickCount - this.flappyWings$lastDoubleJumpTick;
    }
}