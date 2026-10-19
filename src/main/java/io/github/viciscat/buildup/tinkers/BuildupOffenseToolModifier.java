package io.github.viciscat.buildup.tinkers;

import io.github.viciscat.buildup.BuildupItems;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

class BuildupOffenseToolModifier extends ToolModifier {
    BuildupOffenseToolModifier() {
        super("buildup_offense", -1);
        addAspects(new ModifierAspect.MultiAspect(this, Integer.MAX_VALUE, 30, 1));
        addItem(BuildupItems.catalyst);
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {}

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
