package io.github.viciscat.buildup.injected;

import io.github.viciscat.buildup.effect.BuildupEffect;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface ExtendedProjectile {
    Object2IntMap<BuildupEffect> buildup$getEffectStacks();
}
