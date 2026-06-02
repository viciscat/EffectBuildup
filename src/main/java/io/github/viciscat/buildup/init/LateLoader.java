package io.github.viciscat.buildup.init;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

@SuppressWarnings("unused")
public class LateLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return List.of("mixins.mods." + Tags.MOD_ID + ".json");
    }
}
