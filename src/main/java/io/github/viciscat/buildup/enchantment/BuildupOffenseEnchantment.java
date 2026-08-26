package io.github.viciscat.buildup.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

class BuildupOffenseEnchantment extends Enchantment {
    protected BuildupOffenseEnchantment() {
        super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
        setName("buildup_offense");
        setRegistryName("buildup_offense");
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
