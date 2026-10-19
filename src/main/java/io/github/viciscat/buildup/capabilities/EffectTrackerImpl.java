package io.github.viciscat.buildup.capabilities;

import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.EffectInstance;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.InstancedEffect;
import io.github.viciscat.buildup.network.BuildupPacketHandler;
import io.github.viciscat.buildup.network.EffectUpdate;
import io.github.viciscat.buildup.network.S2CUpdateEntityEffects;
import io.github.viciscat.buildup.network.S2CUpdateOwnEffects;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EffectTrackerImpl implements EffectTracker, ICapabilitySerializable<NBTTagCompound> {
    private final EntityLivingBase entity;
    private final Map<InstancedEffect<?>, EffectInstance> map = new Reference2ObjectOpenHashMap<>();

    public EffectTrackerImpl(EntityLivingBase entity) {
        this.entity = entity;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY;
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantValue")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound effects = new NBTTagCompound();
        map.forEach((effect, instance) -> effects.setTag(effect.getId(), instance.serializeNBT()));
        compound.setTag("effects", effects);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        map.clear();
        NBTTagCompound effects = nbt.getCompoundTag("effects");
        for (String effectId : effects.getKeySet()) {
            BuildupEffect effect = Effects.get(effectId);
            if (!(effect instanceof InstancedEffect<?> instancedEffect)) continue;
            getOrCreateInstance(instancedEffect).deserializeNBT(effects.getCompoundTag(effectId));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EffectInstance> T getOrCreateInstance(InstancedEffect<T> effect) {
        return (T) map.computeIfAbsent(effect, _ -> effect.getInstanceFactory().newInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EffectInstance> Optional<T> getInstance(InstancedEffect<T> effect) {
        return Optional.ofNullable((T) map.get(effect));
    }

    @Override
    public void tick() {
        if (entity.world.isRemote) {
            //if (BuildupMod.debug()) BuildupMod.LOGGER.info("Ticking client");
            map.values().forEach(instance -> instance.clientTick(entity));
            return;
        }
        List<InstancedEffect<?>> toRemove = new ArrayList<>();
        Set<InstancedEffect<?>> toSync = new ReferenceArraySet<>();
        for (Map.Entry<InstancedEffect<?>, EffectInstance> entry : map.entrySet()) {
            //if (BuildupMod.debug()) BuildupMod.LOGGER.info("Ticking {} on the server for {}", entry.getKey().getId(), entity);
            entry.getValue().tick(entity);
            if (entry.getValue().getDuration() <= 0) {
                toRemove.add(entry.getKey());
                entry.getValue().end(entity);
            } else if (entry.getValue().shouldSync()) {
                toSync.add(entry.getKey());
            }
        }
        for (InstancedEffect<?> effect : toRemove) {
            if (BuildupMod.debug()) BuildupMod.LOGGER.info("Removing effect {} from '{}' ({})", effect.getId(), entity.getName(), entity.getEntityId());
            map.remove(effect);
        }
        toSync.addAll(toRemove);
        List<EffectUpdate> updates = new ArrayList<>(toSync.size());
        for (InstancedEffect<?> effect : toSync) {
            if (toRemove.contains(effect)) {
                updates.add(EffectUpdate.removed(effect));
            } else {
                ByteBuf buf = Unpooled.buffer();
                map.get(effect).write(new PacketBuffer(buf));
                updates.add(EffectUpdate.update(effect, buf));
            }
        }
        if (!updates.isEmpty()) {
            if (entity instanceof EntityPlayerMP player) {
                BuildupPacketHandler.INSTANCE.sendTo(new S2CUpdateOwnEffects(updates), player);
            }
            BuildupPacketHandler.INSTANCE.sendToAllTracking(new S2CUpdateEntityEffects(entity, updates), entity);

        }
    }

    @Override
    public void onStartTracking(EntityPlayerMP player) {
        List<EffectUpdate> updates = map.entrySet().stream().map(entry -> {
            ByteBuf buffer = Unpooled.buffer();
            entry.getValue().write(new PacketBuffer(buffer));
            EffectUpdate.update(entry.getKey(), buffer);
            return EffectUpdate.update(entry.getKey(), buffer);
        }).toList();
        if (player == entity) {
            BuildupPacketHandler.INSTANCE.sendTo(new S2CUpdateOwnEffects(updates), player);
        } else {
            BuildupPacketHandler.INSTANCE.sendTo(new S2CUpdateEntityEffects(entity, updates), player);
        }
    }

    @Override
    public void update(List<EffectUpdate> updates) {
        if (BuildupMod.debug()) BuildupMod.LOGGER.info("Effect updates: {}", updates);
        for (EffectUpdate update : updates) {
            if (update.removed()) {
                map.remove(update.effect());
            } else {
                getOrCreateInstance(update.effect()).read(new PacketBuffer(update.data()));
            }
        }
    }
}
