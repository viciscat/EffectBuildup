package io.github.viciscat.buildup;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.kustweaks.item.ItemGunUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class BuildupItems {
    private static final String PREFIX = Tags.MOD_ID + ".";

    public static ItemGunUpgrade[] gunUpgrades = new ItemGunUpgrade[0];

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        gunUpgrades = BuildupGunUpgrades.UPGRADES.stream().map(BuildupGunUpgrades.Upgrade::gunUpgrade).map(ItemGunUpgrade::new).toArray(ItemGunUpgrade[]::new);
        for (ItemGunUpgrade gunUpgrade : gunUpgrades) {
            registry.register(gunUpgrade.setTranslationKey(PREFIX + gunUpgrade.getRegistryName().getPath()));
        }
    }
}
