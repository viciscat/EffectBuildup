package io.github.viciscat.buildup.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleDestinedDeath extends BasicParticle {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("destined_death", 224, 124, 16, 48);
    public ParticleDestinedDeath(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn, 0, 0.02, 0);
        setMaxAge(30);
        setParticleTexture(PARTICLE);
    }

    public ParticleDestinedDeath(World world, Vec3d pos) {
        this(world, pos.x, pos.y, pos.z);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float minU = this.particleTexture.getMinU();
        float maxU = this.particleTexture.getMaxU();
        float minV = this.particleTexture.getMinV();
        float maxV = this.particleTexture.getMaxV();
        float scale = 0.1F * this.particleScale;

        double x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        double y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        double z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] corners = new Vec3d[] {
                new Vec3d(-rotationX * scale - rotationXY * scale, -rotationZ * scale * 3, -rotationYZ * scale - rotationXZ * scale),
                new Vec3d(-rotationX * scale + rotationXY * scale, rotationZ * scale * 3, -rotationYZ * scale + rotationXZ * scale),
                new Vec3d(rotationX * scale + rotationXY * scale, rotationZ * scale * 3, rotationYZ * scale + rotationXZ * scale),
                new Vec3d(rotationX * scale - rotationXY * scale, -rotationZ * scale * 3, rotationYZ * scale - rotationXZ * scale)
        };

        if (this.particleAngle != 0.0F)
        {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d(f10, f11, f12);

            for (int l = 0; l < 4; ++l)
            {
                corners[l] = vec3d.scale(2.0D * corners[l].dotProduct(vec3d)).add(corners[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(corners[l]).scale(2.0 * f9));
            }
        }

        buffer.pos(x + corners[0].x, y + corners[0].y, z + corners[0].z).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(x + corners[1].x, y + corners[1].y, z + corners[1].z).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(x + corners[2].x, y + corners[2].y, z + corners[2].z).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(x + corners[3].x, y + corners[3].y, z + corners[3].z).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }
}
