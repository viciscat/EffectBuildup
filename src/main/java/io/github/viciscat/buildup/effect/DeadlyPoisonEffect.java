package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class DeadlyPoisonEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("deadly_poison", 140, 64, 12, 12);
    private int ticks;
    private float previousProcDamage;
    public DeadlyPoisonEffect() {
        super(new StackDurationStrategy.RefreshStrategy(10 * 60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, float procDamage, DamageSource source) {
        previousProcDamage += this.procDamage * 0.1f;
        super.proc(target, procDamage, source);
        ticks = 0;
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        ticks++;
        if (ticks <= 10 * 20 && ticks % 20 == 0) {
            target.attackEntityFrom(dmgSrc("fx_poison").setDamageBypassesArmor().setMagicDamage(), procDamage * 0.1f + previousProcDamage);
        }
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 5 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.DEADLY_POISON_PROC, 1, 1);
        for (int i = 0; i < 10; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + target.height * 0.8f + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.1f,
                particlePos.z
        );
        particle.setMaxAge(30);
        particle.setGravity(0.8f);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("ticks", ticks);
        compound.setFloat("previousProcDamage", previousProcDamage);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        ticks = compound.getInteger("ticks");
        previousProcDamage = compound.getFloat("previousProcDamage");
    }
}
