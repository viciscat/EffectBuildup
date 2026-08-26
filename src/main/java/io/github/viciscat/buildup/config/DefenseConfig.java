package io.github.viciscat.buildup.config;

import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.Objects;

public record DefenseConfig(int global, Reference2IntMap<BuildupEffect> extras) {
    public static final DefenseConfig EMPTY = new DefenseConfig(0, Reference2IntMaps.emptyMap());

    public static DefenseConfig fromString(String defense) {
        defense = defense.trim();
        int global = 0;
        String[] entries = defense.split(",");
        Reference2IntMap<BuildupEffect> effectDefense = new Reference2IntOpenHashMap<>(entries.length);
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i].trim();
            String[] split = entry.split(" ");
            if (split.length != 2) throw new IllegalArgumentException("Invalid entry: '" + entry + "' at index " + i);
            if (split[0].equals("global")) {
                global = Integer.parseInt(split[1]);
            } else {
                effectDefense.put(Objects.requireNonNull(Effects.get(split[0]), "Unknown effect id: " + split[0]), Integer.parseInt(split[1]));
            }
        }
        return new DefenseConfig(global, effectDefense.isEmpty() ? Reference2IntMaps.emptyMap() : Reference2IntMaps.unmodifiable(effectDefense));
    }
}
