package io.github.viciscat.buildup.tinkers;

import c4.conarm.lib.modifiers.ArmorModifier;
import c4.conarm.lib.utils.RecipeMatchHolder;
import io.github.viciscat.buildup.BuildupItems;
import io.github.viciscat.buildup.config.RawConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;

public class BuildupDefenseAffinityModifier extends ArmorModifier {
    public BuildupDefenseAffinityModifier(BuildupEffect effect) {
        super(effect.getId() + "_affinity", effect.getDisplay().color());
        addAspects(new ModifierAspect.MultiAspect(this, Integer.MAX_VALUE, RawConfig.defense.conarmDefenseSpecificAffinity, 1));
        RecipeMatchHolder.addItem(this, BuildupItems.effectToDust.get(effect));
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {

    }

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
