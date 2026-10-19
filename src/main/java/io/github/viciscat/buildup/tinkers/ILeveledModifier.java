package io.github.viciscat.buildup.tinkers;

import slimeknights.tconstruct.library.modifiers.IModifier;

public interface ILeveledModifier extends IModifier {

    int getCountPerLevel();
}
