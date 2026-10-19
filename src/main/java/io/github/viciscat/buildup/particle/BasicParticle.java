package io.github.viciscat.buildup.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BasicParticle extends Particle {
    public BasicParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        setScale(1);
    }

    public BasicParticle(World world, Vec3d pos) {
        this(world, pos.x, pos.y, pos.z);
    }

    public BasicParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        setScale(1);
    }

    public BasicParticle(World worldIn, Vec3d pos, Vec3d motion) {
        this(worldIn, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    public void setScale(float scale) {
        this.particleScale = scale;
    }

    public void setCanCollide(boolean canCollide) {
        this.canCollide = canCollide;
    }

    public void setGravity(float gravity) {
        this.particleGravity = gravity;
    }

    public float getAlpha() {
        return this.particleAlpha;
    }

    public Vec3d getPosition() {
        return new Vec3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
