package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.MaterialAndPart;
import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.config.DefenseConfig;
import io.github.viciscat.buildup.config.RawConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.UnstableEffect;
import io.github.viciscat.buildup.enchantment.BuildupEnchantments;
import io.github.viciscat.buildup.mixin.mods.PartMaterialTypeAccessor;
import io.github.viciscat.buildup.network.BuildupPacketHandler;
import io.github.viciscat.buildup.network.BuildupUpdate;
import io.github.viciscat.buildup.network.S2CBuildupProc;
import io.github.viciscat.buildup.network.S2CUpdateOwnBuildup;
import io.github.viciscat.buildup.tinkers.BuildupTinkerModifiers;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import slimeknights.tconstruct.library.materials.Material;
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
import java.util.Set;

class BuildupTrackerImpl implements BuildupTracker, ICapabilitySerializable<NBTTagCompound> {
    private DDDMaps.DamageMap prevDamageMap = DDDMaps.newDamageMap();
    private final EntityLivingBase entity;
    private final DefenseConfig defense;
    private final Map<BuildupEffect, BuildUp> buildup = new Reference2ObjectOpenHashMap<>();
    private final Set<BuildupEffect> needClientSend = new ReferenceOpenHashSet<>();
    private final boolean isPlayer;
    public BuildupTrackerImpl(EntityLivingBase entity) {
        this.entity = entity;
        this.isPlayer = entity instanceof EntityPlayerMP;
        this.defense = BuildupConfig.get().entityDefense().get(entity).orElse(DefenseConfig.EMPTY);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("previous_damage", DDDBaseMap.toNBT(prevDamageMap));
        NBTTagCompound buildupTag = new NBTTagCompound();
        buildup.forEach((effect, buildUp) -> buildupTag.setFloat(effect.getId(), buildUp.current));
        compound.setTag("buildup", buildupTag);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        DDDMaps.DamageMap map = DDDMaps.newDamageMap();
        map.putAll(DDDBaseMap.fromNBT(nbt.getTagList("previous_damage", (byte) 10), () -> 0f));
        NBTTagCompound buildupTag = nbt.getCompoundTag("buildup");
        for (String buildupId : buildupTag.getKeySet()) {
            BuildupEffect effect = Effects.get(buildupId);
            if (effect == null) continue;
            buildup.computeIfAbsent(effect, BuildUp::new).current = buildupTag.getFloat(buildupId);
        }
        prevDamageMap = map;
    }

    public void hit(DamageSource source, Reference2FloatMap<BuildupEffect> buildups, DDDMaps.DamageMap procDamage, boolean ignoreImmunities) {
        this.prevDamageMap = procDamage;
        if (isPlayer) needClientSend.addAll(buildups.keySet());
        buildups.forEach((effect, amount) -> {
            amount *= EffectTracker.getOptional(entity).flatMap(effectTracker -> effectTracker.getInstance(Effects.UNSTABLE)).map(UnstableEffect::getBuildupMultiplier).orElse(1f);
            int triggers = buildup.computeIfAbsent(effect, BuildUp::new).add(amount, ignoreImmunities);
            for (int i = 0; i < triggers; i++) {
                float damage = procDamage.getOrDefault(effect.getDamageType(), -1f);
                if (damage < 0) {
                    damage = (float) (procDamage.values().stream().mapToDouble(Float::doubleValue).sum() / 2);
                }
                BuildupMod.LOGGER.info("Applying effect {} to '{}' ({}) with {} damage", effect.getId(), entity.getName(), entity.getEntityId(), damage);
                effect.applyEffect(entity, damage, source, ignoreImmunities);
                S2CBuildupProc message = new S2CBuildupProc(entity, effect);
                BuildupPacketHandler.INSTANCE.sendToAllTracking(message, entity);
                if (isPlayer) BuildupPacketHandler.INSTANCE.sendTo(message, (EntityPlayerMP) entity);
            }
        });
    }

    @Override
    public void tick() {
        Set<BuildupEffect> decay = new ReferenceOpenHashSet<>();
        if (entity.ticksExisted % 20 == 0) {
            buildup.forEach((effect, buildup) -> {
                final boolean wasZero = buildup.current <= 0;
                if (buildup.decay()) {
                    if (BuildupMod.debug()) BuildupMod.LOGGER.info("Applying effect '{}' to {} due to decay", effect, entity);
                }
                if (buildup.current > 0 || !wasZero) {
                    if (needClientSend.add(effect)) decay.add(effect);
                }
            });
        }
        if (isPlayer && !needClientSend.isEmpty()) {
            List<BuildupUpdate> updates = needClientSend.stream().map(effect -> {
                BuildUp buildUp = buildup.get(effect);
                return new BuildupUpdate(effect, buildUp.current, buildUp.max, decay.contains(effect));
            }).toList();
            BuildupPacketHandler.INSTANCE.sendTo(new S2CUpdateOwnBuildup(updates), (EntityPlayerMP) entity);
            needClientSend.clear();
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
        private float max;
        public BuildUp(BuildupEffect effect) {
            this.effect = effect;
            this.damageType = effect.getDamageType();
        }

        /**
         * @return the amount of time the effect should be triggered
         */
        private int add(float amount, boolean ignoreImmunities) {
            if (!ignoreImmunities && DDDAPI.accessor.getMobResistances(entity).filter(res -> res.hasImmunity(damageType)).isPresent()) return 0;
            current += amount;
            this.max = calculateMax();
            if (BuildupMod.debug()) BuildupMod.LOGGER.info("{}: {} now at {} / {}", entity, effect, current, max);
            int triggers = 0;
            while (current >= max) {
                triggers++;
                current -= max;
            }
            if (triggers > 0) {
                current = 0;
            }
            return triggers;
        }

        private boolean decay() {
            if (current <= 0) return false;
            current--;
            this.max = calculateMax();
            if (current > this.max) {
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
                final int extraDefense = EnchantmentHelper.getEnchantmentLevel(BuildupEnchantments.BUILDUP_DEFENSE, stack) * RawConfig.defense.defenseEnchantmentBuildup +
                        BuildupTinkerModifiers.getCurrent(stack, BuildupTinkerModifiers.BUILDUP_DEFENSE);
                max += DDDAPI.accessor.getArmorResistances(stack)
                        .flatMap(dist -> BuildupConfig.get().armorDefense().get(stack.getItem()).map(c -> new DistConf(dist, c)))
                        .map(distConf -> distConf.dist().distributeArmor(distConf.defense().global() + extraDefense, 0).get(damageType).getArmor() + distConf.defense().extras().getInt(effect))
                        .orElse(0f);
                max += BuildupTinkerModifiers.getCurrent(stack, BuildupTinkerModifiers.DEFENSE_AFFINITY.get(effect));
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
