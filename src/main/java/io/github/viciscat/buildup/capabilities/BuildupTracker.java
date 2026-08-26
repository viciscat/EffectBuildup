package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.BuildupEffect;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yeelp.distinctdamagedescriptions.util.lib.DDDMaps;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public interface BuildupTracker extends INBTSerializable<NBTTagCompound> {
    @CapabilityInject(BuildupTracker.class)
    Capability<BuildupTracker> CAPABILITY = null;
    ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "buildup_tracker");

    @SuppressWarnings("DataFlowIssue")
    static @Nullable BuildupTracker get(EntityLivingBase entity) {
        return entity.getCapability(CAPABILITY, null);
    }

    void hit(DamageSource source, Reference2FloatMap<BuildupEffect> buildups, DDDMaps.DamageMap procDamage, boolean ignoreImmunities);

    void tick();

    class Storage implements Capability.IStorage<BuildupTracker> {
        public static final Storage INSTANCE = new Storage();
        private Storage() {}

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<BuildupTracker> capability, BuildupTracker instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<BuildupTracker> capability, BuildupTracker instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }

    @SubscribeEvent
    static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject().world == null || event.getObject().world.isRemote || !(event.getObject() instanceof EntityLivingBase base)) return;
        event.addCapability(ID, new BuildupTrackerImpl(base));
    }

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        BuildupTracker tracker = get(event.getEntityLiving());
        if (tracker == null) return;
        tracker.tick();
    }
}
