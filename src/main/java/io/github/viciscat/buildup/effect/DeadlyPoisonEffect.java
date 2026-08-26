package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class DeadlyPoisonEffect extends EffectInstance {
    private static final DamageSource DAMAGE_SOURCE = new DamageSource("fx_poison").setDamageBypassesArmor().setMagicDamage();
    private int ticks;
    private float previousProcDamage;
    public DeadlyPoisonEffect() {
        super(new StackDurationStrategy.RefreshStrategy(10 * 60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        previousProcDamage = this.procDamage * 0.1f;
        super.proc(target, procDamage, source);
        ticks = 0;
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        ticks++;
        if (ticks <= 10 * 20 && ticks % 20 == 0) {
            target.attackEntityFrom(DAMAGE_SOURCE, procDamage * 0.1f + previousProcDamage);
        }
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
