package io.github.viciscat.buildup.tinkers;

import c4.conarm.lib.modifiers.ArmorModifier;
import io.github.viciscat.buildup.BuildupItems;
import io.github.viciscat.buildup.config.RawConfig;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;

class BuildupDefenseArmorModifier extends ArmorModifier {
    BuildupDefenseArmorModifier() {
        super("buildup_defense", -1);
        addAspects(new ModifierAspect.MultiAspect(this, Integer.MAX_VALUE, RawConfig.defense.conarmDefenseGeneralAffinity, 1));
        addItem(BuildupItems.catalyst);
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {}

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
