package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.EffectDamageSource;
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
    public boolean ignoreImmunities;

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
        shouldSync = true;
    }

    public void proc(EntityLivingBase target, float procDamage, @Nullable DamageSource source) {
        this.procDamage = procDamage;
        stackDurationStrategy.stack();
        shouldSync = true;
    }

    public void clientProc(EntityLivingBase target) {}

    public void end(EntityLivingBase target) {
        shouldSync = true;
    }

    public float getProcDamage() {
        return procDamage;
    }

    public boolean shouldSync() {
        boolean b = shouldSync;
        shouldSync = false;
        return b;
    }

    public int getStacks() {
        return stackDurationStrategy.getStacks();
    }

    public int getDuration() {
        return getStacks() == 0 ? 0 : stackDurationStrategy.getDuration();
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

    protected EffectDamageSource dmgSrc(String damageType) {
        EffectDamageSource source = new EffectDamageSource(damageType.startsWith("fx_") ? damageType : ("fx_" + damageType));
        source.ignoreImmunities = ignoreImmunities;
        return source;
    }

    public void clientTick(EntityLivingBase target) {}

    protected void onStackChange(EntityLivingBase target) {}

    public void read(PacketBuffer buf) {
        try {
            stackDurationStrategy.deserializeNBT(buf.readCompoundTag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(PacketBuffer buf) {
        buf.writeCompoundTag(stackDurationStrategy.serializeNBT());
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
