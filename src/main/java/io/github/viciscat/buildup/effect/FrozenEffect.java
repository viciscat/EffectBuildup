package io.github.viciscat.buildup.effect;

import arekkuusu.enderskills.common.skill.ability.mobility.wind.Hasten;
import com.tmtravlr.potioncore.PotionCoreAttributes;
import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public class FrozenEffect extends StunningEffect {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("frozen", 128, 92, 32, 32);
    private static final UUID MODIFIER_UUID = UUID.fromString("caa167ec-9443-437a-a424-d886419b53fb");
    static {
        MinecraftForge.EVENT_BUS.register(new EventHandler(Effects.FROZEN));
    }
    private int stacksPastEight = 0;

    public FrozenEffect() {
        super(new StackDurationStrategy.RefreshStrategy(30 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        applyModifier(target);
        setStunTime(6, target);
        if (getStacks() > 8) {
            stacksPastEight++;
            if (stacksPastEight >= 12) {
                stacksPastEight = 0;
                setStunTime(4 * 20, target);
                BuildupSounds.playAt(target, BuildupSounds.FROZEN_20_PROC, 1, 1);
                return;
            }
        }
        BuildupSounds.playAt(target, BuildupSounds.FROZEN_PROC, 1, 1);

    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        int i = shouldBeStunned() ? 2 : 10;
        if (target.ticksExisted % i == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        for (int i = 0; i < 5; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.5f,
                particlePos.z,
                0,
                -0.02,
                0
        );
        particle.setMaxAge(30);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    private void applyModifier(EntityLivingBase target) {
        float reduction = getReduction();
        applyModifier(
                target,
                SharedMonsterAttributes.MOVEMENT_SPEED,
                new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -Math.min(reduction, 0.8f), Constants.AttributeModifierOperation.MULTIPLY)
        );
        applyModifier(
                target,
                PotionCoreAttributes.JUMP_HEIGHT,
                new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -Math.min(reduction, 0.8f), Constants.AttributeModifierOperation.MULTIPLY)
        );
        if (target.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED) != null)
            applyModifier(
                    target,
                    SharedMonsterAttributes.ATTACK_SPEED,
                    new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -reduction, Constants.AttributeModifierOperation.MULTIPLY)
        );
        applyModifier(
                target,
                Hasten.HASTEN,
                new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -reduction, Constants.AttributeModifierOperation.ADD)
        );
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("stacksPastEight", stacksPastEight);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        stacksPastEight = compound.getInteger("stacksPastEight");
    }

    public float getReduction() {
        float reduction = 0f;
        int stacks = getStacks();
        reduction += Math.clamp(stacks, 0, 8) * 0.1f;
        stacks -= 8;
        reduction += Math.clamp(stacks, 0, 7) * 0.05f;
        stacks -= 7;
        reduction += Math.max(stacks, 0) * 0.02f;
        return reduction;
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        applyModifier(target);
        if (getStacks() < 8) stacksPastEight = 0;
    }

    @Override
    public void end(EntityLivingBase target) {
        super.end(target);
        target.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER_UUID);
        target.getEntityAttribute(PotionCoreAttributes.JUMP_HEIGHT).removeModifier(MODIFIER_UUID);
        if (target.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED) != null) target.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(MODIFIER_UUID);
        target.getEntityAttribute(Hasten.HASTEN).removeModifier(MODIFIER_UUID);
    }
}
