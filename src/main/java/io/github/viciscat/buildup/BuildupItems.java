package io.github.viciscat.buildup;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.kustweaks.item.ItemGunUpgrade;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class BuildupItems {
    private static final String PREFIX = Tags.MOD_ID + ".";

    public static ItemGunUpgrade[] gunUpgrades = new ItemGunUpgrade[0];
    public static ItemAffinityDust[] dusts = new ItemAffinityDust[0];
    public static final Map<BuildupEffect, ItemAffinityDust> effectToDust = new Reference2ObjectOpenHashMap<>();
    public static Item catalyst = new Item();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        gunUpgrades = BuildupGunUpgrades.UPGRADES.stream().map(BuildupGunUpgrades.Upgrade::gunUpgrade).map(ItemGunUpgrade::new).toArray(ItemGunUpgrade[]::new);
        for (ItemGunUpgrade gunUpgrade : gunUpgrades) {
            gunUpgrade.setCreativeTab(CreativeTabs.COMBAT);
            registry.register(gunUpgrade.setTranslationKey(PREFIX + gunUpgrade.getRegistryName().getPath()));
        }
        List<ItemAffinityDust> items = new ArrayList<>();
        Effects.forEach(effect -> {
            ItemAffinityDust item = new ItemAffinityDust(effect);
            item.setTranslationKey(PREFIX + item.getRegistryName().getPath());
            items.add(item);
            effectToDust.put(effect, item);
            registry.register(item);
        });
        dusts = items.toArray(ItemAffinityDust[]::new);
        catalyst = new Item();
        catalyst.setTranslationKey(PREFIX + "affinity_catalyst");
        catalyst.setRegistryName("affinity_catalyst");
        catalyst.setCreativeTab(CreativeTabs.MATERIALS);
        registry.register(catalyst);
    }
}
