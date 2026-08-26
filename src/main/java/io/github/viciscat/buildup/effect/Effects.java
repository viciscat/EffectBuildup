package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.EffectApplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.api.impl.DDDBuiltInDamageType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Effects {
    private Effects() {}

    private static final Map<String, BuildupEffect> EFFECTS = new HashMap<>();
    private static final Map<DDDDamageType, BuildupEffect> DAMAGE_TYPE_TO_EFFECT = new HashMap<>();

    private static <T extends BuildupEffect> T register(T effect) {
        EFFECTS.put(effect.getId(),  effect);
        return effect;
    }

    private static BuildupEffect register(String id, String damageTypeName, EffectApplier applier) {
        return register(BuildupEffect.create(id, damageTypeName, applier));
    }

    @SuppressWarnings("unchecked")
    private static <E extends EffectInstance, I extends InstancedEffect<E>> I registerInstanced(String id, String damageTypeName, InstancedEffect.InstanceFactory<E> instanceFactory) {
        return (I) register(new InstancedEffect<>(id, damageTypeName, instanceFactory));
    }

    @SuppressWarnings("unchecked")
    private static <E extends EffectInstance, I extends InstancedEffect<E>> I registerInstanced(String id, String damageTypeName, InstancedEffect.InstanceFactory<E> instanceFactory, EffectApplier applier) {
        return (I) register(new InstancedEffect<>(id, damageTypeName, instanceFactory, applier));
    }

    @Deprecated
    private static BuildupEffect placeholder(String id, String damageTypeName) {
        return register(id, damageTypeName, (_, _, source) -> {});
    }

    public static final InstancedEffect<MegabonkEffect> MEGABONK = registerInstanced("megabonk", DDDBuiltInDamageType.BLUDGEONING.getTypeName(), MegabonkEffect::new);
    public static final BuildupEffect WOUND = register("wound", DDDBuiltInDamageType.PIERCING.getTypeName(), (target, procDamage, _) -> target.attackEntityFrom(new DamageSource("fx_piercing"), procDamage * 8.25f));
    public static final InstancedEffect<BleedOutEffect> BLEED_OUT = registerInstanced("bleed_out", DDDBuiltInDamageType.SLASHING.getTypeName(), BleedOutEffect::new);
    public static final InstancedEffect<AblazeEffect> ABLAZE = registerInstanced("ablaze", DDDBuiltInDamageType.FIRE.getTypeName(), AblazeEffect::new);
    public static final InstancedEffect<FrozenEffect> FROZEN = registerInstanced("frozen", DDDBuiltInDamageType.COLD.getTypeName(), FrozenEffect::new);
    public static final InstancedEffect<DeconstructionEffect> DECONSTRUCTION = registerInstanced("deconstruction", DDDBuiltInDamageType.FORCE.getTypeName(), DeconstructionEffect::new);
    public static final InstancedEffect<EntropyEffect> ENTROPY = registerInstanced("entropy", DDDBuiltInDamageType.PSYCHIC.getTypeName(), EntropyEffect::new);
    public static final InstancedEffect<MeltEffect> MELT = registerInstanced("melt", DDDBuiltInDamageType.ACID.getTypeName(), MeltEffect::new);
    public static final InstancedEffect<DeadlyPoisonEffect> DEADLY_POISON = registerInstanced("deadly_poison", DDDBuiltInDamageType.POISON.getTypeName(), DeadlyPoisonEffect::new);
    public static final BuildupEffect SHOCKWAVE = register("shockwave", DDDBuiltInDamageType.THUNDER.getTypeName(), (target, procDamage, source) -> {
        List<Entity> entities = target.getEntityWorld().getEntitiesInAABBexcluding(source == null ? null : source.getImmediateSource(), new AxisAlignedBB(target.getPositionVector(), target.getPositionVector()).grow(4), entity -> entity instanceof EntityLivingBase);
        DamageSource fxThunder = new DamageSource("fx_thunder");
        for (Entity entity : entities) {
            entity.attackEntityFrom(fxThunder, procDamage * 2.2f);
        }
    });
    public static final InstancedEffect<ChainLightningEffect> CHAIN_LIGHTNING = registerInstanced("chain_lightning", DDDBuiltInDamageType.LIGHTNING.getTypeName(), ChainLightningEffect::new);
    public static final InstancedEffect<DecayedEffect> DECAYED = registerInstanced("decayed", DDDBuiltInDamageType.NECROTIC.getTypeName(), DecayedEffect::new);
    public static final InstancedEffect<JudgementEffect> JUDGEMENT = registerInstanced("judgement", DDDBuiltInDamageType.RADIANT.getTypeName(), JudgementEffect::new);
    public static final InstancedEffect<UnstableEffect> UNSTABLE = registerInstanced("unstable", "ddd_magic", UnstableEffect::new);
    public static final InstancedEffect<DestinedDeathEffect> DESTINED_DEATH = registerInstanced("destined_death", "ddd_true", DestinedDeathEffect::new);

    public static @Nullable BuildupEffect get(String id) {
        return EFFECTS.get(id);
    }

    public static @Nullable BuildupEffect get(DDDDamageType damageType) {
        return DAMAGE_TYPE_TO_EFFECT.get(damageType);
    }

    public static void forEach(Consumer<BuildupEffect> consumer) {
        EFFECTS.values().forEach(consumer);
    }

    public static void init() {
        initDamageTypes();
    }

    public static void initDamageTypes() {
        DAMAGE_TYPE_TO_EFFECT.clear();
        EFFECTS.forEach((_, effect) -> {
            effect.initializeDamageType();
            DAMAGE_TYPE_TO_EFFECT.put(effect.getDamageType(), effect);
        });
    }

}
