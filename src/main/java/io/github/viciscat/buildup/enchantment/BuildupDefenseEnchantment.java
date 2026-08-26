package io.github.viciscat.buildup.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

class BuildupDefenseEnchantment extends Enchantment {
    protected BuildupDefenseEnchantment() {
        super(Rarity.RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET});
        setName("buildup_defense");
        setRegistryName("buildup_defense");
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
