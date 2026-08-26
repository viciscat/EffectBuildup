package io.github.viciscat.buildup;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

@FunctionalInterface
public interface EffectApplier {
    void applyEffect(EntityLivingBase target, int procDamage, @Nullable DamageSource source);
}
