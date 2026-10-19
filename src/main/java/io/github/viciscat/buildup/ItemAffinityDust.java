package io.github.viciscat.buildup;

import io.github.viciscat.buildup.effect.BuildupEffect;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemAffinityDust extends Item {
    public final BuildupEffect effect;

    public ItemAffinityDust(BuildupEffect effect) {
        this.effect = effect;
        setCreativeTab(CreativeTabs.MATERIALS);
        setRegistryName(effect.getId() + "_affinity_dust");
    }
}
