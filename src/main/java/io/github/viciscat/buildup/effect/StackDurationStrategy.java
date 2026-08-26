package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupMod;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraftforge.common.util.INBTSerializable;

public interface StackDurationStrategy extends INBTSerializable<NBTTagCompound> {
    void stack();
    int getStacks();
    void setStacks(int stacks);
    int getDuration();
    void tick();

    class DecayingStackStrategy implements StackDurationStrategy {
        private final IntList stacks = new IntArrayList();
        private final int duration;

        public DecayingStackStrategy(int duration) {
            this.duration = duration;
        }

        @Override
        public void tick() {
            IntListIterator iterator = stacks.iterator();
            while (iterator.hasNext()) {
                int i = iterator.nextInt();
                if (i <= 1) iterator.remove();
                else iterator.set(i - 1);
            }
        }

        @Override
        public void stack() {
            stacks.add(duration);
        }

        @Override
        public void setStacks(int stacks) {
            if (stacks > this.stacks.size()) {
                BuildupMod.LOGGER.warn("Setting stacks to a larger value isn't supported for DecayingStackStrategy");
            }
            this.stacks.removeElements(0, Math.max(this.stacks.size() - stacks, 0));
        }

        @Override
        public int getStacks() {
            return stacks.size();
        }

        @Override
        public int getDuration() {
            return stacks.getInt(stacks.size() - 1);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("stacks", new NBTTagIntArray(stacks.toIntArray()));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            stacks.clear();
            stacks.addElements(0, nbt.getIntArray("stacks"));
        }
    }

    class RefreshStrategy implements StackDurationStrategy {
        private final int duration;
        private int stacks = 1;
        private int timeLeft;
        public RefreshStrategy(int duration) {
            this.duration = duration;
            timeLeft = duration;
        }

        @Override
        public void stack() {
            stacks++;
            timeLeft = duration;
        }

        @Override
        public void setStacks(int stacks) {
            this.stacks = stacks;
        }

        @Override
        public int getStacks() {
            return stacks;
        }

        @Override
        public int getDuration() {
            return timeLeft;
        }

        @Override
        public void tick() {
            timeLeft--;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("timeLeft", timeLeft);
            nbt.setInteger("stacks", stacks);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            timeLeft = nbt.getInteger("timeLeft");
            stacks = nbt.getInteger("stacks");
        }
    }
}
