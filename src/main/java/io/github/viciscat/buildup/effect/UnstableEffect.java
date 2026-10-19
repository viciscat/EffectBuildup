package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import io.github.viciscat.buildup.particle.ParticleUnstable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class UnstableEffect extends EffectInstance {
    public UnstableEffect() {
        super(new StackDurationStrategy.RefreshStrategy(60 * 20));
    }

    public float getBuildupMultiplier() {
        float multiplier = 1f;
        int stacks = getStacks();
        multiplier += Math.clamp(stacks, 0, 10) * 0.1f;
        stacks -= 10;
        multiplier += Math.clamp(stacks, 0, 10) * 0.05f;
        stacks -= 10;
        multiplier += Math.max(stacks, 0) * 0.005f;
        return multiplier;
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 6 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.UNSTABLE_PROC, 1, 1);
        for (int i = 0; i < 10; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d vec3d = BuildupParticleManager.randomPositionAround(target);
        double randomY = target.height * 0.2 + BuildupParticleManager.RANDOM.nextDouble() * 0.5 * target.height;
        BuildupParticleManager.INSTANCE.addParticle(new ParticleUnstable(target.getEntityWorld(), vec3d.add(0, randomY, 0)));
    }
}
