package io.github.viciscat.buildup.particle;

import io.github.viciscat.buildup.BuildupUtils;
import io.github.viciscat.buildup.effect.BleedOutEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class ParticleSequenceBleedOut extends ParticleSequence {

    private final EntityLivingBase target;
    private final Vec3d pos;

    public ParticleSequenceBleedOut(EntityLivingBase target) {
        super();
        this.target = target;
        computeLocalVectors(target);
        if (vectors == null) end();
        this.pos = target.getPositionVector().add(0, target.height / 2, 0).add(vectors.front().scale(target.width * BuildupUtils.INV_SQRT_2));
    }

    @Override
    protected void tick() {
        float progress = ticks / 4f;
        float vertical = 0.2f - progress * 0.4f;
        float horizontal = 0.3f - progress * 0.6f;
        Vec3d motion = new Vec3d(0, -0.01f, 0);
        for (int i = 0; i < 3; i++) {
            Vec3d point = pos
                    .add(vectors.side().scale(horizontal + BuildupParticleManager.RANDOM.nextDouble() * 0.04d - 0.02d))
                    .add(UP.scale(vertical + BuildupParticleManager.RANDOM.nextDouble() * 0.04d - 0.02d));
            BasicParticle particle = new BasicParticle(
                    target.getEntityWorld(),
                    point,
                    motion
            );
            particle.setMaxAge(20);
            particle.setParticleTexture(BleedOutEffect.PARTICLE);
            BuildupParticleManager.INSTANCE.addParticle(particle);
        }
        if (ticks >= 5) end();
    }
}
