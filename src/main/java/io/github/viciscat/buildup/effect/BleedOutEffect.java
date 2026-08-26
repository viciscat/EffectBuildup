package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class BleedOutEffect extends EffectInstance {
    private int activeFor;

    public BleedOutEffect() {
        super(new StackDurationStrategy.RefreshStrategy(5 * 20));
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        activeFor++;
        if (activeFor % 20 == 0) {
            int min = Math.min(5, getStacks());
            target.attackEntityFrom(new DamageSource("fx_slashing"), procDamage * min * 0.1f);
        }
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() >= 22) {
            stackDurationStrategy.setStacks(5);
            target.attackEntityFrom(new DamageSource("fx_slashing"), target.getHealth() * 0.09f + (target.getMaxHealth() - target.getHealth()) * 0.06f);
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
