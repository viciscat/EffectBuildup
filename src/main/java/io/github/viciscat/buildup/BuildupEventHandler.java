package io.github.viciscat.buildup;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.registry.Skill;
import io.github.viciscat.buildup.capabilities.BuildupTracker;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.config.AttackConfig;
import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.StunningEffect;
import io.github.viciscat.buildup.enchantment.BuildupEnchantments;
import io.github.viciscat.buildup.injected.ExtendedProjectile;
import io.github.viciscat.buildup.mixin.mods.PartMaterialTypeAccessor;
import io.github.viciscat.buildup.tinkers.BuildupTinkerModifiers;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import yeelp.distinctdamagedescriptions.api.DDDAPI;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.capability.IDamageDistribution;
import yeelp.distinctdamagedescriptions.init.DDDEnchantments;
import yeelp.distinctdamagedescriptions.registries.DDDRegistries;
import yeelp.distinctdamagedescriptions.util.lib.DDDMaps;
import yeelp.distinctdamagedescriptions.util.lib.damagecalculation.DamageCalculation;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class BuildupEventHandler {
    private static final Reference2ObjectMap<DamageSource, SkillDamageEvent> SOURCE_TO_EVENT = new Reference2ObjectArrayMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSkillDamage(SkillDamageEvent event) {
        //System.out.println("Skill damage event: " + event.getSkill() + " " + event.getAmount() + " " + event.getSource());
        if (event.getAmount() > 0) SOURCE_TO_EVENT.put(event.getSource(), event);
    }

    @SubscribeEvent
    public static void onSkillActivate(SkillActionableEvent event) {
        EffectTracker.getOptional(event.getEntityLiving())
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .filter(StunningEffect::shouldBeStunned)
                .ifPresent(_ -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityHurt(LivingDamageEvent event) {
        BuildupTracker tracker = BuildupTracker.get(event.getEntityLiving());
        if (tracker == null) return;
        Reference2FloatMap<BuildupEffect> buildup = new Reference2FloatArrayMap<>();
        BiConsumer<BuildupEffect, Float> addEffect = (effect, f) -> buildup.put(effect, f + buildup.getFloat(effect));
        BiConsumer<DDDDamageType, Float> addType = (damageType, f) -> {
            BuildupEffect effect = Effects.get(damageType);
            if (effect == null) return;
            addEffect.accept(effect, f);
        };

        DamageSource source = event.getSource();
        SkillDamageEvent skillDamageEvent = SOURCE_TO_EVENT.get(source);
        if (skillDamageEvent != null) {
            Skill skill = skillDamageEvent.getSkill();
            DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(skill.getRegistryName());
            Capabilities.get(source.getTrueSource())
                    .flatMap(skills -> skills.getOwned(skill).or(() -> skills.getActive(skill).map(holder -> holder.data.skill).flatMap(skills::getOwned)))
                    .ifPresent(skillInfo -> {
                        final int level = skillInfo instanceof InfoUpgradeable upgradeable ? upgradeable.getLevel() : 0;
                        config.map.keySet().stream().filter(s -> s.startsWith("BUILDUP:")).forEach(id -> {
                            BuildupEffect effect = Effects.get(id.split(":", 2)[1].trim());
                            if (effect == null) return;
                            float v = DSLEvaluator.evaluateFloat(skill, id, level, 1);
                            addEffect.accept(effect, v);
                        });
                    });

            DDDMaps.DamageMap damage = DDDAPI.accessor.getDDDCombatTracker(event.getEntityLiving())
                    .flatMap(t -> t.getLastCalculation(20))
                    .map(DamageCalculation::getClassifiedDamage)
                    .orElseGet(DDDMaps::newDamageMap);
            tracker.hit(source, buildup, damage, false);
            return;
        }

        record DistConf(IDamageDistribution dist, AttackConfig attack) {
        }
        Consumer<DistConf> applyDamageDistribution = distConf -> {
            DDDMaps.DamageMap damageMap = distConf.dist().distributeDamage(distConf.attack().main());
            damageMap.forEach(addType);
            distConf.attack().extras().forEach((effect, i) -> addEffect.accept(effect, Float.valueOf(i)));
        };
        // FIXME extras from the buildup config do not get applied at all if damage distribution does not exist
        boolean ignoreImmunities = false;
        if (source.getImmediateSource() instanceof EntityLivingBase base) {
            ItemStack mainhand = base.getHeldItemMainhand();
            DDDAPI.accessor.getDamageDistribution(base)
                    .flatMap(dist -> BuildupConfig.get().entityAttack().get(base).map(c -> new DistConf(dist, c)))
                    .ifPresent(applyDamageDistribution);
            DDDAPI.accessor.getDamageDistribution(mainhand)
                    .flatMap(dist -> BuildupConfig.get().itemAttack().get(mainhand.getItem()).map(c -> new DistConf(dist, c)))
                    .ifPresent(applyDamageDistribution);
            ignoreImmunities |= EnchantmentHelper.getMaxEnchantmentLevel(DDDEnchantments.slyStrike, base) > 0;
            ignoreImmunities |= EnchantmentHelper.getMaxEnchantmentLevel(DDDEnchantments.slyStrike, base) > 0;
            int bonusBuildup = EnchantmentHelper.getMaxEnchantmentLevel(BuildupEnchantments.BUILDUP_OFFENSE, base) * 100 +
                    BuildupTinkerModifiers.getCurrent(mainhand, BuildupTinkerModifiers.BUILDUP_OFFENSE);
            if (bonusBuildup > 0) {
                DDDAPI.accessor.getDamageDistribution(mainhand).ifPresent(distribution -> distribution.distributeDamage(bonusBuildup).forEach(addType));
            }
            Effects.forEach(effect -> {
                int build = BuildupTinkerModifiers.getCurrent(mainhand, BuildupTinkerModifiers.OFFENSE_AFFINITY.get(effect));
                if (build > 0) addEffect.accept(effect, (float) build);
            });
            /*record MaterialAndType(Material material, PartMaterialTypeAccessor type) {}
            Optional.of(mainhand.getItem())
                    .filter(TinkersItem.class::isInstance)
                    .map(TinkersItem.class::cast)
                    .stream()
                    .flatMap(item -> {
                        List<PartMaterialType> components = item.getRequiredComponents();
                        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(mainhand));
                        List<MaterialAndType> materialAndTypes = new ArrayList<>(components.size());
                        for (int i = 0; i < components.size(); i++) {
                            Material material = materials.get(i);
                            PartMaterialTypeAccessor type = (PartMaterialTypeAccessor) components.get(i);
                            materialAndTypes.add(new MaterialAndType(material, type));
                        }
                        return materialAndTypes.stream();
                    })
                    .flatMap(materialAndType -> Arrays.stream(materialAndType.type().getNeededTypes()).map(s -> new MaterialAndPart(materialAndType.material, s)));*/

            if (mainhand.getItem() instanceof TinkersItem tinkersItem) {
                List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(mainhand));
                List<PartMaterialType> components = tinkersItem.getRequiredComponents();
                if (materials.size() == components.size()) {
                    for (int i = 0; i < materials.size(); i++) {
                        Material material = materials.get(i);
                        PartMaterialTypeAccessor type = (PartMaterialTypeAccessor) components.get(i);
                        for (String part : type.getNeededTypes()) {
                            MaterialAndPart materialAndPart = new MaterialAndPart(material, part);
                            DDDAPI.accessor.getDamageDistribution(mainhand)
                                    .flatMap(dist -> BuildupConfig.get().materialPartAttack().get(materialAndPart).map(c -> new DistConf(dist, c)))
                                    .ifPresent(applyDamageDistribution);
                        }
                    }
                }
            }

        } else if (source.getImmediateSource() instanceof IProjectile projectile) {
            Optional<IDamageDistribution> distribution = DDDAPI.accessor.getDamageDistribution(projectile);
            distribution
                    .flatMap(dist -> BuildupConfig.get().entityAttack().get((Entity) projectile).map(c -> new DistConf(dist, c)))
                    .ifPresent(applyDamageDistribution);
            ignoreImmunities = source.getTrueSource() instanceof EntityLivingBase base && EnchantmentHelper.getMaxEnchantmentLevel(DDDEnchantments.slyStrike, base) > 0;
            if (source.getImmediateSource() instanceof ExtendedProjectile extendedProjectile) {
                for (Object2IntMap.Entry<BuildupEffect> entry : extendedProjectile.buildup$getEffectStacks().object2IntEntrySet()) {
                    if (entry.getKey() == null)
                        distribution.ifPresent(dist -> dist.distributeDamage(entry.getIntValue()).forEach(addType));
                    else addEffect.accept(entry.getKey(), (float) entry.getIntValue());
                }
            }
        } else {
            DDDRegistries.distributions.getDamageDistribution(source, event.getEntityLiving())
                    .flatMap(dist -> BuildupConfig.get().damageSourceAttack().get(source).map(c -> new DistConf(dist, c)))
                    .ifPresent(applyDamageDistribution);
        }
        ignoreImmunities |= BuildupConfig.get().damageSourceBypassesImmunities(source);
        if (buildup.isEmpty()) return;
        DDDMaps.DamageMap damage = DDDAPI.accessor.getDDDCombatTracker(event.getEntityLiving())
                .flatMap(t -> t.getLastCalculation(20))
                .map(DamageCalculation::getClassifiedDamage)
                .orElseGet(DDDMaps::newDamageMap);
        tracker.hit(source, buildup, damage, ignoreImmunities);
    }
}
