package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public interface StunTracker extends INBTSerializable<NBTTagCompound> {
    @CapabilityInject(StunTracker.class)
    Capability<StunTracker> CAPABILITY = null;
    ResourceLocation ID = BuildupMod.loc("stun_tracker");

    float tryStun(int stunDuration);
    void tick();

    @SuppressWarnings("DataFlowIssue")
    static @Nullable StunTracker get(EntityLivingBase entity) {
        return entity.getCapability(CAPABILITY, null);
    }
    static Optional<StunTracker> getOptional(EntityLivingBase entity) {
        return Optional.ofNullable(get(entity));
    }

    class Storage implements Capability.IStorage<StunTracker> {
        public static final StunTracker.Storage INSTANCE = new StunTracker.Storage();
        private Storage() {}

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<StunTracker> capability, StunTracker instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<StunTracker> capability, StunTracker instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }

    @SubscribeEvent
    static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject().world == null || event.getObject().world.isRemote || !(event.getObject() instanceof EntityLivingBase base)) return;
        event.addCapability(ID, new StunTrackerImpl(base));
    }

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        StunTracker tracker = get(event.getEntityLiving());
        if (tracker == null) return;
        tracker.tick();
    }
}
