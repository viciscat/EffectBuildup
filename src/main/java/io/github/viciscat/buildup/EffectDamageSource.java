package io.github.viciscat.buildup;

import net.minecraft.util.DamageSource;

public class EffectDamageSource extends DamageSource {
    public boolean ignoreImmunities = false;
    public EffectDamageSource(String damageTypeIn) {
        super(damageTypeIn);
    }
}
