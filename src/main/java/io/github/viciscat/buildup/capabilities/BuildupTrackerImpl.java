package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.MaterialAndPart;
import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.config.DefenseConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.enchantment.BuildupEnchantments;
import io.github.viciscat.buildup.mixin.mods.PartMaterialTypeAccessor;
import io.github.viciscat.buildup.tinkers.BuildupTinkerModifiers;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import yeelp.distinctdamagedescriptions.api.DDDAPI;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;
import yeelp.distinctdamagedescriptions.capability.IArmorDistribution;
import yeelp.distinctdamagedescriptions.capability.IMobResistances;
import yeelp.distinctdamagedescriptions.util.lib.DDDBaseMap;
import yeelp.distinctdamagedescriptions.util.lib.DDDMaps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

class BuildupTrackerImpl implements BuildupTracker, ICapabilitySerializable<NBTTagCompound> {
    private DDDMaps.DamageMap prevDamageMap = DDDMaps.newDamageMap();
    private final EntityLivingBase entity;
    private final DefenseConfig defense;
    private final Map<BuildupEffect, BuildUp> buildup = new Reference2ObjectOpenHashMap<>();
    public BuildupTrackerImpl(EntityLivingBase entity) {
        this.entity = entity;
        this.defense = BuildupConfig.get().entityDefense().get(entity).orElse(DefenseConfig.EMPTY);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("previous_damage", DDDBaseMap.toNBT(prevDamageMap));
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        DDDMaps.DamageMap map = DDDMaps.newDamageMap();
        map.putAll(DDDBaseMap.fromNBT(nbt.getTagList("previous_damage", (byte) 10), () -> 0f));
        prevDamageMap = map;
    }

    public void hit(DamageSource source, Reference2FloatMap<BuildupEffect> buildups, DDDMaps.DamageMap procDamage, boolean ignoreImmunities) {
        this.prevDamageMap = procDamage;
        buildups.forEach((effect, amount) -> {
            boolean trigger = buildup.computeIfAbsent(effect, BuildUp::new).add(amount, ignoreImmunities);
            if (trigger) {
                BuildupMod.LOGGER.info("Applying effect '{}' to {} due to hit", effect, entity);
            }
        });
    }

    @Override
    public void tick() {
        if (entity.ticksExisted % 20 == 0) {
            buildup.forEach((effect, buildup) -> {
                if (buildup.decay()) {
                    BuildupMod.LOGGER.info("Applying effect '{}' to {} due to decay", effect, entity);
                }
            });
        }
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY;
    }

    @SuppressWarnings("ConstantValue")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
    }

    private class BuildUp {
        private final BuildupEffect effect;
        private final DDDDamageType damageType;
        private float current;
        public BuildUp(BuildupEffect effect) {
            this.effect = effect;
            this.damageType = effect.getDamageType();
        }

        /**
         * @return true if the effect should be triggered
         */
        private boolean add(float amount, boolean ignoreImmunities) {
            if (!ignoreImmunities && DDDAPI.accessor.getMobResistances(entity).filter(res -> res.hasImmunity(damageType)).isPresent()) return false;
            current += amount;
            float max = calculateMax();
            BuildupMod.LOGGER.info("{}: {} now at {} / {}", entity, effect, current, max);
            if (current >= max) {
                current = 0;
                return true;
            }
            return false;
        }

        private boolean decay() {
            if (current <= 0) return false;
            current--;
            if (current > calculateMax()) {
                current = 0;
                return true;
            }
            return false;
        }

        private float calculateMax() {
            float max = BuildupConfig.get().getDefaultEntityDefense();
            // Armor stuff
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot.getSlotType() == EntityEquipmentSlot.Type.HAND) continue;
                record DistConf(IArmorDistribution dist, DefenseConfig defense) {}
                ItemStack stack = entity.getItemStackFromSlot(slot);
                final int extraDefense = EnchantmentHelper.getEnchantmentLevel(BuildupEnchantments.BUILDUP_DEFENSE, stack) * 100 +
                        BuildupTinkerModifiers.getLevel(stack, BuildupTinkerModifiers.BUILDUP_DEFENSE) * 110;
                max += DDDAPI.accessor.getArmorResistances(stack)
                        .flatMap(dist -> BuildupConfig.get().armorDefense().get(stack.getItem()).map(c -> new DistConf(dist, c)))
                        .map(distConf -> distConf.dist().distributeArmor(distConf.defense().global() + extraDefense, 0).get(damageType).getArmor() + distConf.defense().extras().getInt(effect))
                        .orElse(0f);
                if (stack.getItem() instanceof TinkersItem tinkersItem) {
                    List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));
                    List<PartMaterialType> components = tinkersItem.getRequiredComponents();
                    if (materials.size() == components.size()) {
                        for (int i = 0; i < materials.size(); i++) {
                            Material material = materials.get(i);
                            PartMaterialTypeAccessor type = (PartMaterialTypeAccessor) components.get(i);
                            for (String part : type.getNeededTypes()) {
                                MaterialAndPart materialAndPart = new MaterialAndPart(material, part);
                                DDDAPI.accessor.getArmorResistances(stack)
                                        .flatMap(dist -> BuildupConfig.get().materialPartDefense().get(materialAndPart).map(c -> new DistConf(dist, c)))
                                        .map(distConf -> distConf.dist().distributeArmor(distConf.defense().global(), 0).get(damageType).getArmor() + distConf.defense().extras().getInt(effect));
                            }
                        }
                    }
                }
            }
            // Entity stuff
            record DistConf(IMobResistances dist, DefenseConfig defense) {}
            max += defense.extras().getInt(effect);
            max += DDDAPI.accessor.getMobResistances(entity)
                    .flatMap(dist -> BuildupConfig.get().entityDefense().get(entity).map(c -> new DistConf(dist, c)))
                    .map(distConf -> distConf.defense().global() * (1 + distConf.dist().getBaseResistance(damageType))).orElse(0f);
            return max;
        }
    }
}
