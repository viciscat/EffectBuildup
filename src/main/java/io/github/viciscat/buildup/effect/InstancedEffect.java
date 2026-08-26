package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.EffectApplier;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

public class InstancedEffect<T extends EffectInstance> extends BuildupEffect {

    private final InstanceFactory<T> instanceFactory;
    private @Nullable EffectApplier additionalApplier;

    public InstancedEffect(String id, String damageTypeName, InstanceFactory<T> instanceFactory) {
        this(id, damageTypeName, instanceFactory, null);
    }

    public InstancedEffect(String id, String damageTypeName, InstanceFactory<T> instanceFactory, @Nullable EffectApplier additionalApplier) {
        super(id, damageTypeName);
        this.instanceFactory = instanceFactory;
        this.additionalApplier = additionalApplier;
    }

    @Override
    void applyEffect(EntityLivingBase entity, int procDamage, @Nullable DamageSource source) {
        EffectTracker tracker = EffectTracker.get(entity);
        if (tracker == null) return;
        tracker.getOrCreateInstance(this).proc(entity, procDamage, source);
        if (additionalApplier != null) {
            additionalApplier.applyEffect(entity, procDamage, source);
        }
    }

    public InstanceFactory<T> getInstanceFactory() {
        return instanceFactory;
    }

    public interface InstanceFactory<T extends EffectInstance> {
        T newInstance();
    }
}
