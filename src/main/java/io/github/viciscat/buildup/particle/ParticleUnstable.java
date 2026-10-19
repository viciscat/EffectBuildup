package io.github.viciscat.buildup.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleUnstable extends BasicParticle {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("unstable", 176, 76, 16, 16);
    private Vec3d currentOffset = Vec3d.ZERO;

    public ParticleUnstable(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0, 0.03, 0);
        setMaxAge(30);
        setParticleTexture(PARTICLE);
    }

    public ParticleUnstable(World worldIn, Vec3d pos) {
        this(worldIn, pos.x, pos.y, pos.z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        Vec3d newOffset = new Vec3d(
                BuildupParticleManager.RANDOM.nextDouble() * 2 - 1,
                BuildupParticleManager.RANDOM.nextDouble() * 2 - 1,
                BuildupParticleManager.RANDOM.nextDouble() * 2 - 1
        ).scale(0.02);
        Vec3d diff = currentOffset.subtract(newOffset);
        this.move(diff.x, diff.y, diff.z);
        this.currentOffset = newOffset;
    }
}
