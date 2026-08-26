package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.io.IOException;

public class EffectInstance implements INBTSerializable<NBTTagCompound> {
    protected final StackDurationStrategy stackDurationStrategy;
    protected float procDamage;
    protected boolean shouldSync;

    protected EffectInstance(StackDurationStrategy stackDurationStrategy) {
        this.stackDurationStrategy = stackDurationStrategy;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("duration", stackDurationStrategy.serializeNBT());
        tag.setFloat("procDamage", procDamage);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        stackDurationStrategy.deserializeNBT(nbt.getCompoundTag("duration"));
        procDamage = nbt.getFloat("procDamage");
    }

    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        this.procDamage = procDamage;
        stackDurationStrategy.stack();
        shouldSync = true;
    }

    public void end(EntityLivingBase target) {
        shouldSync = true;
    }

    public float getProcDamage() {
        return procDamage;
    }

    public boolean shouldSync() {
        return shouldSync;
    }

    public int getStacks() {
        return stackDurationStrategy.getStacks();
    }

    public int getDuration() {
        return stackDurationStrategy.getDuration();
    }

    public void tick(EntityLivingBase target) {
        if (!target.world.isRemote) {
            int stacks = stackDurationStrategy.getStacks();
            stackDurationStrategy.tick();
            if (stacks != stackDurationStrategy.getStacks()) {
                shouldSync = true;
                onStackChange(target);
            }
        }
    }

    protected void onStackChange(EntityLivingBase target) {}

    public void read(PacketBuffer buf) {
        buf.writeCompoundTag(stackDurationStrategy.serializeNBT());
    }
    public void write(PacketBuffer buf) {
        try {
            stackDurationStrategy.deserializeNBT(buf.readCompoundTag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void applyModifier(EntityLivingBase target, IAttribute attribute, AttributeModifier modifier) {
        IAttributeInstance instance = target.getEntityAttribute(attribute);
        applyModifier(instance, modifier);
    }

    protected static void applyModifier(IAttributeInstance instance, AttributeModifier modifier) {
        instance.removeModifier(modifier.getID());
        instance.applyModifier(modifier);
    }
}
