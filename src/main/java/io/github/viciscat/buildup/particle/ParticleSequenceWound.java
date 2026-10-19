package io.github.viciscat.buildup.particle;

import io.github.viciscat.buildup.BuildupUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ParticleSequenceWound extends ParticleSequence {
    private static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("wound", 128, 124, 48, 48);

    private final EntityLivingBase target;
    private final Vec3d pos;

    public ParticleSequenceWound(EntityLivingBase target) {
        super();
        this.target = target;
        computeLocalVectors(target);
        if (vectors == null) end();
        this.pos = target.getPositionVector().add(0, target.height / 2, 0).add(vectors.front().scale(target.width * BuildupUtils.INV_SQRT_2));
    }

    protected void tick() {
        if (ticks == 0) {
            BasicParticle particle = new BasicParticle(
                    target.getEntityWorld(),
                    pos
            );
            particle.setMaxAge(2);
            particle.setParticleTexture(PARTICLE);
            BuildupParticleManager.INSTANCE.addParticle(particle);
        } else {
            float dist = 0.1f * ticks;
            for (int i = 0; i < 4; i++) {
                int sideOffset = (i & 1) == 0 ? 1 : -1;
                int upOffset = (i & 2) == 0 ? 1 : -1;
                BasicParticle particle = new BasicParticle(
                        target.getEntityWorld(),
                        pos.add(vectors.side().scale(dist * sideOffset)).add(ParticleSequence.UP.scale(dist * upOffset))
                );
                particle.setMaxAge(2);
                particle.setParticleTexture(PARTICLE);
                BuildupParticleManager.INSTANCE.addParticle(particle);
            }
        }
        if (ticks >= 3) end();
    }
}
