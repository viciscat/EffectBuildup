package io.github.viciscat.buildup.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public final class BuildupEnchantments {
    public static final Enchantment BUILDUP_DEFENSE = new BuildupDefenseEnchantment();
    public static final Enchantment BUILDUP_OFFENSE = new BuildupOffenseEnchantment();


    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(BUILDUP_DEFENSE);
        event.getRegistry().register(BUILDUP_OFFENSE);
    }
}
