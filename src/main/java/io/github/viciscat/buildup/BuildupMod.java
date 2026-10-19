package io.github.viciscat.buildup;

import io.github.viciscat.buildup.capabilities.BuildupCapabilities;
import io.github.viciscat.buildup.command.BuildupCommand;
import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.network.BuildupPacketHandler;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class BuildupMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static boolean debug = false;

    public static boolean debug() {
        return debug;
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     * Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        debug = Minecraft.getMinecraft().getVersion().equals("FML_DEV");
        BuildupConfig.init();
        BuildupCapabilities.register();
        BuildupGunUpgrades.init();
        BuildupPacketHandler.init();
        MinecraftForge.EVENT_BUS.register(BuildupHud.INSTANCE);
        MinecraftForge.EVENT_BUS.register(BuildupParticleManager.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Effects.init();
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new BuildupCommand());
    }
}
