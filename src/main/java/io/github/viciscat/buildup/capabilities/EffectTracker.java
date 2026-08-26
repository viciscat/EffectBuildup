package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.EffectInstance;
import io.github.viciscat.buildup.effect.InstancedEffect;
import io.github.viciscat.buildup.network.EffectUpdate;
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
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public interface EffectTracker extends INBTSerializable<NBTTagCompound> {
    @CapabilityInject(EffectTracker.class)
    Capability<EffectTracker> CAPABILITY = null;
    ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "effect_tracker");

    @SuppressWarnings("DataFlowIssue")
    static @Nullable EffectTracker get(EntityLivingBase entity) {
        return entity.getCapability(CAPABILITY, null);
    }

    <T extends EffectInstance> T getOrCreateInstance(InstancedEffect<T> effect);
    <T extends EffectInstance> Optional<T> getInstance(InstancedEffect<T> effect);

    void tick();

    void update(List<EffectUpdate> updates);

    class Storage implements Capability.IStorage<EffectTracker> {
        public static final EffectTracker.Storage INSTANCE = new EffectTracker.Storage();
        private Storage() {}

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<EffectTracker> capability, EffectTracker instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<EffectTracker> capability, EffectTracker instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }

    @SubscribeEvent
    static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject().world == null || !(event.getObject() instanceof EntityLivingBase base)) return;
        event.addCapability(ID, new EffectTrackerImpl(base));
    }

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        EffectTracker tracker = get(event.getEntityLiving());
        if (tracker == null) return;
        tracker.tick();
    }
}
