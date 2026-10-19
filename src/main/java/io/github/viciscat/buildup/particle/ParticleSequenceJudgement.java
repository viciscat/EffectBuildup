package io.github.viciscat.buildup.particle;

import io.github.viciscat.buildup.BuildupUtils;
import io.github.viciscat.buildup.effect.JudgementEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ParticleSequenceJudgement extends ParticleSequence {
    private final EntityLivingBase target;

    public ParticleSequenceJudgement(EntityLivingBase target) {
        this.target = target;
    }

    @Override
    protected void tick() {
        if ((ticks & 1) != 0) return;
        for (int i = 0; i < 6; i++) {
            float tau = (float) Math.TAU;
            float angle = i / 6f * tau + ticks * tau / 32f;
            float cos = MathHelper.cos(angle);
            float sin = MathHelper.sin(angle);
            float v = BuildupUtils.INV_SQRT_2_F * target.width;
            BasicParticle particle = new BasicParticle(
                    target.getEntityWorld(),
                    target.posX + cos * v,
                    target.posY + 0.1,
                    target.posZ + sin * v,
                    0,
                    0.04,
                    0
            );
            particle.setParticleTexture(JudgementEffect.PARTICLE);
            particle.setMaxAge(30);
            BuildupParticleManager.INSTANCE.addParticle(particle);
        }
        if (ticks >= 40) end();
    }
}
