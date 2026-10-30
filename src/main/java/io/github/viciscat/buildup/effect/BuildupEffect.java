package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.EffectApplier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.registries.DDDRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class BuildupEffect {
    private final String id;
    private final String damageTypeName;
    private final Display display;
    private @Nullable DDDDamageType damageType;

    public BuildupEffect(String id, Display display, String damageTypeName) {
        this.id = Objects.requireNonNull(id);
        this.display = display;
        this.damageTypeName = Objects.requireNonNull(damageTypeName);
    }

    public abstract void applyEffect(EntityLivingBase entity, float procDamage, @Nullable DamageSource source, boolean ignoreImmunities);

    public void applyClientEffect(EntityLivingBase base) {}

    public DDDDamageType getDamageType() {
        return Objects.requireNonNull(this.damageType, "Damage Type not initialized (" + damageTypeName + ")");
    }

    public final void initializeDamageType() {
        damageType = DDDRegistries.damageTypes.get(this.damageTypeName);
    }

    public String getId() {
        return id;
    }

    public Display getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return id + "[" + damageTypeName + "]";
    }

    public static BuildupEffect create(String id, Display display, String damageTypeName, EffectApplier applier, Consumer<EntityLivingBase> clientApplier) {
        return new BuildupEffect(id, display, damageTypeName) {
            @Override
            public void applyEffect(EntityLivingBase entity, float procDamage, @Nullable DamageSource source, boolean ignoreImmunities) {
                applier.applyEffect(entity, procDamage, source, ignoreImmunities);
            }

            @Override
            public void applyClientEffect(EntityLivingBase base) {
                clientApplier.accept(base);
            }
        };
    }
    
    public record Display(int iconId, int color) {
        public Display(int iconId, int color) {
            this.iconId = iconId;
            this.color = 0xFF_00_00_00 | color;
        }
    }
}
