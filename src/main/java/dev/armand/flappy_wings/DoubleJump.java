package dev.armand.flappy_wings;

import dev.armand.flappy_wings.util.SmoothGradient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class DoubleJump {

    public static final int LAUNCH_DELAY_TICKS = 7;

    public static final SmoothGradient verticalToVerticalBoost;
    public static final SmoothGradient verticalToHorizontalBoost;
    public static final SmoothGradient horizontalToHorizontalBoost;

    static {
        verticalToVerticalBoost = new SmoothGradient();
        verticalToHorizontalBoost = new SmoothGradient();
        horizontalToHorizontalBoost = new SmoothGradient();

        verticalToVerticalBoost.addStop(0.3, 0.6);
        verticalToVerticalBoost.addStop(0.1, 1);
        verticalToVerticalBoost.addStop(-0.7, 1);
        verticalToVerticalBoost.addStop(-1.6, 0.6);

        verticalToHorizontalBoost.addStop(0.5, 0.5);
        verticalToHorizontalBoost.addStop(0, 0.3);
        verticalToHorizontalBoost.addStop(-0.7, 0.3);
        verticalToHorizontalBoost.addStop(-1.6, 0.8);

        horizontalToHorizontalBoost.addStop(-0.1, 1);
        horizontalToHorizontalBoost.addStop(0, 0.4);
        horizontalToHorizontalBoost.addStop(0.1, 1);
    }

    public static void launchPlayer(Player player, Vec3 originalMovement, int ticks) {

        player.resetFallDistance();

        float amount = (float) ticks / LAUNCH_DELAY_TICKS;

        Vec3 movement = player.getDeltaMovement();

        double verticalSpeed = originalMovement.y;
        System.out.println("ORIGINAL Y: " + verticalSpeed);

        BiFunction<Double, Boolean, Double> horizontal = (speedHorizontal, isX) -> {
            float yaw = player.getYRot();
            double dx = -Math.sin(Math.toRadians(yaw));
            double dz = Math.cos(Math.toRadians(yaw));

            double boost = (isX ? dx : dz) * Config.horizontalMultiplier * verticalToHorizontalBoost.getValue(verticalSpeed);

            return speedHorizontal * 0 + boost * horizontalToHorizontalBoost.getValue(speedHorizontal);
        };
        UnaryOperator<Double> vertical = (speedVertical) -> verticalToVerticalBoost.getValue(verticalSpeed) * Config.verticalBoost;

        player.setDeltaMovement(
                Mth.lerp(amount, movement.x, horizontal.apply(movement.x, true)),
                Mth.lerp(amount, movement.y, vertical.apply(movement.y)),
                Mth.lerp(amount, movement.z, horizontal.apply(movement.z, false))
        );

        if (ticks == 0) player.playSound(
                FlappyWingsSounds.ELYTRA_FLAP.get(),
                1,
                0.7f + player.getRandom().nextFloat() * 0.3f
        );

        if (ticks > 2) {

            int particleCount = 2 + player.getRandom().nextInt(3);

            for (int i = 0; i < particleCount; i++) {
                double dx = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dz = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dy = player.getRandom().nextDouble() * 0.2;

                player.level().addParticle(
                        FlappyWingsParticles.ELYTRA_WIND.get(),
                        player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                        dx * 0.5, -0.4 - player.getRandom().nextDouble() * 0.3, dz * 0.5
                );
            }
        }
    }

    public static void landingParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {

            int particleCount = 8 + player.getRandom().nextInt(5);

            for (int i = 0; i < particleCount; i++) {
                double angle = (i * Math.PI * 2) / particleCount;

                // Form a small circle around the player's feet
                double dx = Math.cos(angle) * 0.6 + player.getRandom().nextDouble() * 0.4 - 0.2; // This returns [0, 5) which gets rounded down to [0, 4]
                double dz = Math.sin(angle) * 0.6 + player.getRandom().nextDouble() * 0.4 - 0.2;

                serverLevel.sendParticles(
                        FlappyWingsParticles.ELYTRA_POOF.get(),
                        player.getX() + dx, player.getY() + 0.1, player.getZ() + dz, // Position
                        0, dx * 0.2, 0.5, dz * 0.2, 0
                );
            }
        }
    }
}