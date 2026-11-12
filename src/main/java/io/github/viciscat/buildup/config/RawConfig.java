package io.github.viciscat.buildup.config;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import net.minecraftforge.common.config.Config;


@Config(modid = Tags.MOD_ID)
public class RawConfig {

    @Config.Comment({
            "In all of the list options here, each line is for a different object (entity, armor, whatever)",
            "Each line is a different object.",
            "Each starts with the object's id.",
            "Next is a comma separated list of defense points for each effect ids. Each entry in the list is '<effect id> <the amount>'",
            "'global' effect id will add the amount to all effect ids."
    })
    public static Defense defense = new Defense();
    @Config.Comment({
            "In all of the list options here, each line is for a different object (entity, item, whatever)",
            "For each line start with object's id and the amount of points to deal that will be distributed among the object's DDD damage distribution.",
            "Next is a comma separated list of EXTRA build up points for each effect ids. Each entry in the list is '<effect id> <the amount>'"
    })
    public static Attack attack = new Attack();

    @Config.Comment("List of damage sources that should bypass all immunities")
    public static String[] damageSourceImmunityBypass = new String[0];



    public static class Defense {

        public int defaultDefense = 100;


        public String[] entityDefense = new String[]{
                "minecraft:sheep global 200, ablaze 20"
        };

        public String[] armorDefense = new String[]{
                "minecraft:iron_chestplate global 200, ablaze 20"
        };

        @Config.Comment("The id is the form 'material:part'.")
        public String[] materialPartDefense = new String[]{
                "magmaslime:head global 30, ablaze 20"
        };

        public int defenseEnchantmentBuildup = 40;

        public int conarmDefenseGeneralAffinity = 30;

        public int conarmDefenseSpecificAffinity = 63;
    }

    public static class Attack {

        public String[] entityAttack = new String[]{
                "minecraft:sheep 30, ablaze 20"
        };

        public String[] itemAttack = new String[]{
                "minecraft:iron_sword 30, ablaze 20"
        };

        public String[] damageSourceAttack = new String[]{
                "lava 30, ablaze 20"
        };

        @Config.Comment("The id is the form 'material:part'.")
        public String[] materialPartAttack = new String[]{
                "magmaslime:head 30, ablaze 20"
        };

        public int offenseEnchantmentBuildup = 30;

        public int offenseGeneralAffinity = 30;

        public int offenseSpecificAffinity = 15;

    }
}
