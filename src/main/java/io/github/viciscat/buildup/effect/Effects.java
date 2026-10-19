package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.EffectApplier;
import io.github.viciscat.buildup.effect.BuildupEffect.Display;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import io.github.viciscat.buildup.particle.ParticleSequenceWound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.api.impl.DDDBuiltInDamageType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Effects {
    private Effects() {
    }

    private static final Map<String, BuildupEffect> EFFECTS = new HashMap<>();
    private static final Map<DDDDamageType, BuildupEffect> DAMAGE_TYPE_TO_EFFECT = new HashMap<>();

    private static <T extends BuildupEffect> T register(T effect) {
        EFFECTS.put(effect.getId(), effect);
        return effect;
    }

    private static BuildupEffect register(String id, Display display, String damageTypeName, EffectApplier applier, Consumer<EntityLivingBase> clientApplier) {
        return register(BuildupEffect.create(id, display, damageTypeName, applier, clientApplier));
    }

    @SuppressWarnings("unchecked")
    private static <E extends EffectInstance, I extends InstancedEffect<E>> I registerInstanced(String id, Display display, String damageTypeName, InstancedEffect.InstanceFactory<E> instanceFactory) {
        return (I) register(new InstancedEffect<>(id, display, damageTypeName, instanceFactory));
    }

    @SuppressWarnings("unchecked")
    private static <E extends EffectInstance, I extends InstancedEffect<E>> I registerInstanced(String id, Display display, String damageTypeName, InstancedEffect.InstanceFactory<E> instanceFactory, EffectApplier applier) {
        return (I) register(new InstancedEffect<>(id, display, damageTypeName, instanceFactory, applier));
    }

    public static final InstancedEffect<MegabonkEffect> MEGABONK = registerInstanced("megabonk", new Display(11, 0xBDBBBB), DDDBuiltInDamageType.BLUDGEONING.getTypeName(), MegabonkEffect::new);
    public static final BuildupEffect WOUND = register("wound", new Display(14, 0x291601), DDDBuiltInDamageType.PIERCING.getTypeName(),
            (target, procDamage, _) -> {
                BuildupSounds.playAt(target, BuildupSounds.WOUND_PROC, 1, 1);
                target.attackEntityFrom(new DamageSource("fx_piercing"), procDamage * 9.25f);
            },
            ParticleSequenceWound::new
    );
    public static final InstancedEffect<BleedOutEffect> BLEED_OUT = registerInstanced("bleed_out", new Display(2, 0xBD1504), DDDBuiltInDamageType.SLASHING.getTypeName(), BleedOutEffect::new);
    public static final InstancedEffect<AblazeEffect> ABLAZE = registerInstanced("ablaze", new Display(0, 0xFF6600), DDDBuiltInDamageType.FIRE.getTypeName(), AblazeEffect::new);
    public static final InstancedEffect<FrozenEffect> FROZEN = registerInstanced("frozen", new Display(9, 0x72A0ED), DDDBuiltInDamageType.COLD.getTypeName(), FrozenEffect::new);
    public static final InstancedEffect<DeconstructionEffect> DECONSTRUCTION = registerInstanced("deconstruction", new Display(6, 0x0CDDED), DDDBuiltInDamageType.FORCE.getTypeName(), DeconstructionEffect::new);
    public static final InstancedEffect<EntropyEffect> ENTROPY = registerInstanced("entropy", new Display(8, 0x5D10C4), DDDBuiltInDamageType.PSYCHIC.getTypeName(), EntropyEffect::new);
    public static final InstancedEffect<MeltEffect> MELT = registerInstanced("armor_melt", new Display(1, 0x95D408), DDDBuiltInDamageType.ACID.getTypeName(), MeltEffect::new);
    public static final InstancedEffect<DeadlyPoisonEffect> DEADLY_POISON = registerInstanced("deadly_poison", new Display(4, 0x0CED18), DDDBuiltInDamageType.POISON.getTypeName(), DeadlyPoisonEffect::new);
    public static final BuildupEffect SHOCKWAVE = register("shockwave", new Display(12, 0xFFFFFF), DDDBuiltInDamageType.THUNDER.getTypeName(), (target, procDamage, source) -> {
                BuildupSounds.playAt(target, BuildupSounds.SHOCKWAVE_PROC, 1, 1);
                List<Entity> entities = target.getEntityWorld().getEntitiesInAABBexcluding(source == null ? null : source.getImmediateSource(), new AxisAlignedBB(target.getPositionVector(), target.getPositionVector()).grow(4), entity -> entity instanceof EntityLivingBase);
                DamageSource fxThunder = new DamageSource("fx_thunder");
                for (Entity entity : entities) {
                    entity.attackEntityFrom(fxThunder, procDamage * 2.2f);
                }
            },
            new Consumer<>() {
                private static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("shockwave", 160, 92, 32, 32);

                @Override
                public void accept(EntityLivingBase target) {
                    for (int i = 0; i < 32; i++) {
                        float angle = (float) i / 32 * (float) Math.TAU;
                        float cos = MathHelper.cos(angle);
                        float sin = MathHelper.sin(angle);
                        double dx = cos * 0.15;
                        double dz = sin * 0.15;
                        BasicParticle particle = new BasicParticle(
                                target.getEntityWorld(),
                                target.posX,
                                target.posY,
                                target.posZ,
                                dx,
                                0,
                                dz
                        );
                        particle.setParticleTexture(PARTICLE);
                        particle.setMaxAge(20);
                        BuildupParticleManager.INSTANCE.addParticle(particle);
                    }
                }
            });
    public static final InstancedEffect<ChainLightningEffect> CHAIN_LIGHTNING = registerInstanced("chain_lightning", new Display(3, 0xEDE611), DDDBuiltInDamageType.LIGHTNING.getTypeName(), ChainLightningEffect::new);
    public static final InstancedEffect<DecayedEffect> DECAYED = registerInstanced("decayed", new Display(5, 0x568575), DDDBuiltInDamageType.NECROTIC.getTypeName(), DecayedEffect::new);
    public static final InstancedEffect<JudgementEffect> JUDGEMENT = registerInstanced("judgement", new Display(10, 0xFAF378), DDDBuiltInDamageType.RADIANT.getTypeName(), JudgementEffect::new);
    public static final InstancedEffect<UnstableEffect> UNSTABLE = registerInstanced("unstable", new Display(13, 0x3A47F0), "ddd_magic", UnstableEffect::new);
    public static final InstancedEffect<DestinedDeathEffect> DESTINED_DEATH = registerInstanced("destined_death", new Display(7, 0x212121), "ddd_true", DestinedDeathEffect::new);

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
