package dev.armand.monarch_wings.mixin;

import dev.armand.monarch_wings.Config;
import dev.armand.monarch_wings.DoubleJump;
import dev.armand.monarch_wings.DoubleJumper;
import dev.armand.monarch_wings.network.ServerboundDoubleJumpPayload;
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

    @Unique
    private boolean monarchWings$hasDoubleJumped = false;

    @Unique
    private boolean monarchWings$doubleJumpReady = true;

    @Unique
    private int monarchWings$lastDoubleJumpTick = 0;

    @Unique
    private Vec3 monarchWings$movementBeforeLaunching = new Vec3(0, 0, 0);

    @Unique
    private double monarchWings$minHeightAfterLaunching = 0;

    @Unique
    private double monarchWings$topHeightAfterLaunching = 0;

    @Unique
    private boolean monarchWings$mayFlyHere(Player player) {
        ResourceLocation dimension = player.level().dimension().location();
        return Config.flyingDimensions.contains(dimension.toString())
                || Config.flyingDimensions.contains(dimension.getPath());
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void preventElytraFlight(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // If we're in a dimension where flying is enabled, SKIP
        if (monarchWings$mayFlyHere(player)) return;

        if (!player.onGround() && !player.isFallFlying() && !player.isInWater() && !player.isInLava()
                && !player.hasEffect(MobEffects.LEVITATION) && !player.onClimbable() && this.monarchWings$doubleJumpReady) {

            ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);

            if (itemstack.canElytraFly(player)) {
                monarchWings$startDoubleJumping(player);

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

        if (!this.monarchWings$hasDoubleJumped) return;

        if (player.isInWater() || player.isInLava() || player.onClimbable() || player.isSpectator()) {
            this.monarchWings$hasDoubleJumped = false;
            this.monarchWings$doubleJumpReady = true;
        }

        int ticksSinceJump = player.tickCount - this.monarchWings$lastDoubleJumpTick;

        // Reset the double jump after the config-specified cooldown
        if (ticksSinceJump > Config.cooldownTicks) {
            this.monarchWings$doubleJumpReady = true;
        }

        this.monarchWings$topHeightAfterLaunching = Math.max(this.monarchWings$topHeightAfterLaunching, player.position().y());

        if (this.monarchWings$hasDoubleJumped && ticksSinceJump < DoubleJump.LAUNCH_DELAY_TICKS) {
            this.monarchWings$minHeightAfterLaunching = Math.min(this.monarchWings$minHeightAfterLaunching, player.position().y());

            DoubleJump.launchPlayer(player, this.monarchWings$movementBeforeLaunching, ticksSinceJump);
        }

    }

    // Intercept fall distance math right before damage calculation
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float decreaseDoubleJumpFallDamage(float fallDistance) {
        Player player = (Player) (Object) this;

        if (!this.monarchWings$hasDoubleJumped) return fallDistance;
        this.monarchWings$hasDoubleJumped = false; // Reset because we hit the ground
        this.monarchWings$doubleJumpReady = true;

        double safeFallHeight = this.monarchWings$minHeightAfterLaunching - player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        boolean landingAboveJump = player.getY() > safeFallHeight;

        if (landingAboveJump && fallDistance > player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) {
            DoubleJump.landingParticles(player); // Epic landing particles

            return 0f;
        }

        double jumpHeight = this.monarchWings$topHeightAfterLaunching - this.monarchWings$minHeightAfterLaunching;

        return Math.max(0f, (float) (fallDistance - jumpHeight));
    }

    @Override
    public void monarchWings$startDoubleJumping(Player player) {
        this.monarchWings$hasDoubleJumped = true;
        this.monarchWings$doubleJumpReady = false;
        this.monarchWings$lastDoubleJumpTick = player.tickCount;
        this.monarchWings$movementBeforeLaunching = player.getDeltaMovement();

        this.monarchWings$topHeightAfterLaunching = player.position().y();
        this.monarchWings$minHeightAfterLaunching = player.position().y();
    }

    @Override
    public void monarchWings$setLastDoubleJumpTick(int tick) {
        this.monarchWings$lastDoubleJumpTick = tick;
    }

    @Override
    public boolean monarchWings$hasDoubleJumped() {
        return this.monarchWings$hasDoubleJumped;
    }

    @Override
    public void monarchWings$setHasDoubleJumped(boolean hasDoubleJumped) {
        this.monarchWings$hasDoubleJumped = hasDoubleJumped;
    }

    @Override
    public int monarchWings$getTicksSinceDoubleJump() {
        Player player = (Player) (Object) this;

        return player.tickCount - this.monarchWings$lastDoubleJumpTick;
    }
}