package io.github.viciscat.buildup;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.kustweaks.GunUpgrade;
import io.github.viciscat.kustweaks.GunUpgrades;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BuildupGunUpgrades {
    static final List<Upgrade> UPGRADES = new ArrayList<>();
    public static final GunUpgrade IMMUNITY_BREAKER = GunUpgrades.register(BuildupMod.loc("immunity_breaker"), "Immunity Breaker", 1);

    public static void init() {
        Effects.forEach((effect) -> UPGRADES.add(new Upgrade(GunUpgrades.register(new ResourceLocation(Tags.MOD_ID, effect.getId() + "_affinity"), I18n.format("effect_buildup.ddd_effect_buildup." + effect.getId())), effect)));
    }

    public static void forEach(Consumer<Upgrade> consumer) {
        UPGRADES.forEach(consumer);
    }

    public static Iterable<Upgrade> getUpgrades() {
        return UPGRADES;
    }

    public record Upgrade(GunUpgrade gunUpgrade, BuildupEffect effect) {}
}
