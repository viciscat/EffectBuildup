package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class BuildupPacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
    private static int discriminator;

    public static void init() {
        INSTANCE.registerMessage(S2CUpdateEntityEffects.Handler.class, S2CUpdateEntityEffects.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(S2CUpdateOwnEffects.Handler.class, S2CUpdateOwnEffects.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(S2CUpdateOwnBuildup.Handler.class, S2CUpdateOwnBuildup.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(S2CBuildupProc.Handler.class, S2CBuildupProc.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(S2CBleedOutSlash.Handler.class, S2CBleedOutSlash.class, discriminator++, Side.CLIENT);
    }
}
