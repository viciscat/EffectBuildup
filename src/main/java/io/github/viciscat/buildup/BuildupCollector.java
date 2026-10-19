package io.github.viciscat.buildup;

import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import yeelp.distinctdamagedescriptions.api.DDDDamageType;

public class BuildupCollector {
    private final Object2FloatMap<BuildupEffect> map = new Object2FloatArrayMap<>();

    public Object2FloatMap<BuildupEffect> getMap() {
        return map;
    }

    public void addEffect(BuildupEffect effect, float f) {
        map.put(effect, map.getFloat(effect) + f);
    }

    public void addDamageType(DDDDamageType damageType, float f) {
        BuildupEffect effect = Effects.get(damageType);
        if (effect == null) return;
        addEffect(effect, f);
    }
}
