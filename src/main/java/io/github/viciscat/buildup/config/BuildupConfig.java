package io.github.viciscat.buildup.config;

import com.google.common.collect.ImmutableMap;
import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.MaterialAndPart;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BuildupConfig {
    private static BuildupConfig instance;

    public static BuildupConfig get() {
        if (instance == null) throw new IllegalStateException("Config not initialized!");
        return instance;
    }

    public static void init() {
        if (instance != null) throw new IllegalStateException("Config is already initialized!");
        instance = new BuildupConfig();
    }

    private final Holder<ResourceLocation, Entity, DefenseConfig> entityDefense;
    private final Holder<ResourceLocation, Item, DefenseConfig> armorDefense;
    private final Holder<MaterialAndPart, MaterialAndPart, DefenseConfig> materialPartDefense;
    private final Holder<ResourceLocation, Entity, AttackConfig> entityAttack;
    private final Holder<ResourceLocation, Item, AttackConfig> itemAttack;
    private final Holder<String, DamageSource, AttackConfig> damageSourceAttack;
    private final Holder<MaterialAndPart, MaterialAndPart, AttackConfig> materialPartAttack;
    private final Set<String> damageSourceImmunityBypass;

    private BuildupConfig() {
        this.entityDefense = new Holder<>(RawConfig.defense.entityDefense, ResourceLocation::new, DefenseConfig::fromString, EntityList::getKey, "entity", "defense");
        this.armorDefense = new Holder<>(RawConfig.defense.armorDefense, ResourceLocation::new, DefenseConfig::fromString, Item::getRegistryName, "armor", "defense");
        this.materialPartDefense = new Holder<>(RawConfig.defense.materialPartDefense, MaterialAndPart::fromString, DefenseConfig::fromString, Function.identity(), "part material", "defense");
        this.entityAttack = new Holder<>(RawConfig.attack.entityAttack, ResourceLocation::new, AttackConfig::fromString, EntityList::getKey, "entity", "attack");
        this.itemAttack = new Holder<>(RawConfig.attack.itemAttack, ResourceLocation::new, AttackConfig::fromString, Item::getRegistryName, "item", "attack");
        this.damageSourceAttack = new Holder<>(RawConfig.attack.damageSourceAttack, Function.identity(), AttackConfig::fromString, DamageSource::getDamageType, "damage source", "attack");
        this.materialPartAttack = new Holder<>(RawConfig.attack.materialPartAttack, MaterialAndPart::fromString, AttackConfig::fromString, Function.identity(), "part material", "attack");
        damageSourceImmunityBypass = new ObjectOpenHashSet<>(RawConfig.damageSourceImmunityBypass);
    }

    private static <A, B> Map<A, B> readMap(String[] toRead, Function<String, A> idReader, Function<String, B> valueReader, String idType, String valueType) {
        ImmutableMap.Builder<A, B> builder = ImmutableMap.builder();
        for (int i = 0; i < toRead.length; i++) {
            String line = toRead[i];
            String[] split = line.split(" ", 2);
            if (split.length == 1) {
                BuildupMod.LOGGER.error("{} {} at index {} seems to only contain the {} id and nothing else.", StringUtils.capitalize(idType), valueType, i, idType);
                continue;
            }
            try {
                builder.put(idReader.apply(split[0]), valueReader.apply(split[1]));
            } catch (Exception e) {
                BuildupMod.LOGGER.error("Error while parsing {} {} at index {}.", idType, valueType, i, e);
            }
        }
        return builder.build();
    }

    public Holder<ResourceLocation, Entity, AttackConfig> entityAttack() {
        return entityAttack;
    }

    public Holder<ResourceLocation, Entity, DefenseConfig> entityDefense() {
        return entityDefense;
    }

    public Holder<MaterialAndPart, MaterialAndPart, DefenseConfig> materialPartDefense() {
        return materialPartDefense;
    }

    public Holder<ResourceLocation, Item, AttackConfig> itemAttack() {
        return itemAttack;
    }

    public Holder<ResourceLocation, Item, DefenseConfig> armorDefense() {
        return armorDefense;
    }

    public Holder<String, DamageSource, AttackConfig> damageSourceAttack() {
        return damageSourceAttack;
    }

    public Holder<MaterialAndPart, MaterialAndPart, AttackConfig> materialPartAttack() {
        return materialPartAttack;
    }

    public int getDefaultEntityDefense() {
        return RawConfig.defense.defaultDefense;
    }

    public boolean damageSourceBypassesImmunities(String damageSource) {
        return damageSourceImmunityBypass.contains(damageSource);
    }

    public boolean damageSourceBypassesImmunities(DamageSource damageSource) {
        return damageSourceBypassesImmunities(damageSource.getDamageType());
    }

    // this is stupid but I love it
    public static class Holder<Id, IdObject, Value> {
        private final Map<Id, Value> map;
        private final Function<IdObject, Id> objectToId;
        public Holder(String[] toRead, Function<String, Id> idReader, Function<String, Value> valueReader, Function<IdObject, Id> objectToId, String idType, String valueType) {
            map = readMap(toRead, idReader, valueReader, idType, valueType);
            this.objectToId = objectToId;
        }

        public Optional<Value> getFromId(Id id) {
            return Optional.ofNullable(map.get(id));
        }

        public Optional<Value> get(IdObject id) {
            return getFromId(objectToId.apply(id));
        }
    }
}
