package io.github.viciscat.buildup;

import slimeknights.tconstruct.library.materials.Material;

public record MaterialAndPart(String material, String part) {
    public MaterialAndPart(Material material, String part) {
        this(material.getIdentifier(), part);
    }

    public static MaterialAndPart fromString(String string) {
        String[] split = string.split(":");
        return new MaterialAndPart(split[0], split[1]);
    }
}
