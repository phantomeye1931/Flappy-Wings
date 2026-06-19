package dev.armand.monarch_wings;

import dev.armand.monarch_wings.util.SmoothGradient;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.UnaryOperator;

public class DoubleJump {

    public static final int LAUNCH_DELAY_TICKS = 7;

    public static final SmoothGradient verticalRatioGradient;
    public static final SmoothGradient horizontalRatioGradient;

    static {
        verticalRatioGradient = new SmoothGradient();
        horizontalRatioGradient = new SmoothGradient();

        verticalRatioGradient.addStop(0.3, 0.6);
        verticalRatioGradient.addStop(0, 1);
        verticalRatioGradient.addStop(-0.7, 1);
        verticalRatioGradient.addStop(-1.6, 0.6);

        horizontalRatioGradient.addStop(-0.7, 1);
        horizontalRatioGradient.addStop(-1.6, 1.3);
    }

    public static void launchPlayer(Player player, Vec3 originalMovement, int ticks) {

        player.resetFallDistance();

        float amount = (float) ticks / LAUNCH_DELAY_TICKS;

        Vec3 movement = player.getDeltaMovement();

        double verticalSpeed = originalMovement.y;
        System.out.println("ORIGINAL Y: " + verticalSpeed);

        UnaryOperator<Double> horizontal = (speedHorizontal) -> {
            double speedMultiplier = Math.sqrt( 1 / horizontalRatioGradient.getValue(verticalSpeed) );
            System.out.println(speedMultiplier);
            return speedHorizontal * speedMultiplier * Config.horizontalMultiplier;
        };
        UnaryOperator<Double> vertical = (speedVertical) -> verticalRatioGradient.getValue(verticalSpeed) * Config.verticalBoost;

        player.setDeltaMovement(
                Mth.lerp(amount, movement.x, horizontal.apply(movement.x)),
                Mth.lerp(amount, movement.y, vertical.apply(movement.y)),
                Mth.lerp(amount, movement.z, horizontal.apply(movement.z))
        );

        if (ticks == 0) player.playSound(SoundEvents.ENDER_DRAGON_FLAP);

        if (ticks > 2) {

            int particleCount = 2 + player.getRandom().nextInt(3);

            for (int i = 0; i < particleCount; i++) {
                double dx = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dz = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dy = player.getRandom().nextDouble() * 0.2;

                player.level().addParticle(
                        player.getRandom().nextDouble() > 0.5 ? ParticleTypes.GUST : ParticleTypes.SMALL_GUST,
                        player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                        dx * 0.5, -0.4 - player.getRandom().nextDouble() * 0.3, dz * 0.5
                );
            }
        }
    }

    public static void landingParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {

            int particleCount = 4 + player.getRandom().nextInt(5);

            for (int i = 0; i < particleCount; i++) {
                double angle = (i * Math.PI * 2) / particleCount;

                // Form a small circle around the player's feet
                double dx = Math.cos(angle) * 0.3 + player.getRandom().nextDouble() * 0.2 - 0.1;
                double dz = Math.sin(angle) * 0.3 + player.getRandom().nextDouble() * 0.2 - 0.1;

                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        player.getX() + dx, player.getY() + 0.1, player.getZ() + dz, // Position
                        0, dx * 0.2, 0.5, dz * 0.2, 1.0
                );
            }
        }
    }
}