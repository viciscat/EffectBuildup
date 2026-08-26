package io.github.viciscat.buildup;

import io.github.viciscat.kustweaks.item.ItemGunUpgrade;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (ItemGunUpgrade upgrade : BuildupItems.gunUpgrades) {
            ResourceLocation location = new ResourceLocation(upgrade.gunUpgrade.id().getNamespace(), "upgrade/" + upgrade.gunUpgrade.id().getPath());
            ModelLoader.setCustomModelResourceLocation(upgrade, 0, new ModelResourceLocation(location, "inventory"));
        }
    }
}
