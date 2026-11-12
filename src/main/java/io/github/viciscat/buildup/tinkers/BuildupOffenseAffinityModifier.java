package io.github.viciscat.buildup.tinkers;

import io.github.viciscat.buildup.BuildupItems;
import io.github.viciscat.buildup.config.RawConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

public class BuildupOffenseAffinityModifier extends ToolModifier {
    public BuildupOffenseAffinityModifier(BuildupEffect effect) {
        super(effect.getId() + "_affinity", effect.getDisplay().color());
        addAspects(new ModifierAspect.MultiAspect(this, Integer.MAX_VALUE, RawConfig.attack.offenseSpecificAffinity, 1));
        addItem(BuildupItems.effectToDust.get(effect));
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {

    }

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
