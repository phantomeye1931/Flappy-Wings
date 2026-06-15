package dev.armand.monarch_wings;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MonarchWingsClient {

    public static final int FALLDAMAGE_TICKS = 30;
    public static final int LAUNCH_DELAY_TICKS = 7;
    public static final double VERTICAL_BOOST = 0.95;
    public static final double HORIZONTAL_BOOST = 1.4;

    public static void launchPlayer(Player player, Vec3 originalMovement, int ticks) {

        System.out.println("launchPlayer@" + player.getClass().getName());
        System.out.println("\n");

        player.resetFallDistance();

        float amount = (float) ticks / LAUNCH_DELAY_TICKS;

        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(
                Mth.lerp(amount, movement.x, movement.x * HORIZONTAL_BOOST),
                Mth.lerp(amount, movement.y, VERTICAL_BOOST),
                Mth.lerp(amount, movement.z, movement.z * HORIZONTAL_BOOST)
        );

        if (ticks == 0) player.playSound(SoundEvents.ENDER_DRAGON_FLAP);

        if (ticks > 2) {

            int particleCount = 2 + player.getRandom().nextInt(5);

            for (int i = 0; i < particleCount; i++) {
                double dx = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dz = (player.getRandom().nextDouble() - 0.5) * 1.3;
                double dy = player.getRandom().nextDouble() * 0.2;

                player.level().addParticle(
                        ParticleTypes.CLOUD,
                        player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                        dx * 0.5, -0.4 - player.getRandom().nextDouble() * 0.3, dz * 0.5
                );
            }
        }
    }
}