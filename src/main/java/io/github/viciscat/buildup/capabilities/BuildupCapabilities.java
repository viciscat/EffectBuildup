package io.github.viciscat.buildup.capabilities;

import net.minecraftforge.common.capabilities.CapabilityManager;

public final class BuildupCapabilities {
    public static void register() {
        CapabilityManager.INSTANCE.register(BuildupTracker.class, BuildupTracker.Storage.INSTANCE, () -> null);
    }
}
