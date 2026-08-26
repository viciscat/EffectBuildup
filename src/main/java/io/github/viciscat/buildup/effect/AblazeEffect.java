package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class AblazeEffect extends EffectInstance {
    private int activeFor;

    public AblazeEffect() {
        super(new StackDurationStrategy.RefreshStrategy(6 * 20));
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        activeFor++;
        if (activeFor % 20 == 0) {
            target.attackEntityFrom(new DamageSource("fx_fire"), procDamage * 0.2f);
        }
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        this.procDamage += this.procDamage == 0 ? procDamage : procDamage * 0.02f;
        stackDurationStrategy.stack();
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
