package io.github.viciscat.buildup.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;

@Mixin(value = PartMaterialType.class, remap = false)
public interface PartMaterialTypeAccessor {

    @Accessor("neededTypes")
    String[] getNeededTypes();
}
