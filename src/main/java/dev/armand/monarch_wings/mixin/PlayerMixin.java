package dev.armand.monarch_wings.mixin;

import dev.armand.monarch_wings.Config;
import dev.armand.monarch_wings.DoubleJump;
import dev.armand.monarch_wings.DoubleJumper;
import dev.armand.monarch_wings.network.ServerboundDoubleJumpPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
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
    private int monarchWings$lastDoubleJumpTick = 0;

    @Unique
    private Vec3 monarchWings$movementBeforeLaunching = new Vec3(0, 0, 0);

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void preventElytraFlight(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

//        if (true) { // TODO: Cooldown config?
        if (!player.onGround() && !player.isFallFlying() && !player.isInWater() && !player.isInLava()
                && !player.hasEffect(MobEffects.LEVITATION) && !player.onClimbable() && !this.monarchWings$hasDoubleJumped) {

            ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);

            if (itemstack.canElytraFly(player)) {
                this.monarchWings$hasDoubleJumped = true;
                this.monarchWings$lastDoubleJumpTick = player.tickCount;
                this.monarchWings$movementBeforeLaunching = player.getDeltaMovement();

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

        // Reset the token if they hit the ground/liquids
        if (player.onGround() || player.isInWater() || player.isInLava() || player.onClimbable() || player.isSpectator()) {
            this.monarchWings$hasDoubleJumped = false;
        }

        int progress = player.tickCount - this.monarchWings$lastDoubleJumpTick;
        if (this.monarchWings$hasDoubleJumped && progress < DoubleJump.LAUNCH_DELAY_TICKS) {
            DoubleJump.launchPlayer(player, this.monarchWings$movementBeforeLaunching, progress);
        }
    }

    // Intercept fall distance math right before damage calculation
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float decreaseDoubleJumpFallDamage(float fallDistance) {
        Player player = (Player) (Object) this;

        if (player.tickCount - this.monarchWings$lastDoubleJumpTick <= DoubleJump.FALLDAMAGE_TICKS) {
            DoubleJump.landingParticles(player); // Epic landing particles
            return 0f;
        }
        return fallDistance;
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