package io.github.viciscat.buildup.capabilities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedList;

public class StunTrackerImpl implements StunTracker, ICapabilitySerializable<NBTTagCompound> {
    private static final int CHECK_PERIOD = 12 * 20;
    private static final int MAX_STUN_TIME = 9 * 20;
    private static final int IMMUNITY_TIME = 5 * 20;
    private final Deque<StunPeriod> stunPeriods = new LinkedList<>();
    private final EntityLivingBase entity;

    private int stage = 0;
    private int timer = 0;

    public StunTrackerImpl(EntityLivingBase entity) {
        this.entity = entity;
    }

    @Override
    public float tryStun(int stunDuration) {
        stunPeriods.add(new StunPeriod(entity.ticksExisted, Math.round(stunDuration * getMultiplier())));
        checkComboBreaker();
        return getMultiplier();
    }

    private float getMultiplier() {
        return switch (stage) {
            case 0 -> 1f;
            case 1 -> .5f;
            case 2 -> .25f;
            default -> 0;
        };
    }

    @Override
    public void tick() {
        if (timer > 0) {
            timer--;
            if (timer <= 0) {
                stage--;
                if (stage > 0) {
                    timer = IMMUNITY_TIME;
                }
            }
        }
    }

    private void checkComboBreaker() {
        int ticksExisted = entity.ticksExisted;
        final int limit = ticksExisted - CHECK_PERIOD;
        stunPeriods.removeIf(period -> period.end() < limit);
        int ticks = 0;
        for (StunPeriod period : stunPeriods) {
            if (period.start() < limit) {
                ticks += period.end() - limit;
            } else {
                ticks += period.duration();
            }
        }
        if (ticks >= MAX_STUN_TIME) {
            stage = Math.min(stage + 1, 3);
            timer = IMMUNITY_TIME;
            stunPeriods.clear();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("stage", stage);
        tag.setInteger("timer", timer);
        NBTTagList list = new NBTTagList();
        for (StunPeriod period : stunPeriods) {
            list.appendTag(period.toNBT());
        }
        tag.setTag("stunPeriods", list);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        stunPeriods.clear();
        stage = nbt.getInteger("stage");
        timer = nbt.getInteger("timer");
        NBTTagList list = nbt.getTagList("stunPeriods", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            stunPeriods.add(StunPeriod.fromNBT(list.getCompoundTagAt(i)));
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
    }


    private record StunPeriod(int start, int duration) {
        public int end() {
            return start + duration;
        }

        public NBTTagCompound toNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("start", start);
            tag.setInteger("duration", duration);
            return tag;
        }

        public static StunPeriod fromNBT(NBTTagCompound tag) {
            return new StunPeriod(tag.getInteger("start"), tag.getInteger("duration"));
        }
    }
}
