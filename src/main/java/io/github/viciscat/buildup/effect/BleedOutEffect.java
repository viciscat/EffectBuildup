package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.network.BuildupPacketHandler;
import io.github.viciscat.buildup.network.S2CBleedOutSlash;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class BleedOutEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("bleed_out", 164, 64, 12, 12);
    private int activeFor;

    public BleedOutEffect() {
        super(new StackDurationStrategy.RefreshStrategy(8 * 20));
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        activeFor++;
        if (activeFor % 20 == 0) {
            final int min = Math.min(5, getStacks());
            final float extra = min == 5 ? 0.45f : (min - 1) * 0.1f;
            target.attackEntityFrom(new DamageSource("fx_slashing"), procDamage * (0.4f + extra));
        }
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 4 == 0) {
            Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
            BasicParticle particle = new BasicParticle(
                    target.getEntityWorld(),
                    particlePos.x,
                    particlePos.y + target.height * 0.4f + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.4f,
                    particlePos.z
                    );
            particle.setGravity(1f);
            particle.setMaxAge(40);
            particle.setParticleTexture(PARTICLE);
            BuildupParticleManager.INSTANCE.addParticle(particle);
        }
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() >= 22) {
            stackDurationStrategy.setStacks(5);
            target.attackEntityFrom(new DamageSource("fx_slashing"), target.getHealth() * 0.09f + (target.getMaxHealth() - target.getHealth()) * 0.06f);
            BuildupSounds.playAt(target, BuildupSounds.BLEED_OUT_22_PROC, 1, 1);
            S2CBleedOutSlash message = new S2CBleedOutSlash(target);
            if (target instanceof EntityPlayerMP player) {
                BuildupPacketHandler.INSTANCE.sendTo(message, player);
            }
        } else {
            BuildupSounds.playAt(target, BuildupSounds.BLEED_OUT_PROC, 1, 1);
        }
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
