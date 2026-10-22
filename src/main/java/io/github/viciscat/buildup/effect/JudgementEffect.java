package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import io.github.viciscat.buildup.particle.ParticleSequenceJudgement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public class JudgementEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("judgement", 128, 64, 12, 12);
    public JudgementEffect() {
        super(new StackDurationStrategy.RefreshStrategy(60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() > 6) stackDurationStrategy.setStacks(6);
        target.attackEntityFrom(new DamageSource("fx_radiant"), procDamage * 2.5f);
    }

    public float getDamageReduction() {
        return 1 - (getStacks() * 0.1f);
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 7 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.JUDGEMENT_PROC, 1, 1);
        new ParticleSequenceJudgement(target);
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.5f,
                particlePos.z,
                0,
                0.04,
                0
        );
        particle.setParticleTexture(PARTICLE);
        particle.setMaxAge(30);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            // optional chain BECAUSE I CAN HAHAHAHA
            Optional.ofNullable(event.getSource().getTrueSource())
                    .filter(EntityLivingBase.class::isInstance)
                    .map(EntityLivingBase.class::cast)
                    .map(EffectTracker::get)
                    .flatMap(tracker -> tracker.getInstance(Effects.JUDGEMENT))
                    .ifPresent(judgement -> event.setAmount(judgement.getDamageReduction() * event.getAmount()));
        }
    }
}
