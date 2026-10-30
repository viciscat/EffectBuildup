package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class AblazeEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("ablaze", 188, 64, 12, 12);
    private int activeFor;

    public AblazeEffect() {
        super(new StackDurationStrategy.RefreshStrategy(6 * 20));
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        activeFor++;
        if (activeFor % 20 == 0) {
            target.attackEntityFrom(dmgSrc("fx_fire"), procDamage * 0.55f);
        }
    }

    @Override
    public void proc(EntityLivingBase target, float procDamage, DamageSource source) {
        this.procDamage += this.procDamage == 0 ? procDamage : procDamage * 0.02f;
        stackDurationStrategy.stack();
        shouldSync = true;
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if ((target.ticksExisted & 1) == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.ABLAZE_PROC, 1, 1);
        for (int i = 0; i < 5; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + target.height * 0.4f + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.4f,
                particlePos.z,
                0,
                0.2,
                0
        );
        particle.setGravity(-0.2f);
        particle.setMaxAge(20);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("activeFor", activeFor);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        activeFor = compound.getInteger("activeFor");
    }
}
