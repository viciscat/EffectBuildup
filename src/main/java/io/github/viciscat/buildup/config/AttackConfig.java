package io.github.viciscat.buildup.config;

import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.Objects;

public record AttackConfig(int main, Reference2IntMap<BuildupEffect> extras) {
    public static final AttackConfig EMPTY = new AttackConfig(0, Reference2IntMaps.emptyMap());

    public static AttackConfig fromString(String onHit) {
        onHit = onHit.trim();
        String[] entries = onHit.split(",");
        int main = Integer.parseInt(entries[0].trim());
        Reference2IntMap<BuildupEffect> effectDefense = new Reference2IntOpenHashMap<>(entries.length - 1);
        for (int i = 1; i < entries.length; i++) {
            String entry = entries[i].trim();
            String[] split = entry.split(" ");
            if (split.length != 2) throw new IllegalArgumentException("Invalid entry: '" + entry + "' at index " + i);
            effectDefense.put(Objects.requireNonNull(Effects.get(split[0]), "Unknown effect id: " + split[0]), Integer.parseInt(split[1]));
        }
        return new AttackConfig(main, effectDefense.isEmpty() ? Reference2IntMaps.emptyMap() : Reference2IntMaps.unmodifiable(effectDefense));
    }
}
