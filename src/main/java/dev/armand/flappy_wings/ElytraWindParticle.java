package dev.armand.flappy_wings;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class ElytraWindParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ElytraWindParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, boolean isPoof) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.gravity = 0.4F;
        this.lifetime = 10 + this.random.nextInt(20);
        this.quadSize *= 1 + this.random.nextFloat() * 0.5F;

        // Override particle momentum because Mojang is giving me headaches
        if (isPoof) {
            this.lifetime = 10 + this.random.nextInt(10);

            this.yd = this.random.nextFloat() * 0.1F;

            this.xd = -0.03F + this.random.nextFloat() * 0.06F;
            this.zd = -0.03F + this.random.nextFloat() * 0.06F;
        }

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public net.minecraft.client.particle.@NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final boolean isPoof;

        public Provider(SpriteSet sprites, boolean isPoof) {
            this.sprites = sprites;
            this.isPoof = isPoof;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ElytraWindParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, isPoof);
        }
    }
}
