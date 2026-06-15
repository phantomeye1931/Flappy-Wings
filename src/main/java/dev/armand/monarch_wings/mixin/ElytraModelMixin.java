package dev.armand.monarch_wings.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.armand.monarch_wings.DoubleJumper;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraModel.class)
public class ElytraModelMixin {
    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/model/ElytraModel;leftWing:Lnet/minecraft/client/model/geom/ModelPart;",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            method = "setupAnim"
    )
    private void flapWings(
            LivingEntity entity, float f, float g, float h, float i, float j,
            CallbackInfo info,
            @Local(ordinal = 5) LocalFloatRef x_rot,
            @Local(ordinal = 6) LocalFloatRef z_rot,
            @Local(ordinal = 7) LocalFloatRef wing_y,
            @Local(ordinal = 8) LocalFloatRef y_rot
    ) {
        if (entity.isFallFlying()) {
            return;
        }

        // Adjust this cast to match whatever interface you are tracking your double jump state on
        if (!(entity instanceof Player player)) return;

        DoubleJumper accessor = (DoubleJumper) player;

        // Check if they already burned their double jump token
        if (accessor.monarchWings$hasDoubleJumped()) {
            float x_orig = x_rot.get();
            float z_orig = z_rot.get();
            float y_orig = y_rot.get();

            int ticksPassed = accessor.monarchWings$getTicksSinceDoubleJump();

            // 15 -15 0
            // 60 -60 20
            // BACKWARDS - SIDEWAYS OUT - OPENING MIDDLE
            if (ticksPassed > 0) {
                x_rot.set(60.0f * Mth.DEG_TO_RAD);
                z_rot.set(-50.0f * Mth.DEG_TO_RAD);
                y_rot.set(90.0f * Mth.DEG_TO_RAD);
            }
            if (ticksPassed > 5) {
                x_rot.set(x_orig);
                z_rot.set(0.0f * Mth.DEG_TO_RAD);
                y_rot.set(40.0f * Mth.DEG_TO_RAD);
            }
            if (ticksPassed > 9) {
                x_rot.set(x_orig);
                z_rot.set(z_orig);
                y_rot.set(y_orig);
            }

            if (entity.isCrouching()) {
                wing_y.set(3.0f);
            } else {
                wing_y.set(0.0f);
            }
        }
    }
}