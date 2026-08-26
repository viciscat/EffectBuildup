package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.EffectApplier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.registries.DDDRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class BuildupEffect {
    private final String id;
    private final String damageTypeName;
    private @Nullable DDDDamageType damageType;

    public BuildupEffect(String id, String damageTypeName) {
        this.id = Objects.requireNonNull(id);
        this.damageTypeName = Objects.requireNonNull(damageTypeName);
    }

    abstract void applyEffect(EntityLivingBase entity, int procDamage, @Nullable DamageSource source);

    public DDDDamageType getDamageType() {
        return Objects.requireNonNull(this.damageType, "Damage Type not initialized");
    }

    public final void initializeDamageType() {
        damageType = DDDRegistries.damageTypes.get(this.damageTypeName);
    }

    public String getId() {
        return id;
    }

    public static BuildupEffect create(String id, String damageTypeName, EffectApplier applier) {
        return new BuildupEffect(id, damageTypeName) {
            @Override
            void applyEffect(EntityLivingBase entity, int procDamage, @Nullable DamageSource source) {
                applier.applyEffect(entity, procDamage, source);
            }
        };
    }
}
