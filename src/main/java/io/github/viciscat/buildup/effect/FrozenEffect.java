package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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

    public FrozenEffect() {
        super(new StackDurationStrategy.DecayingStackStrategy(30 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        applyModifier(target);
        stunTime = 6;
        if (getStacks() >= 20) {
            stackDurationStrategy.setStacks(8);
            stunTime = 4 * 20;
            BuildupSounds.playAt(target, BuildupSounds.FROZEN_20_PROC, 1, 1);
        } else {
            BuildupSounds.playAt(target, BuildupSounds.FROZEN_PROC, 1, 1);
        }
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
        float reduction = Math.min(8, getStacks()) * 0.1f;
        applyModifier(
                target,
                SharedMonsterAttributes.MOVEMENT_SPEED,
                new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -reduction, Constants.AttributeModifierOperation.MULTIPLY)
        );
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        applyModifier(target);
    }

    @Override
    public void end(EntityLivingBase target) {
        super.end(target);
        target.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER_UUID);
    }
}
